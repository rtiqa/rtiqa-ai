package com.rtiqa.core.data.mapper

import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.network.model.CourseDto
import com.rtiqa.core.network.model.LessonDto

fun CourseEntity.toDomain(): Course = Course(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalLessons,
    durationMinutes = durationMinutes,
    iconUrl = iconUrl,
    isDownloaded = isDownloaded,
    progressPercent = progressPercent
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalLessons,
    durationMinutes = durationMinutes,
    iconUrl = iconUrl,
    isDownloaded = isDownloaded,
    progressPercent = progressPercent
)

fun CourseDto.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalLessons,
    durationMinutes = durationMinutes,
    iconUrl = iconUrl,
    isDownloaded = false,
    progressPercent = 0f
)

fun com.rtiqa.core.network.api.NetworkCourseDto.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalModules,
    durationMinutes = 30,
    iconUrl = null,
    isDownloaded = false,
    progressPercent = progressPercent
)

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    order = order,
    isCompleted = isCompleted,
    audioUrl = audioUrl
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    levelXp = levelXp,
    streakDays = streakDays,
    isAdmin = isAdmin,
    isOfflineModeEnabled = isOfflineModeEnabled
)
