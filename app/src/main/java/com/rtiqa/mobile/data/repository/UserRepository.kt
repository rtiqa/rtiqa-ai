package com.rtiqa.mobile.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rtiqa.mobile.data.local.dao.UserProfileDao
import com.rtiqa.mobile.data.local.entity.UserProfileEntity
import com.rtiqa.mobile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val userProfileDao: UserProfileDao) {

    private val firebaseAuth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    val userProfile: Flow<UserProfile> = userProfileDao.getUserProfile().map { entity ->
        entity?.toDomain() ?: getCurrentFirebaseUserProfile() ?: UserProfile()
    }

    private fun getCurrentFirebaseUserProfile(): UserProfile? {
        val user = firebaseAuth?.currentUser ?: return null
        return UserProfile(
            id = user.uid,
            name = user.displayName ?: user.email?.substringBefore("@") ?: "",
            email = user.email ?: "",
            avatarResName = "img_ai_tutor_avatar_1785095337393",
            xp = 0,
            coins = 0,
            level = 1,
            streakDays = 1
        )
    }

    suspend fun createUser(id: String, name: String, email: String): UserProfile {
        val entity = UserProfileEntity(
            id = id,
            name = name,
            email = email,
            avatarResName = "img_ai_tutor_avatar_1785095337393",
            xp = 0,
            coins = 0,
            level = 1,
            streakDays = 1,
            currentGoal = "",
            language = "ar",
            isOfflineAutoSyncEnabled = true,
            isDarkMode = true
        )
        userProfileDao.saveUserProfile(entity)
        syncWithFirestore(entity.toDomain())
        return entity.toDomain()
    }

    suspend fun fetchUserProfileFromFirestore(userId: String): UserProfile? {
        val db = firestore ?: return null
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val entity = UserProfileEntity(
                    id = doc.getString("id") ?: userId,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    avatarResName = doc.getString("avatarResName") ?: "img_ai_tutor_avatar_1785095337393",
                    xp = (doc.getLong("xp") ?: doc.getLong("levelXp") ?: 0L).toInt(),
                    coins = (doc.getLong("coins") ?: 0L).toInt(),
                    level = (doc.getLong("level") ?: 1L).toInt(),
                    streakDays = (doc.getLong("streakDays") ?: 0L).toInt(),
                    currentGoal = doc.getString("currentGoal") ?: "",
                    language = doc.getString("language") ?: "ar",
                    isOfflineAutoSyncEnabled = doc.getBoolean("isOfflineAutoSyncEnabled") ?: true,
                    isDarkMode = doc.getBoolean("isDarkMode") ?: true
                )
                userProfileDao.saveUserProfile(entity)
                entity.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncWithFirestore(profile: UserProfile) {
        val db = firestore ?: return
        try {
            val userMap = hashMapOf(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "xp" to profile.xp,
                "coins" to profile.coins,
                "level" to profile.level,
                "streakDays" to profile.streakDays,
                "currentGoal" to profile.currentGoal,
                "language" to profile.language,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(profile.id).set(userMap, SetOptions.merge()).await()
        } catch (e: Exception) {
            // Ignore offline network errors
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val entity = UserProfileEntity(
            id = profile.id,
            name = profile.name,
            email = profile.email,
            avatarResName = profile.avatarResName,
            xp = profile.xp,
            coins = profile.coins,
            level = profile.level,
            streakDays = profile.streakDays,
            currentGoal = profile.currentGoal,
            language = profile.language,
            isOfflineAutoSyncEnabled = profile.isOfflineAutoSyncEnabled,
            isDarkMode = profile.isDarkMode
        )
        userProfileDao.saveUserProfile(entity)
        syncWithFirestore(profile)
    }

    suspend fun addRewards(xpGained: Int, coinsGained: Int) {
        val activeId = firebaseAuth?.currentUser?.uid ?: ""
        userProfileDao.addRewards(activeId, xpGained, coinsGained)
    }

    suspend fun updateLanguage(language: String) {
        val activeId = firebaseAuth?.currentUser?.uid ?: ""
        userProfileDao.updateLanguage(activeId, language)
    }

    suspend fun updateTheme(isDark: Boolean) {
        val activeId = firebaseAuth?.currentUser?.uid ?: ""
        userProfileDao.updateTheme(activeId, isDark)
    }

    suspend fun updateOfflineAutoSync(enabled: Boolean) {
        val activeId = firebaseAuth?.currentUser?.uid ?: ""
        userProfileDao.updateOfflineAutoSync(activeId, enabled)
    }

    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id,
        name = name,
        email = email,
        avatarResName = avatarResName,
        xp = xp,
        coins = coins,
        level = level,
        streakDays = streakDays,
        currentGoal = currentGoal,
        language = language,
        isOfflineAutoSyncEnabled = isOfflineAutoSyncEnabled,
        isDarkMode = isDarkMode,
        isAdmin = email.endsWith("@rtiqa.edu") || email == "irtiqahq@gmail.com"
    )
}

