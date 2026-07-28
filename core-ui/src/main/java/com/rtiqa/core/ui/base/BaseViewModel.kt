package com.rtiqa.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Foundation BaseViewModel for Clean Architecture MVI Presentation layer.
 * Enforces immutable StateFlow state management, single-shot SharedFlow event channels,
 * and structured Action dispatching.
 */
abstract class BaseViewModel<S : ViewUiState, A : ViewUiAction, E : ViewUiEvent>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<E>(extraBufferCapacity = 16)
    val uiEvent: SharedFlow<E> = _uiEvent.asSharedFlow()

    val currentState: S
        get() = _uiState.value

    /**
     * Process incoming user interaction or system action.
     */
    abstract fun onAction(action: A)

    /**
     * Atomically update current UI state.
     */
    protected fun setState(reducer: S.() -> S) {
        _uiState.update { currentState.reducer() }
    }

    /**
     * Emit a single-shot UI event (e.g., show Toast, navigate).
     */
    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    /**
     * Utility method to safely launch coroutines on viewModelScope with standard error handling.
     */
    protected fun launchWithHandler(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (t: Throwable) {
                onError?.invoke(t)
            }
        }
    }
}
