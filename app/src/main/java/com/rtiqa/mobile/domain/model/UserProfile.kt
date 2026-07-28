package com.rtiqa.mobile.domain.model

data class UserProfile(
    val id: String = "user_001",
    val name: String = "Tariq Al-Mansoor",
    val email: String = "learner@rtiqa.edu",
    val avatarResName: String = "img_ai_tutor_avatar_1785095337393",
    val xp: Int = 2450,
    val coins: Int = 380,
    val level: Int = 5,
    val streakDays: Int = 12,
    val currentGoal: String = "Master AI Neural Networks in 30 Days",
    val language: String = "ar", // "ar" or "en"
    val isOfflineAutoSyncEnabled: Boolean = true,
    val isDarkMode: Boolean = true
)
