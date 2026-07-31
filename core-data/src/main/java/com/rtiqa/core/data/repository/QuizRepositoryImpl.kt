package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.AcademicDao
import com.rtiqa.core.database.entity.AssessmentAttemptEntity
import com.rtiqa.core.database.entity.AssessmentEntity
import com.rtiqa.core.database.entity.QuestionBankEntity
import com.rtiqa.core.data.firestore.FirestoreSyncManager
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Production repository implementation for Quiz operations with Room & Firestore offline-first sync.
 */
class QuizRepositoryImpl(
    private val academicDao: AcademicDao? = null,
    private val offlineSyncManager: OfflineSyncManager,
    private val firestoreSyncManager: FirestoreSyncManager? = null,
    private val currentUserIdProvider: (suspend () -> String?)? = null
) : QuizRepositoryContract {

    override fun getQuizzesForCourse(courseId: String): Flow<List<Quiz>> {
        if (academicDao == null) {
            return flowOf(listOf(getDefaultQuizForCourse(courseId)))
        }

        return combine(
            academicDao.getAssessmentsForCourse(courseId),
            academicDao.getQuestionsForCourse(courseId)
        ) { assessments, questions ->
            if (assessments.isEmpty()) {
                listOf(getDefaultQuizForCourse(courseId))
            } else {
                assessments.map { assessment ->
                    val quizQuestions = questions
                        .filter { it.courseId == courseId }
                        .map { qe -> qe.toDomain() }
                    
                    Quiz(
                        id = assessment.id,
                        courseId = assessment.courseId,
                        title = assessment.title,
                        titleAr = assessment.title,
                        questions = if (quizQuestions.isNotEmpty()) quizQuestions else getDefaultQuestions(),
                        passingScorePercent = assessment.passingScore,
                        durationMinutes = assessment.timeLimitMinutes,
                        timeLimitSeconds = assessment.timeLimitMinutes * 60
                    )
                }
            }
        }
    }

    override fun getQuizForCourse(courseId: String): Flow<Quiz?> {
        return getQuizzesForCourse(courseId).map { it.firstOrNull() ?: getDefaultQuizForCourse(courseId) }
    }

    override fun getQuizById(quizId: String): Flow<Quiz?> {
        if (academicDao == null) {
            val courseId = quizId.removePrefix("quiz_")
            return flowOf(getDefaultQuizForCourse(courseId))
        }

        return academicDao.getAssessmentById(quizId).map { assessment ->
            if (assessment == null) {
                val courseId = quizId.removePrefix("quiz_")
                getDefaultQuizForCourse(courseId)
            } else {
                Quiz(
                    id = assessment.id,
                    courseId = assessment.courseId,
                    title = assessment.title,
                    titleAr = assessment.title,
                    questions = getDefaultQuestions(),
                    passingScorePercent = assessment.passingScore,
                    durationMinutes = assessment.timeLimitMinutes,
                    timeLimitSeconds = assessment.timeLimitMinutes * 60
                )
            }
        }
    }

    override suspend fun submitQuizResult(quizId: String, score: Int, total: Int): RtiqaResult<QuizResult> {
        return try {
            val totalCount = if (total <= 0) 1 else total
            val scorePercent = ((score.toFloat() / totalCount) * 100).toInt()
            val isPassed = scorePercent >= 70
            val userId = currentUserIdProvider?.invoke() ?: "user_default"
            val courseId = quizId.removePrefix("quiz_")

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

            academicDao?.insertAssessmentAttempt(attemptEntity)

            val payload = "{\"quizId\":\"$quizId\",\"score\":$score,\"total\":$total,\"scorePercent\":$scorePercent,\"isPassed\":$isPassed,\"attemptId\":\"$attemptId\"}"
            offlineSyncManager.enqueueOfflineAction(actionType = "SUBMIT_QUIZ_RESULT", payloadJson = payload)

            if (userId != "user_default") {
                firestoreSyncManager?.syncQuizResultToCloud(userId, quizId, score, total)
            }

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
        if (academicDao == null) return flowOf(emptyList())

        val courseId = quizId.removePrefix("quiz_")
        return academicDao.getAttempts(quizId, userId).map { attempts ->
            attempts.map { attempt ->
                QuizResult(
                    id = attempt.id,
                    quizId = attempt.assessmentId,
                    courseId = courseId,
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
            academicDao?.insertAssessment(
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
            academicDao?.insertQuestions(questionEntities)

            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to save quiz", e))
        }
    }

    private fun getDefaultQuizForCourse(courseId: String): Quiz {
        return Quiz(
            id = "quiz_$courseId",
            courseId = courseId,
            title = "اختبار التقييم للسياري والمفاهيم الأساسية",
            titleAr = "اختبار التقييم للسياري والمفاهيم الأساسية",
            questions = getDefaultQuestions(),
            passingScorePercent = 70,
            durationMinutes = 5,
            timeLimitSeconds = 300
        )
    }

    private fun getDefaultQuestions(): List<Question> {
        return listOf(
            Question(
                id = "q1",
                text = "What is the primary benefit of Kotlin Coroutines in Android?",
                textAr = "ما هي الفائدة الرئيسية من كوروتينات كوتلن (Kotlin Coroutines) في أندرويد؟",
                options = listOf(
                    "Asynchronous non-blocking concurrency",
                    "Automatic memory garbage collection",
                    "SQL database table creation",
                    "Layout rendering optimization"
                ),
                optionsAr = listOf(
                    "البرمجة التزامنية غير الحاجبة (Non-blocking)",
                    "إدارة الذاكرة التلقائية",
                    "إنشاء جداول قاعدة البيانات",
                    "تحسين تحويل وتخطيط الواجهات"
                ),
                correctAnswerIndex = 0,
                explanation = "Coroutines simplify asynchronous execution without blocking main threads.",
                explanationAr = "تسمح الكوروتينات بتنفيذ المهام غير المتزامنة على خلفية التطبيق دون تجميد واجهة المستخدم.",
                type = QuestionType.MULTIPLE_CHOICE,
                hint = "Think about main thread responsiveness",
                hintAr = "فكر في استجابة الخيط الرئيسي (Main Thread)",
                xpReward = 15
            ),
            Question(
                id = "q2",
                text = "Jetpack Room is the official persistence library for SQLite in Android.",
                textAr = "تعتبر مكتبة Room في أندرويد الحل الرسمي الموصى به لإدارة قاعدة بيانات SQLite.",
                options = listOf("True", "False"),
                optionsAr = listOf("صح", "خطأ"),
                correctAnswerIndex = 0,
                explanation = "Room provides an abstraction layer over SQLite to allow fluent database access.",
                explanationAr = "تضمن مكتبة Room التحقق من استعلامات SQL في وقت التجميع وتسهل التعامل مع SQLite.",
                type = QuestionType.TRUE_FALSE,
                hint = "Think about Android Jetpack architecture components",
                hintAr = "تذكر مكونات البناء الأساسية في Android Jetpack",
                xpReward = 10
            ),
            Question(
                id = "q3",
                text = "Which Jetpack Compose component is used for scrollable lists?",
                textAr = "أي عنصر في Jetpack Compose يُستخدم لعرض القوائم التمريرية الكبيرة بكفاءة؟",
                options = listOf("Column", "LazyColumn", "Box", "ScrollView"),
                optionsAr = listOf("Column", "LazyColumn", "Box", "ScrollView"),
                correctAnswerIndex = 1,
                explanation = "LazyColumn renders only the visible items on screen, saving memory.",
                explanationAr = "يعوم LazyColumn بتحميل العناصر الظاهرة فقط على الشاشة مما يمنح أداءً ممتازاً.",
                type = QuestionType.MULTIPLE_CHOICE,
                hint = "It loads items lazily",
                hintAr = "يقوم بتحميل العناصر بشكل كسلان (Lazy)",
                xpReward = 15
            ),
            Question(
                id = "q4",
                text = "StateFlow replay value is always 1.",
                textAr = "مفهوم StateFlow يحتفظ دائماً بآخر قيمة (Replay = 1).",
                options = listOf("True", "False"),
                optionsAr = listOf("صح", "خطأ"),
                correctAnswerIndex = 0,
                explanation = "StateFlow is a state-holder observable flow that emits current and new state updates.",
                explanationAr = "صحيح، StateFlow يحفظ ويصدر دائماً أحدث قيمة للمشتركين الجدد.",
                type = QuestionType.TRUE_FALSE,
                hint = "Consider how StateFlow differs from SharedFlow",
                hintAr = "تذكر الفرق الرئيسي بين StateFlow و SharedFlow",
                xpReward = 10
            )
        )
    }

    private fun QuestionBankEntity.toDomain(): Question {
        val optionsList = listOf(optionA, optionB, optionC, optionD).filter { it.isNotBlank() }
        val isTrueFalse = questionType == "TRUE_FALSE" || optionsList.size == 2
        val type = if (isTrueFalse) QuestionType.TRUE_FALSE else QuestionType.MULTIPLE_CHOICE

        return Question(
            id = id,
            text = questionText,
            textAr = questionText,
            options = if (optionsList.isNotEmpty()) optionsList else listOf("Option 1", "Option 2"),
            optionsAr = if (optionsList.isNotEmpty()) optionsList else listOf("خيار 1", "خيار 2"),
            correctAnswerIndex = correctAnswerIndex,
            explanation = explanation,
            explanationAr = explanation,
            type = type,
            xpReward = 15
        )
    }
}
