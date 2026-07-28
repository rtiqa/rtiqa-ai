package com.rtiqa.feature.settings

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.SyncOfflineDataUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val isOfflineModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState

sealed interface SettingsUiAction : ViewUiAction {
    data class DarkThemeToggled(val enabled: Boolean) : SettingsUiAction
    data class OfflineModeToggled(val enabled: Boolean) : SettingsUiAction
    data class NotificationsToggled(val enabled: Boolean) : SettingsUiAction
    object ManualSyncRequested : SettingsUiAction
}

sealed interface SettingsUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : SettingsUiEvent
}

class SettingsViewModel(
    private val syncOfflineDataUseCase: SyncOfflineDataUseCase
) : BaseViewModel<SettingsUiState, SettingsUiAction, SettingsUiEvent>(SettingsUiState()) {

    override fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.DarkThemeToggled -> setState { copy(isDarkTheme = action.enabled) }
            is SettingsUiAction.OfflineModeToggled -> setState { copy(isOfflineModeEnabled = action.enabled) }
            is SettingsUiAction.NotificationsToggled -> setState { copy(notificationsEnabled = action.enabled) }
            is SettingsUiAction.ManualSyncRequested -> manualSync()
        }
    }

    private fun manualSync() {
        if (currentState.isSyncing) return

        setState { copy(isSyncing = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = syncOfflineDataUseCase()) {
                is RtiqaResult.Success -> {
                    setState { copy(isSyncing = false) }
                    sendEvent(SettingsUiEvent.ShowToast("Data synchronization complete!"))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isSyncing = false, errorMessage = result.error.message) }
                    sendEvent(SettingsUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isSyncing = true) }
                }
            }
        }
    }
}
