package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.model.QuizResult
import com.rtiqa.core.domain.repository.QuizRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.validation.QuizSubmissionValidator
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve all quizzes for a course.
 */
class GetQuizzesForCourseUseCase(
    private val quizRepository: QuizRepositoryContract
) {
    operator fun invoke(courseId: String): Flow<List<Quiz>> = quizRepository.getQuizzesForCourse(courseId)
}

/**
 * Use case to retrieve quiz assessment for a course.
 */
class GetQuizForCourseUseCase(
    private val quizRepository: QuizRepositoryContract
) {
    operator fun invoke(courseId: String): Flow<Quiz?> = quizRepository.getQuizForCourse(courseId)
}

/**
 * Use case to retrieve a quiz by ID.
 */
class GetQuizDetailUseCase(
    private val quizRepository: QuizRepositoryContract
) {
    operator fun invoke(quizId: String): Flow<Quiz?> = quizRepository.getQuizById(quizId)
}

/**
 * Use case to fetch historical quiz results for a user.
 */
class GetQuizHistoryUseCase(
    private val quizRepository: QuizRepositoryContract
) {
    operator fun invoke(quizId: String, userId: String): Flow<List<QuizResult>> =
        quizRepository.getQuizResultsForUser(quizId, userId)
}

/**
 * Domain evaluation logic for quiz answers.
 */
data class QuizEvaluation(
    val score: Int,
    val totalQuestions: Int,
    val scorePercent: Int,
    val isPassed: Boolean,
    val xpEarned: Int
)

class EvaluateQuizAnswersUseCase {
    operator fun invoke(quiz: Quiz, userAnswers: Map<String, Int>): QuizEvaluation {
        var correctCount = 0
        quiz.questions.forEach { question ->
            val answer = userAnswers[question.id]
            if (answer != null && answer == question.correctAnswerIndex) {
                correctCount++
            }
        }
        val total = quiz.questions.size
        val percent = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0
        val isPassed = quiz.isPassed(percent)
        val xpEarned = if (isPassed) 50 else 0

        return QuizEvaluation(
            score = correctCount,
            totalQuestions = total,
            scorePercent = percent,
            isPassed = isPassed,
            xpEarned = xpEarned
        )
    }
}

/**
 * Use case to evaluate and submit quiz results with automatic XP reward and streak update.
 */
class SubmitQuizResultUseCase(
    private val quizRepository: QuizRepositoryContract,
    private val userRepository: UserRepositoryContract
) {
    suspend operator fun invoke(quizId: String, score: Int, total: Int): RtiqaResult<QuizResult> {
        val validation = QuizSubmissionValidator.validate(score, total)
        if (!validation.isValid()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(validation.getErrorsOrEmpty()))
        }

        val submitResult = quizRepository.submitQuizResult(quizId, score, total)
        if (submitResult is RtiqaResult.Success) {
            val resultData = submitResult.data
            if (resultData.isPassed) {
                // Reward 50 XP for passing quiz
                userRepository.addXp(50)
                userRepository.incrementStreak()
            }
        }
        return submitResult
    }
}
