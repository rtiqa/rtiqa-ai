package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.repository.QuizRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.validation.QuizSubmissionValidator
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve quiz assessment for a course.
 */
class GetQuizForCourseUseCase(
    private val quizRepository: QuizRepositoryContract
) {
    operator fun invoke(courseId: String): Flow<Quiz?> = quizRepository.getQuizForCourse(courseId)
}

/**
 * Use case to evaluate and submit quiz results with automatic XP reward and streak update.
 */
class SubmitQuizResultUseCase(
    private val quizRepository: QuizRepositoryContract,
    private val userRepository: UserRepositoryContract
) {
    suspend operator fun invoke(quizId: String, score: Int, total: Int): RtiqaResult<Unit> {
        val validation = QuizSubmissionValidator.validate(score, total)
        if (!validation.isValid()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(validation.getErrorsOrEmpty()))
        }

        val submitResult = quizRepository.submitQuizResult(quizId, score, total)
        if (submitResult is RtiqaResult.Success) {
            val percent = ((score.toFloat() / total) * 100).toInt()
            if (percent >= 70) {
                // Reward 50 XP for passing quiz
                userRepository.addXp(50)
                userRepository.incrementStreak()
            }
        }
        return submitResult
    }
}
