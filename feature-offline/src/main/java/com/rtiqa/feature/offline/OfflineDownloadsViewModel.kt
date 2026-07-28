package com.rtiqa.feature.offline

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.GetCoursesUseCase
import com.rtiqa.core.domain.usecase.ObserveSyncStatusUseCase
import com.rtiqa.core.domain.usecase.SyncOfflineDataUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class OfflineDownloadsUiState(
    val downloadedCourses: List<Course> = emptyList(),
    val pendingQueueCount: Int = 0,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : ViewUiState

sealed interface OfflineDownloadsUiAction : ViewUiAction {
    object TriggerSyncClicked : OfflineDownloadsUiAction
    data class CourseClicked(val courseId: String) : OfflineDownloadsUiAction
}

sealed interface OfflineDownloadsUiEvent : ViewUiEvent {
    data class NavigateToCourseDetail(val courseId: String) : OfflineDownloadsUiEvent
    data class ShowToast(val message: String) : OfflineDownloadsUiEvent
}

class OfflineDownloadsViewModel(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val syncOfflineDataUseCase: SyncOfflineDataUseCase,
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase
) : BaseViewModel<OfflineDownloadsUiState, OfflineDownloadsUiAction, OfflineDownloadsUiEvent>(OfflineDownloadsUiState()) {

    init {
        observeDownloadsAndSyncState()
    }

    private fun observeDownloadsAndSyncState() {
        combine(
            getCoursesUseCase(),
            observeSyncStatusUseCase()
        ) { courses, pendingCount ->
            val offlineList = courses.filter { it.isDownloaded }
            setState {
                copy(
                    downloadedCourses = offlineList,
                    pendingQueueCount = pendingCount,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: OfflineDownloadsUiAction) {
        when (action) {
            is OfflineDownloadsUiAction.TriggerSyncClicked -> triggerSync()
            is OfflineDownloadsUiAction.CourseClicked -> sendEvent(OfflineDownloadsUiEvent.NavigateToCourseDetail(action.courseId))
        }
    }

    private fun triggerSync() {
        if (currentState.isSyncing) return

        setState { copy(isSyncing = true) }
        viewModelScope.launch {
            when (val result = syncOfflineDataUseCase()) {
                is RtiqaResult.Success -> {
                    setState { copy(isSyncing = false) }
                    sendEvent(OfflineDownloadsUiEvent.ShowToast("Offline synchronization complete!"))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isSyncing = false, errorMessage = result.error.message) }
                    sendEvent(OfflineDownloadsUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isSyncing = true) }
                }
            }
        }
    }
}
