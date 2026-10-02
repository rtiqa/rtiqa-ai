package com.rtiqa.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.di.AppDiContainer
import com.rtiqa.feature.offline.OfflineDownloadsViewModel

class OfflineDownloadsViewModelFactory(
    private val appDiContainer: AppDiContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfflineDownloadsViewModel::class.java)) {
            val useCases = appDiContainer.domainUseCasesContainer
            return OfflineDownloadsViewModel(
                getCoursesUseCase = useCases.getCoursesUseCase,
                syncOfflineDataUseCase = useCases.syncOfflineDataUseCase,
                observeSyncStatusUseCase = useCases.observeSyncStatusUseCase,
                deleteCourseDownloadUseCase = useCases.deleteCourseDownloadUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
