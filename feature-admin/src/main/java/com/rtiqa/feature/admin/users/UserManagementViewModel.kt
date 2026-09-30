package com.rtiqa.feature.admin.users

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.MemberStatus
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.usecase.DeleteEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetUsersForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class UserManagementViewModel(
    private val getUsersForSchoolUseCase: GetUsersForSchoolUseCase,
    private val saveEnterpriseMemberUseCase: SaveEnterpriseMemberUseCase,
    private val deleteEnterpriseMemberUseCase: DeleteEnterpriseMemberUseCase,
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val preferencesDataStore: RtiqaPreferencesDataStore
) : BaseViewModel<UsersUiState, UsersUiAction, UsersUiEvent>(UsersUiState()) {

    init {
        observeData()
    }

    private fun observeData() {
        val schoolsFlow = getSchoolsUseCase()
        val userPrefsFlow = preferencesDataStore.userPreferencesFlow

        combine(schoolsFlow, userPrefsFlow) { schools, userPrefs ->
            val savedActiveId = userPrefs.activeSchoolId?.takeIf { it.isNotBlank() }
            val activeSchool = if (savedActiveId != null) {
                schools.find { it.id == savedActiveId } ?: schools.firstOrNull()
            } else {
                schools.firstOrNull()
            }
            val activeId = activeSchool?.id
            setState {
                copy(
                    schools = schools,
                    activeSchoolId = activeId,
                    activeSchool = activeSchool
                )
            }
            activeId
        }.flatMapLatest { activeSchoolId ->
            if (activeSchoolId != null) {
                getUsersForSchoolUseCase(activeSchoolId)
            } else {
                flowOf(emptyList())
            }
        }.onEach { usersList ->
            setState {
                val filtered = applyFilterAndSearch(
                    users = usersList,
                    query = searchQuery,
                    roleFilter = roleFilter
                )
                copy(
                    rawUsers = usersList,
                    filteredUsers = filtered,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun applyFilterAndSearch(
        users: List<EnterpriseMember>,
        query: String,
        roleFilter: EnterpriseRole?
    ): List<EnterpriseMember> {
        val cleanQuery = query.trim().lowercase()
        return users.filter { member ->
            val matchesRole = roleFilter == null || member.role == roleFilter
            val matchesQuery = cleanQuery.isEmpty() ||
                    member.name.lowercase().contains(cleanQuery) ||
                    member.email.lowercase().contains(cleanQuery) ||
                    member.department.lowercase().contains(cleanQuery) ||
                    member.phone.lowercase().contains(cleanQuery)
            matchesRole && matchesQuery
        }
    }

    override fun onAction(action: UsersUiAction) {
        when (action) {
            is UsersUiAction.SelectActiveSchool -> {
                viewModelScope.launch {
                    preferencesDataStore.setActiveSchoolId(action.schoolId)
                    sendEvent(UsersUiEvent.ShowToast("تم التبديل إلى المدرسة النشطة بنجاح"))
                }
            }
            is UsersUiAction.SearchUsers -> {
                setState {
                    val filtered = applyFilterAndSearch(rawUsers, action.query, roleFilter)
                    copy(searchQuery = action.query, filteredUsers = filtered)
                }
            }
            is UsersUiAction.FilterByRole -> {
                setState {
                    val filtered = applyFilterAndSearch(rawUsers, searchQuery, action.role)
                    copy(roleFilter = action.role, filteredUsers = filtered)
                }
            }
            is UsersUiAction.OpenAddUserDialog -> {
                setState { copy(editingUser = null, isAddEditOpen = true) }
            }
            is UsersUiAction.OpenEditUserDialog -> {
                setState { copy(editingUser = action.user, isAddEditOpen = true, isDetailsOpen = false) }
            }
            is UsersUiAction.CloseAddEditDialog -> {
                setState { copy(editingUser = null, isAddEditOpen = false) }
            }
            is UsersUiAction.OpenUserDetails -> {
                setState { copy(selectedUserForDetails = action.user, isDetailsOpen = true) }
            }
            is UsersUiAction.CloseUserDetails -> {
                setState { copy(selectedUserForDetails = null, isDetailsOpen = false) }
            }
            is UsersUiAction.SaveUser -> {
                saveUser(action)
            }
            is UsersUiAction.DeleteUser -> {
                deleteUser(action.userId)
            }
        }
    }

    private fun saveUser(action: UsersUiAction.SaveUser) {
        val currentSchoolId = currentState.activeSchoolId
        if (currentSchoolId.isNullOrBlank()) {
            sendEvent(UsersUiEvent.ShowToast("لا توجد مدرسة نشطة لإضافة المستخدم إليها"))
            return
        }
        viewModelScope.launch {
            val userId = action.id ?: "usr_${UUID.randomUUID().toString().take(8)}"
            val member = EnterpriseMember(
                id = userId,
                orgId = "org_1",
                name = action.name,
                email = action.email,
                role = action.role,
                department = action.department,
                status = action.status,
                phone = action.phone,
                schoolId = currentSchoolId
            )
            saveEnterpriseMemberUseCase(member)
            setState { copy(isAddEditOpen = false, editingUser = null) }
            val toastMessage = if (action.id == null) "تمت إضافة المستخدم للمدرسة النشطة بنجاح" else "تم تحديث بيانات المستخدم"
            sendEvent(UsersUiEvent.ShowToast(toastMessage))
        }
    }

    private fun deleteUser(userId: String) {
        viewModelScope.launch {
            deleteEnterpriseMemberUseCase(userId)
            setState { copy(isDetailsOpen = false, selectedUserForDetails = null) }
            sendEvent(UsersUiEvent.ShowToast("تم حذف المستخدم من المدرسة النشطة"))
        }
    }
}
