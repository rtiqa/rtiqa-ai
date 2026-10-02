package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.AcademicDao
import com.rtiqa.core.database.entity.AssessmentAttemptEntity
import com.rtiqa.core.database.entity.AssessmentEntity
import com.rtiqa.core.database.entity.QuestionBankEntity
import com.rtiqa.core.domain.repository.RemoteSyncDataSource
import com.rtiqa.core.data.sync.OfflineSyncManager
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.Question
import com.rtiqa.core.domain.model.QuestionType
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.model.QuizResult
import com.rtiqa.core.domain.repository.QuizRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Production repository implementation for Quiz operations with Room & Firestore offline-first sync.
 */
class QuizRepositoryImpl(
    private val academicDao: AcademicDao,
    private val offlineSyncManager: OfflineSyncManager,
    private val remoteSyncDataSource: RemoteSyncDataSource? = null,
    private val currentUserIdProvider: suspend () -> String?
) : QuizRepositoryContract {

    override fun getQuizzesForCourse(courseId: String): Flow<List<Quiz>> {
        return combine(
            academicDao.getAssessmentsForCourse(courseId),
            academicDao.getQuestionsForCourse(courseId)
        ) { assessments, questions ->
            assessments.map { assessment ->
                    val quizQuestions = questions
                        .filter { it.courseId == courseId }
                        .mapNotNull { qe -> qe.toDomainOrNull() }
                    
                    Quiz(
                        id = assessment.id,
                        courseId = assessment.courseId,
                        title = assessment.title,
                        titleAr = assessment.title,
                        questions = quizQuestions,
                        passingScorePercent = assessment.passingScore,
                        durationMinutes = assessment.timeLimitMinutes,
                        timeLimitSeconds = assessment.timeLimitMinutes * 60
                    )
            }
        }
    }

    override fun getQuizForCourse(courseId: String): Flow<Quiz?> {
        return getQuizzesForCourse(courseId).map { it.firstOrNull() }
    }

    override fun getQuizById(quizId: String): Flow<Quiz?> {
        return academicDao.getAssessmentById(quizId).flatMapLatest { assessment ->
            if (assessment == null) {
                kotlinx.coroutines.flow.flowOf(null)
            } else academicDao.getQuestionsForCourse(assessment.courseId).map { questions ->
                Quiz(
                    id = assessment.id,
                    courseId = assessment.courseId,
                    title = assessment.title,
                    titleAr = assessment.title,
                    questions = questions.mapNotNull { it.toDomainOrNull() },
                    passingScorePercent = assessment.passingScore,
                    durationMinutes = assessment.timeLimitMinutes,
                    timeLimitSeconds = assessment.timeLimitMinutes * 60
                )
            }
        }
    }

    override suspend fun submitQuizResult(quizId: String, score: Int, total: Int): RtiqaResult<QuizResult> {
        return try {
            val assessment = academicDao.getAssessmentById(quizId).firstOrNull()
                ?: return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Quiz is not available.")))
            val totalCount = if (total <= 0) 1 else total
            val scorePercent = ((score.toFloat() / totalCount) * 100).toInt()
            val isPassed = scorePercent >= assessment.passingScore
            val userId = currentUserIdProvider()
                ?: return RtiqaResult.Error(RtiqaError.AuthError("An authenticated user is required to submit a quiz."))
            val courseId = assessment.courseId

            val attemptId = UUID.randomUUID().toString()
            val completedAt = System.currentTimeMillis()

            val attemptEntity = AssessmentAttemptEntity(
                id = attemptId,
                assessmentId = quizId,
                studentId = userId,
                scorePercent = scorePercent,
                isPassed = isPassed,
                autoGradedFeedback = if (isPassed) "اجتياز بنجاح! أحسنت." else "لم يتم الإجتياز، حاول مرة أخرى.",
                completedAt = completedAt
            )

            academicDao.insertAssessmentAttempt(attemptEntity)

            val payload = "{\"quizId\":\"$quizId\",\"score\":$score,\"total\":$total,\"scorePercent\":$scorePercent,\"isPassed\":$isPassed,\"attemptId\":\"$attemptId\"}"
            offlineSyncManager.enqueueOfflineAction(actionType = "SUBMIT_QUIZ_RESULT", payloadJson = payload)

            remoteSyncDataSource?.syncQuizResultToCloud(userId, quizId, score, total)

            val quizResult = QuizResult(
                id = attemptId,
                quizId = quizId,
                courseId = courseId,
                studentId = userId,
                score = score,
                totalQuestions = total,
                scorePercent = scorePercent,
                isPassed = isPassed,
                completedAt = completedAt
            )

            RtiqaResult.Success(quizResult)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to enqueue quiz result", e))
        }
    }

    override fun getQuizResultsForUser(quizId: String, userId: String): Flow<List<QuizResult>> {
        return combine(
            academicDao.getAssessmentById(quizId),
            academicDao.getAttempts(quizId, userId)
        ) { assessment, attempts ->
            if (assessment == null) emptyList() else attempts.map { attempt ->
                QuizResult(
                    id = attempt.id,
                    quizId = attempt.assessmentId,
                    courseId = assessment.courseId,
                    studentId = attempt.studentId,
                    score = (attempt.scorePercent * 10 / 100),
                    totalQuestions = 10,
                    scorePercent = attempt.scorePercent,
                    isPassed = attempt.isPassed,
                    completedAt = attempt.completedAt
                )
            }
        }
    }

    override suspend fun saveQuiz(quiz: Quiz): RtiqaResult<Unit> {
        return try {
            academicDao.insertAssessment(
                AssessmentEntity(
                    id = quiz.id,
                    courseId = quiz.courseId,
                    orgId = "org_default",
                    title = quiz.title,
                    type = "QUIZ",
                    passingScore = quiz.passingScorePercent,
                    timeLimitMinutes = quiz.durationMinutes,
                    totalQuestions = quiz.questions.size
                )
            )

            val questionEntities = quiz.questions.map { q ->
                QuestionBankEntity(
                    id = q.id,
                    courseId = quiz.courseId,
                    orgId = "org_default",
                    questionText = q.text,
                    optionA = q.options.getOrElse(0) { "" },
                    optionB = q.options.getOrElse(1) { "" },
                    optionC = q.options.getOrElse(2) { "" },
                    optionD = q.options.getOrElse(3) { "" },
                    correctAnswerIndex = q.correctAnswerIndex,
                    explanation = q.explanation,
                    difficultyLevel = "INTERMEDIATE",
                    questionType = q.type.name
                )
            }
            academicDao.insertQuestions(questionEntities)

            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to save quiz", e))
        }
    }

    private fun QuestionBankEntity.toDomainOrNull(): Question? {
        val optionsList = listOf(optionA, optionB, optionC, optionD).filter { it.isNotBlank() }
        if (questionText.isBlank() || optionsList.size < 2 || correctAnswerIndex !in optionsList.indices) return null
        val isTrueFalse = questionType == "TRUE_FALSE" || optionsList.size == 2
        val type = if (isTrueFalse) QuestionType.TRUE_FALSE else QuestionType.MULTIPLE_CHOICE

        return Question(
            id = id,
            text = questionText,
            textAr = questionText,
            options = optionsList,
            optionsAr = optionsList,
            correctAnswerIndex = correctAnswerIndex,
            explanation = explanation,
            explanationAr = explanation,
            type = type
        )
    }
}
