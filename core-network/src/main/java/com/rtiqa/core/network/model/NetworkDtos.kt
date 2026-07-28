package com.rtiqa.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "category") val category: String,
    @Json(name = "total_lessons") val totalLessons: Int,
    @Json(name = "duration_minutes") val durationMinutes: Int,
    @Json(name = "icon_url") val iconUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class LessonDto(
    @Json(name = "id") val id: String,
    @Json(name = "course_id") val courseId: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "order") val order: Int
)
