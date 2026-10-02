package com.rtiqa.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.di.AppDiContainer
import com.rtiqa.feature.profile.ProfileViewModel

class ProfileViewModelFactory(
    private val appDiContainer: AppDiContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val useCases = appDiContainer.domainUseCasesContainer
            return ProfileViewModel(
                getUserProfileUseCase = useCases.getUserProfileUseCase,
                updateUserProfileUseCase = useCases.updateUserProfileUseCase,
                logoutUseCase = useCases.logoutUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
