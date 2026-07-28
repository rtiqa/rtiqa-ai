package com.rtiqa.feature.auth

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.ObserveUserSessionUseCase
import com.rtiqa.core.domain.usecase.RegisterUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState

sealed interface RegisterUiAction : ViewUiAction {
    data class NameChanged(val name: String) : RegisterUiAction
    data class EmailChanged(val email: String) : RegisterUiAction
    data class PasswordChanged(val password: String) : RegisterUiAction
    object SubmitRegister : RegisterUiAction
    object ClearError : RegisterUiAction
}

sealed interface RegisterUiEvent : ViewUiEvent {
    object NavigateToHome : RegisterUiEvent
    data class ShowMessage(val message: String) : RegisterUiEvent
}

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val observeUserSessionUseCase: ObserveUserSessionUseCase
) : BaseViewModel<RegisterUiState, RegisterUiAction, RegisterUiEvent>(RegisterUiState()) {

    init {
        observeUserSessionUseCase()
            .onEach { profile ->
                if (profile != null) {
                    sendEvent(RegisterUiEvent.NavigateToHome)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: RegisterUiAction) {
        when (action) {
            is RegisterUiAction.NameChanged -> setState { copy(name = action.name, errorMessage = null) }
            is RegisterUiAction.EmailChanged -> setState { copy(email = action.email, errorMessage = null) }
            is RegisterUiAction.PasswordChanged -> setState { copy(password = action.password, errorMessage = null) }
            is RegisterUiAction.SubmitRegister -> submitRegister()
            is RegisterUiAction.ClearError -> setState { copy(errorMessage = null) }
        }
    }

    private fun submitRegister() {
        if (currentState.isLoading) return

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = registerUseCase(currentState.name, currentState.email, currentState.password)) {
                is RtiqaResult.Success -> {
                    setState { copy(isLoading = false) }
                    sendEvent(RegisterUiEvent.ShowMessage("Account created! Welcome, ${result.data.name}!"))
                    sendEvent(RegisterUiEvent.NavigateToHome)
                }
                is RtiqaResult.Error -> {
                    val msg = result.error.message
                    setState { copy(isLoading = false, errorMessage = msg) }
                    sendEvent(RegisterUiEvent.ShowMessage(msg))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isLoading = true) }
                }
            }
        }
    }
}
