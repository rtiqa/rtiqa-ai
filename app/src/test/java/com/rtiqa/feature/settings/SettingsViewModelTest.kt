package com.rtiqa.feature.settings

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.repository.OfflineSyncContract
import com.rtiqa.core.domain.repository.SettingsPreferences
import com.rtiqa.core.domain.repository.SettingsPreferencesContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.SyncOfflineDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val preferences = FakePreferences()
    private val sync = FakeSync()
    private val network = MutableStateFlow(true)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()
    private fun viewModel() = SettingsViewModel(preferences, SyncOfflineDataUseCase(sync), network)

    @Test fun languageChange_persistsToCoreDataStore() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(SettingsUiAction.LanguageChanged("en")); advanceUntilIdle()
        assertEquals("en", preferences.settingsFlow.value.languageCode)
        assertFalse(vm.currentState.isArabic)
    }

    @Test fun invalidLanguage_fallsBackSafely() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(SettingsUiAction.LanguageChanged("invalid")); advanceUntilIdle()
        assertEquals("ar", preferences.settingsFlow.value.languageCode)
    }

    @Test fun darkThemeAndOfflineModeChanges_persist() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(SettingsUiAction.DarkThemeToggled(true))
        vm.onAction(SettingsUiAction.OfflineModeToggled(true)); advanceUntilIdle()
        assertTrue(preferences.settingsFlow.value.isDarkTheme)
        assertTrue(preferences.settingsFlow.value.isOfflineModeEnabled)
    }

    @Test fun persistedSettings_areRestoredAfterViewModelCreation() = runTest {
        preferences.settingsFlow.value = SettingsPreferences("en", true, true)
        val vm = viewModel(); advanceUntilIdle()
        assertEquals("en", vm.currentState.languageCode)
        assertTrue(vm.currentState.isDarkTheme)
        assertTrue(vm.currentState.isOfflineModeEnabled)
    }

    @Test fun manualSyncFailure_surfacesRealError_andNeverSuccess() = runTest {
        sync.result = RtiqaResult.Error(RtiqaError.SyncError("sync failed"))
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(SettingsUiAction.ManualSyncRequested); advanceUntilIdle()
        assertEquals("sync failed", vm.currentState.errorMessage)
        assertFalse(vm.currentState.isSyncing)
    }

    @Test fun manualSyncSuccess_reportsSuccessOnlyOnSuccess() = runTest {
        val vm = viewModel(); val events = mutableListOf<SettingsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(SettingsUiAction.ManualSyncRequested); advanceUntilIdle()
        assertFalse(vm.currentState.isSyncing)
        assertNull(vm.currentState.errorMessage)
        assertTrue(events.any { it is SettingsUiEvent.ShowToast && it.message.contains("بنجاح") })
    }

    @Test fun connectivity_comesFromCoreNetworkFlow() = runTest {
        val vm = viewModel(); advanceUntilIdle(); assertTrue(vm.currentState.isOnline)
        network.value = false; advanceUntilIdle(); assertFalse(vm.currentState.isOnline)
    }

    private class FakePreferences : SettingsPreferencesContract {
        override val settingsFlow = MutableStateFlow(SettingsPreferences())
        override suspend fun setLanguageCode(languageCode: String) {
            settingsFlow.value = settingsFlow.value.copy(languageCode = if (languageCode == "en") "en" else "ar")
        }
        override suspend fun setDarkTheme(enabled: Boolean) { settingsFlow.value = settingsFlow.value.copy(isDarkTheme = enabled) }
        override suspend fun setOfflineMode(enabled: Boolean) { settingsFlow.value = settingsFlow.value.copy(isOfflineModeEnabled = enabled) }
    }

    private class FakeSync : OfflineSyncContract {
        var result: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        override suspend fun syncRemoteCourses() = result
        override suspend fun enqueueOfflineAction(actionType: String, payloadJson: String) = RtiqaResult.Success(Unit)
        override fun observePendingSyncCount() = MutableStateFlow(0)
    }
}
