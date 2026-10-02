package com.rtiqa.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.di.AppDiContainer
import com.rtiqa.feature.settings.SettingsViewModel

class SettingsViewModelFactory(
    private val appDiContainer: AppDiContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                preferences = appDiContainer.preferencesDataStore,
                syncOfflineDataUseCase = appDiContainer.domainUseCasesContainer.syncOfflineDataUseCase,
                networkStatus = appDiContainer.networkMonitor.isOnline
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
