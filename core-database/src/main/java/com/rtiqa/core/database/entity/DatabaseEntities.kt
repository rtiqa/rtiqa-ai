package com.rtiqa.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val totalLessons: Int,
    val durationMinutes: Int,
    val iconUrl: String?,
    val isDownloaded: Boolean,
    val progressPercent: Float
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val content: String,
    val order: Int,
    val isCompleted: Boolean,
    val audioUrl: String?
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val levelXp: Int = 0,
    val streakDays: Int = 0,
    val isAdmin: Boolean = false,
    val isOfflineModeEnabled: Boolean = false
)

@Entity(tableName = "ai_insights")
data class AiInsightEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val response: String,
    val timestamp: Long,
    val modelVersion: String
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val actionType: String,
    val payloadJson: String,
    val createdAt: Long,
    val retryCount: Int = 0
)
