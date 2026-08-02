package com.rtiqa.feature.admin.users

import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.MemberStatus
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState

data class UsersUiState(
    val rawUsers: List<EnterpriseMember> = emptyList(),
    val filteredUsers: List<EnterpriseMember> = emptyList(),
    val schools: List<School> = emptyList(),
    val activeSchoolId: String = "school_001",
    val activeSchool: School? = null,
    val searchQuery: String = "",
    val roleFilter: EnterpriseRole? = null,
    val selectedUserForDetails: EnterpriseMember? = null,
    val editingUser: EnterpriseMember? = null,
    val isAddEditOpen: Boolean = false,
    val isDetailsOpen: Boolean = false,
    val isLoading: Boolean = true
) : ViewUiState

sealed interface UsersUiAction : ViewUiAction {
    data class SelectActiveSchool(val schoolId: String) : UsersUiAction
    data class SearchUsers(val query: String) : UsersUiAction
    data class FilterByRole(val role: EnterpriseRole?) : UsersUiAction
    object OpenAddUserDialog : UsersUiAction
    data class OpenEditUserDialog(val user: EnterpriseMember) : UsersUiAction
    object CloseAddEditDialog : UsersUiAction
    data class OpenUserDetails(val user: EnterpriseMember) : UsersUiAction
    object CloseUserDetails : UsersUiAction
    data class SaveUser(
        val id: String?,
        val name: String,
        val email: String,
        val role: EnterpriseRole,
        val department: String,
        val phone: String,
        val status: MemberStatus
    ) : UsersUiAction
    data class DeleteUser(val userId: String) : UsersUiAction
}

sealed interface UsersUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : UsersUiEvent
}
