package com.rtiqa.core.domain.model

/**
 * Sealed interface representing domain events emitted during core business workflows.
 */
sealed interface DomainEvent {
    val timestamp: Long

    data class CourseCompletedEvent(
        val courseId: String,
        val userId: String,
        val xpEarned: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DomainEvent

    data class LessonCompletedEvent(
        val lessonId: String,
        val courseId: String,
        val userId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DomainEvent

    data class QuizPassedEvent(
        val quizId: String,
        val courseId: String,
        val userId: String,
        val scorePercent: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DomainEvent

    data class StreakUpdatedEvent(
        val userId: String,
        val newStreakDays: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DomainEvent

    data class OfflineSyncCompletedEvent(
        val itemsSyncedCount: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DomainEvent
}
