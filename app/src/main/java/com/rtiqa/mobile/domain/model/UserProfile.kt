package com.rtiqa.mobile.domain.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarResName: String = "img_ai_tutor_avatar_1785095337393",
    val xp: Int = 0,
    val coins: Int = 0,
    val level: Int = 1,
    val streakDays: Int = 0,
    val currentGoal: String = "",
    val language: String = "ar", // "ar" or "en"
    val isOfflineAutoSyncEnabled: Boolean = true,
    val isDarkMode: Boolean = true,
    val isAdmin: Boolean = false
)
