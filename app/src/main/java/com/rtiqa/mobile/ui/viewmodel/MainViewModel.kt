package com.rtiqa.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rtiqa.mobile.data.local.RtiqaDatabase
import com.rtiqa.mobile.data.remote.NetworkMonitor
import com.rtiqa.mobile.data.repository.UserRepository
import com.rtiqa.mobile.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RtiqaDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userProfileDao())
    private val networkMonitor = NetworkMonitor(application)

    val userProfile: StateFlow<UserProfile> = userRepository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _currentScreenRoute = MutableStateFlow("home")
    val currentScreenRoute: StateFlow<String> = _currentScreenRoute.asStateFlow()

    fun navigateTo(route: String) {
        _currentScreenRoute.value = route
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = userProfile.value.language
            val newLang = if (current == "ar") "en" else "ar"
            userRepository.updateLanguage(newLang)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = userProfile.value.isDarkMode
            userRepository.updateTheme(!current)
        }
    }

    fun toggleOfflineAutoSync() {
        viewModelScope.launch {
            val current = userProfile.value.isOfflineAutoSyncEnabled
            userRepository.updateOfflineAutoSync(!current)
        }
    }

    fun addRewards(xp: Int, coins: Int) {
        viewModelScope.launch {
            userRepository.addRewards(xp, coins)
        }
    }
}
