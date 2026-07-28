package com.rtiqa.mobile.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String,
    val iconName: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val xpReward: Int
)

data class Certificate(
    val id: String,
    val courseId: String,
    val courseTitle: String,
    val courseTitleAr: String,
    val issueDate: String,
    val certificateCode: String,
    val learnerName: String
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val xp: Int,
    val countryFlag: String,
    val isCurrentUser: Boolean = false
)

data class SyncQueueItem(
    val id: String,
    val actionType: String, // e.g. "LESSON_COMPLETED", "QUIZ_SUBMITTED", "BOOKMARK_TOGGLED"
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // PENDING, SYNCED, FAILED
)
