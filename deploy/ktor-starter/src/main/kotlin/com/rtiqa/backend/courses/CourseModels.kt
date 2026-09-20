package com.rtiqa.backend.courses

import kotlinx.serialization.Serializable

@Serializable
data class CourseResponseDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val totalModules: Int,
    val completedModules: Int,
    val progressPercent: Float
)

@Serializable
data class LessonResponseDto(
    val id: String,
    val courseId: String,
    val title: String,
    val content: String,
    val moduleOrder: Int,
    val estimatedMinutes: Int,
    val isCompleted: Boolean = false
)

@Serializable
data class CompleteLessonRequestDto(
    val score: Int? = null
)

@Serializable
data class LessonCompletionResponseDto(
    val success: Boolean,
    val lessonId: String,
    val courseId: String,
    val completed: Boolean,
    val courseProgressPercent: Float,
    val completedLessons: Int,
    val totalLessons: Int
)

sealed class CompleteLessonResult {
    data class Success(val completion: LessonCompletionResponseDto) : CompleteLessonResult()
    object CourseNotFound : CompleteLessonResult()
    object LessonNotFound : CompleteLessonResult()
    object NotEnrolled : CompleteLessonResult()
}

@Serializable
data class ErrorResponseDto(
    val status: Int,
    val message: String
)
