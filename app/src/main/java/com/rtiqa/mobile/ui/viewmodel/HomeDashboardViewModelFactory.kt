package com.rtiqa.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.di.AppDiContainer
import com.rtiqa.feature.home.HomeDashboardViewModel

class HomeDashboardViewModelFactory(
    private val appDiContainer: AppDiContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeDashboardViewModel::class.java)) {
            val useCases = appDiContainer.domainUseCasesContainer
            return HomeDashboardViewModel(
                getUserProfileUseCase = useCases.getUserProfileUseCase,
                getCoursesUseCase = useCases.getCoursesUseCase,
                observeSyncStatusUseCase = useCases.observeSyncStatusUseCase,
                updateUserStreakUseCase = useCases.updateUserStreakUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
