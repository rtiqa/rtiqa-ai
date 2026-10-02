package com.rtiqa.feature.profile

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.GetUserProfileUseCase
import com.rtiqa.core.domain.usecase.LogoutUseCase
import com.rtiqa.core.domain.usecase.UpdateUserProfileUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isEditing: Boolean = false,
    val editName: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState

sealed interface ProfileUiAction : ViewUiAction {
    object StartEditing : ProfileUiAction
    object CancelEditing : ProfileUiAction
    data class NameInputChanged(val name: String) : ProfileUiAction
    object SaveProfileClicked : ProfileUiAction
    object LogoutClicked : ProfileUiAction
}

sealed interface ProfileUiEvent : ViewUiEvent {
    object NavigateToLogin : ProfileUiEvent
    data class ShowToast(val message: String) : ProfileUiEvent
}

class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<ProfileUiState, ProfileUiAction, ProfileUiEvent>(ProfileUiState()) {

    init {
        observeProfile()
    }

    private fun observeProfile() {
        getUserProfileUseCase()
            .onEach { userProfile ->
                setState {
                    copy(
                        profile = userProfile,
                        editName = userProfile?.name ?: "",
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: ProfileUiAction) {
        when (action) {
            is ProfileUiAction.StartEditing -> setState { copy(isEditing = true) }
            is ProfileUiAction.CancelEditing -> setState { copy(isEditing = false, editName = profile?.name ?: "") }
            is ProfileUiAction.NameInputChanged -> setState { copy(editName = action.name, errorMessage = null) }
            is ProfileUiAction.SaveProfileClicked -> saveProfile()
            is ProfileUiAction.LogoutClicked -> logout()
        }
    }

    private fun saveProfile() {
        val currentP = currentState.profile ?: return
        val trimmedName = currentState.editName.trim()
        if (trimmedName.isBlank()) {
            val message = RtiqaError.ValidationError(listOf("Name cannot be blank.")).message
            setState { copy(errorMessage = message) }
            sendEvent(ProfileUiEvent.ShowToast(message))
            return
        }
        val updated = currentP.copy(name = trimmedName)

        setState { copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = updateUserProfileUseCase(updated)) {
                is RtiqaResult.Success -> {
                    setState { copy(isEditing = false, isSaving = false) }
                    sendEvent(ProfileUiEvent.ShowToast("تم تحديث الملف الشخصي!"))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isSaving = false, errorMessage = result.error.message) }
                    sendEvent(ProfileUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isSaving = true) }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            when (val result = logoutUseCase()) {
                is RtiqaResult.Success -> sendEvent(ProfileUiEvent.NavigateToLogin)
                is RtiqaResult.Error -> {
                    setState { copy(errorMessage = result.error.message) }
                    sendEvent(ProfileUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {}
            }
        }
    }
}
