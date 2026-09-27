package com.rtiqa.backend.courses

import kotlinx.serialization.Serializable

@Serializable
data class CreateCourseRequestDto(
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val level: String? = null,
    val durationMinutes: Int? = null,
    val iconUrl: String? = null,
    val imageUrl: String? = null
)

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
    val completed: Boolean? = true,
    val score: Int? = null
)

@Serializable
data class CreateLessonRequestDto(
    val title: String,
    val content: String? = null,
    val moduleOrder: Int? = null,
    val estimatedMinutes: Int? = null,
    val audioUrl: String? = null
)

sealed class CreateLessonResult {
    data class Success(val lesson: LessonResponseDto) : CreateLessonResult()
    object CourseNotFound : CreateLessonResult()
}

sealed class GetLessonResult {
    data class Success(val lesson: LessonResponseDto) : GetLessonResult()
    object CourseNotFound : GetLessonResult()
    object LessonNotFound : GetLessonResult()
}

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
