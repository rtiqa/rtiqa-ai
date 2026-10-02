package com.rtiqa.feature.profile

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.GetUserProfileUseCase
import com.rtiqa.core.domain.usecase.LogoutUseCase
import com.rtiqa.core.domain.usecase.UpdateUserProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val profile = UserProfile("u1", "Persisted Name", "user@example.com", levelXp = 240, streakDays = 5)
    private val users = FakeUserRepository(profile)
    private val auth = FakeAuthRepository()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ProfileViewModel(
        GetUserProfileUseCase(users), UpdateUserProfileUseCase(users), LogoutUseCase(auth)
    )

    @Test fun coreProfile_isDisplayedFromGetUserProfileUseCase() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        assertEquals(profile, vm.currentState.profile)
        assertFalse(vm.currentState.isLoading)
    }

    @Test fun startEditing_prefillsCurrentName() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(ProfileUiAction.StartEditing)
        assertTrue(vm.currentState.isEditing)
        assertEquals("Persisted Name", vm.currentState.editName)
    }

    @Test fun cancelEditing_restoresPersistedName() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(ProfileUiAction.StartEditing)
        vm.onAction(ProfileUiAction.NameInputChanged("Unsaved"))
        vm.onAction(ProfileUiAction.CancelEditing)
        assertFalse(vm.currentState.isEditing)
        assertEquals("Persisted Name", vm.currentState.editName)
    }

    @Test fun saveNameSuccess_usesUpdateUserProfileUseCase() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(ProfileUiAction.StartEditing)
        vm.onAction(ProfileUiAction.NameInputChanged("Updated Name"))
        vm.onAction(ProfileUiAction.SaveProfileClicked); advanceUntilIdle()
        assertEquals("Updated Name", users.lastUpdated?.name)
        assertEquals("Updated Name", vm.currentState.profile?.name)
        assertFalse(vm.currentState.isEditing)
    }

    @Test fun saveNameFailure_surfacesRealError() = runTest {
        users.updateResult = RtiqaResult.Error(RtiqaError.DatabaseError("update failed"))
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(ProfileUiAction.NameInputChanged("Updated Name"))
        vm.onAction(ProfileUiAction.SaveProfileClicked); advanceUntilIdle()
        assertEquals("update failed", vm.currentState.errorMessage)
        assertNull(users.profile.value?.takeIf { it.name == "Updated Name" })
    }

    @Test fun blankName_doesNotInvokeUpdateOrReportSuccess() = runTest {
        val vm = viewModel(); advanceUntilIdle()
        vm.onAction(ProfileUiAction.NameInputChanged("   "))
        vm.onAction(ProfileUiAction.SaveProfileClicked); advanceUntilIdle()
        assertNull(users.lastUpdated)
        assertEquals("Name cannot be blank.", vm.currentState.errorMessage)
        assertEquals("Persisted Name", vm.currentState.profile?.name)
    }

    @Test fun remoteLogoutFailure_butRepositoryLocalLogoutSuccess_navigatesExactlyOnce() = runTest {
        val vm = viewModel(); val events = mutableListOf<ProfileUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(ProfileUiAction.LogoutClicked); advanceUntilIdle()
        assertEquals(listOf(ProfileUiEvent.NavigateToLogin), events)
    }

    @Test fun logoutFailure_doesNotNavigate_andSurfacesError() = runTest {
        auth.logoutResult = RtiqaResult.Error(RtiqaError.NetworkError("logout failed"))
        val vm = viewModel(); val events = mutableListOf<ProfileUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(ProfileUiAction.LogoutClicked); advanceUntilIdle()
        assertFalse(events.any { it is ProfileUiEvent.NavigateToLogin })
        assertEquals("logout failed", vm.currentState.errorMessage)
    }

    private class FakeUserRepository(initial: UserProfile) : UserRepositoryContract {
        val profile = MutableStateFlow<UserProfile?>(initial)
        var updateResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        var lastUpdated: UserProfile? = null
        override fun getUserProfile(): Flow<UserProfile?> = profile
        override suspend fun updateUserProfile(profile: UserProfile): RtiqaResult<Unit> {
            lastUpdated = profile
            if (updateResult is RtiqaResult.Success) this.profile.value = profile
            return updateResult
        }
        override suspend fun addXp(amount: Int) = RtiqaResult.Success(Unit)
        override suspend fun incrementStreak() = RtiqaResult.Success(Unit)
    }

    private class FakeAuthRepository : AuthRepositoryContract {
        var logoutResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        override fun observeUserSession(): Flow<UserProfile?> = MutableStateFlow(null)
        override suspend fun login(email: String, pass: String): RtiqaResult<UserProfile> = error("unused")
        override suspend fun register(name: String, email: String, pass: String): RtiqaResult<UserProfile> = error("unused")
        override suspend fun logout() = logoutResult
        override suspend fun getCurrentUserId(): String? = null
        override suspend fun resetPassword(email: String) = RtiqaResult.Success(Unit)
    }
}
