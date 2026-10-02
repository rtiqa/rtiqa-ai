package com.rtiqa.feature.settings

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.repository.SettingsPreferencesContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.SyncOfflineDataUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val languageCode: String = "ar",
    val isDarkTheme: Boolean = false,
    val isOfflineModeEnabled: Boolean = false,
    val isOnline: Boolean = true,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState {
    val isArabic: Boolean get() = languageCode == "ar"
}

sealed interface SettingsUiAction : ViewUiAction {
    data class LanguageChanged(val languageCode: String) : SettingsUiAction
    data class DarkThemeToggled(val enabled: Boolean) : SettingsUiAction
    data class OfflineModeToggled(val enabled: Boolean) : SettingsUiAction
    object ManualSyncRequested : SettingsUiAction
}

sealed interface SettingsUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : SettingsUiEvent
}

class SettingsViewModel(
    private val preferences: SettingsPreferencesContract,
    private val syncOfflineDataUseCase: SyncOfflineDataUseCase,
    networkStatus: Flow<Boolean>
) : BaseViewModel<SettingsUiState, SettingsUiAction, SettingsUiEvent>(SettingsUiState()) {

    init {
        combine(preferences.settingsFlow, networkStatus) { settings, isOnline ->
            setState {
                copy(
                    languageCode = settings.languageCode,
                    isDarkTheme = settings.isDarkTheme,
                    isOfflineModeEnabled = settings.isOfflineModeEnabled,
                    isOnline = isOnline,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.LanguageChanged -> viewModelScope.launch {
                preferences.setLanguageCode(action.languageCode)
            }
            is SettingsUiAction.DarkThemeToggled -> viewModelScope.launch {
                preferences.setDarkTheme(action.enabled)
            }
            is SettingsUiAction.OfflineModeToggled -> viewModelScope.launch {
                preferences.setOfflineMode(action.enabled)
            }
            SettingsUiAction.ManualSyncRequested -> manualSync()
        }
    }

    private fun manualSync() {
        if (currentState.isSyncing) return
        setState { copy(isSyncing = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = syncOfflineDataUseCase()) {
                is RtiqaResult.Success -> {
                    setState { copy(isSyncing = false) }
                    sendEvent(SettingsUiEvent.ShowToast("تمت مزامنة البيانات بنجاح!"))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isSyncing = false, errorMessage = result.error.message) }
                    sendEvent(SettingsUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> setState { copy(isSyncing = true) }
            }
        }
    }
}
