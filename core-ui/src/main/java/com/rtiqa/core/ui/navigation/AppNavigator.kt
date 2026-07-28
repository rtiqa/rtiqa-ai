package com.rtiqa.core.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class NavigationIntent {
    data class NavigateTo(
        val route: String,
        val popUpToRoute: String? = null,
        val inclusive: Boolean = false
    ) : NavigationIntent()

    object NavigateBack : NavigationIntent()
}

/**
 * Singleton navigation flow manager for decoupled navigation calls from ViewModels/Services.
 */
class AppNavigator {

    private val _navigationFlow = MutableSharedFlow<NavigationIntent>(extraBufferCapacity = 16)
    val navigationFlow: SharedFlow<NavigationIntent> = _navigationFlow.asSharedFlow()

    fun navigateTo(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = false
    ) {
        _navigationFlow.tryEmit(
            NavigationIntent.NavigateTo(
                route = route,
                popUpToRoute = popUpToRoute,
                inclusive = inclusive
            )
        )
    }

    fun navigateBack() {
        _navigationFlow.tryEmit(NavigationIntent.NavigateBack)
    }
}
