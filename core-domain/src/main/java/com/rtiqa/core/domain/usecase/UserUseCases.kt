package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe active user profile state.
 */
class GetUserProfileUseCase(
    private val userRepository: UserRepositoryContract
) {
    operator fun invoke(): Flow<UserProfile?> = userRepository.getUserProfile()
}

/**
 * Use case to update profile details.
 */
class UpdateUserProfileUseCase(
    private val userRepository: UserRepositoryContract
) {
    suspend operator fun invoke(profile: UserProfile): RtiqaResult<Unit> {
        if (profile.name.isBlank()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Name cannot be blank.")))
        }
        return userRepository.updateUserProfile(profile)
    }
}

/**
 * Use case to manually increment daily learning streak.
 */
class UpdateUserStreakUseCase(
    private val userRepository: UserRepositoryContract
) {
    suspend operator fun invoke(): RtiqaResult<Unit> {
        return userRepository.incrementStreak()
    }
}
