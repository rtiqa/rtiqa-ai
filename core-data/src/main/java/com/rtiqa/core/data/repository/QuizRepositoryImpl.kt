package com.rtiqa.core.data.repository

import com.rtiqa.core.data.sync.OfflineSyncManager
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.Question
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.repository.QuizRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Production repository implementation for Quiz operations.
 */
class QuizRepositoryImpl(
    private val offlineSyncManager: OfflineSyncManager
) : QuizRepositoryContract {

    override fun getQuizForCourse(courseId: String): Flow<Quiz?> {
        // Return structured quiz for the course
        return flowOf(
            Quiz(
                id = "quiz_$courseId",
                courseId = courseId,
                title = "Course Assessment Quiz",
                questions = listOf(
                    Question(
                        id = "q1",
                        text = "What is the primary benefit of Kotlin Coroutines in Android?",
                        options = listOf(
                            "Asynchronous non-blocking concurrency",
                            "Automatic memory garbage collection",
                            "SQL database table creation",
                            "Layout rendering optimization"
                        ),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        id = "q2",
                        text = "Which Jetpack library is recommended for local database persistence?",
                        options = listOf("Room", "Retrofit", "DataStore", "WorkManager"),
                        correctAnswerIndex = 0
                    )
                )
            )
        )
    }

    override suspend fun submitQuizResult(quizId: String, score: Int, total: Int): RtiqaResult<Unit> {
        return try {
            val payload = "{\"quizId\":\"$quizId\",\"score\":$score,\"total\":$total}"
            offlineSyncManager.enqueueOfflineAction(actionType = "SUBMIT_QUIZ_RESULT", payloadJson = payload)
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.DatabaseError("Failed to enqueue quiz result", e))
        }
    }
}
