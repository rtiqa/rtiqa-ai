package com.rtiqa.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleAr: String,
    val category: String,
    val categoryAr: String,
    val description: String,
    val descriptionAr: String,
    val rating: Float,
    val durationMinutes: Int,
    val totalLessons: Int,
    val enrolledCount: Int,
    val imageResName: String,
    val level: String,
    val tagsCsv: String,
    val progressPercent: Float = 0f,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val titleAr: String,
    val durationMinutes: Int,
    val orderIndex: Int,
    val videoUrl: String,
    val audioUrl: String,
    val contentMarkdown: String,
    val contentMarkdownAr: String,
    val isCompleted: Boolean = false,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "",
    val name: String,
    val email: String,
    val avatarResName: String,
    val xp: Int,
    val coins: Int,
    val level: Int,
    val streakDays: Int,
    val currentGoal: String,
    val language: String,
    val isOfflineAutoSyncEnabled: Boolean,
    val isDarkMode: Boolean
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val actionType: String,
    val payloadJson: String,
    val timestamp: Long,
    val status: String
)
