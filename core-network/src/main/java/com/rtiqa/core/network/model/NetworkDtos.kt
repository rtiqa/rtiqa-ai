package com.rtiqa.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseDto(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "description") val description: String,
    @field:Json(name = "category") val category: String,
    @field:Json(name = "total_lessons") val totalLessons: Int,
    @field:Json(name = "duration_minutes") val durationMinutes: Int,
    @field:Json(name = "icon_url") val iconUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class LessonDto(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "course_id") val courseId: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "content") val content: String,
    @field:Json(name = "order") val order: Int
)
