package com.rtiqa.core.domain.model

/**
 * Domain entity representing the authenticated user's profile and progress state.
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val levelXp: Int = 0,
    val streakDays: Int = 0,
    val isAdmin: Boolean = false,
    val isOfflineModeEnabled: Boolean = true
) {
    /**
     * Business rule: Calculates user level based on accumulated XP (100 XP per level).
     */
    fun calculateLevel(): Int = (levelXp / 100) + 1

    /**
     * Business rule: Calculates XP progress percentage toward the next level.
     */
    fun calculateLevelProgressPercent(): Float = (levelXp % 100) / 100.0f

    /**
     * Business rule: Creates updated profile with incremented XP.
     */
    fun addXp(amount: Int): UserProfile = copy(levelXp = levelXp + amount)

    /**
     * Business rule: Creates updated profile with incremented streak.
     */
    fun incrementStreak(): UserProfile = copy(streakDays = streakDays + 1)
}
