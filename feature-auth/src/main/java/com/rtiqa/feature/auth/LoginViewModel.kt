package com.rtiqa.feature.auth

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.LoginUseCase
import com.rtiqa.core.domain.usecase.ObserveUserSessionUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isOfflineMode: Boolean = false
) : ViewUiState

sealed interface LoginUiAction : ViewUiAction {
    data class EmailChanged(val email: String) : LoginUiAction
    data class PasswordChanged(val password: String) : LoginUiAction
    object SubmitLogin : LoginUiAction
    object ClearError : LoginUiAction
}

sealed interface LoginUiEvent : ViewUiEvent {
    object NavigateToHome : LoginUiEvent
    data class ShowMessage(val message: String) : LoginUiEvent
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val observeUserSessionUseCase: ObserveUserSessionUseCase
) : BaseViewModel<LoginUiState, LoginUiAction, LoginUiEvent>(LoginUiState()) {

    init {
        observeUserSessionUseCase()
            .onEach { profile ->
                if (profile != null) {
                    sendEvent(LoginUiEvent.NavigateToHome)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.EmailChanged -> {
                setState { copy(email = action.email, emailError = null, errorMessage = null) }
            }
            is LoginUiAction.PasswordChanged -> {
                setState { copy(password = action.password, passwordError = null, errorMessage = null) }
            }
            is LoginUiAction.SubmitLogin -> submitLogin()
            is LoginUiAction.ClearError -> {
                setState { copy(errorMessage = null, emailError = null, passwordError = null) }
            }
        }
    }

    private fun submitLogin() {
        if (currentState.isLoading) return

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = loginUseCase(currentState.email, currentState.password)) {
                is RtiqaResult.Success -> {
                    setState { copy(isLoading = false) }
                    sendEvent(LoginUiEvent.ShowMessage("Welcome back, ${result.data.name}!"))
                    sendEvent(LoginUiEvent.NavigateToHome)
                }
                is RtiqaResult.Error -> {
                    val msg = result.error.message
                    setState { copy(isLoading = false, errorMessage = msg) }
                    sendEvent(LoginUiEvent.ShowMessage(msg))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isLoading = true) }
                }
            }
        }
    }
}
