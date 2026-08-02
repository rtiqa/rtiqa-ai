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
        seedInitialUsersIfEmpty()
        observeData()
    }

    private fun seedInitialUsersIfEmpty() {
        viewModelScope.launch {
            getUsersForSchoolUseCase("school_001").collect { school1Users ->
                if (school1Users.isEmpty()) {
                    // Seed diverse roles for School 001
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_p1",
                            orgId = "org_1",
                            name = "د. محمد بن سلمان العتيبي",
                            email = "principal@school1.edu",
                            role = EnterpriseRole.PRINCIPAL,
                            department = "إدارة المدرسة",
                            status = MemberStatus.ACTIVE,
                            phone = "+966501112233",
                            schoolId = "school_001"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_vp1",
                            orgId = "org_1",
                            name = "أ. عبد الرحمن الغامدي",
                            email = "vp@school1.edu",
                            role = EnterpriseRole.VICE_PRINCIPAL,
                            department = "الشؤون التعليمية",
                            status = MemberStatus.ACTIVE,
                            phone = "+966502223344",
                            schoolId = "school_001"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_t1",
                            orgId = "org_1",
                            name = "أ.د. عبد الله الشهري",
                            email = "abdullah@school1.edu",
                            role = EnterpriseRole.TEACHER,
                            department = "الرياضيات والعلوم",
                            status = MemberStatus.ACTIVE,
                            phone = "+966503334455",
                            schoolId = "school_001"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_s1",
                            orgId = "org_1",
                            name = "علي أحمد المظفر",
                            email = "ali@school1.edu",
                            role = EnterpriseRole.STUDENT,
                            department = "الصف الأول الثانوي",
                            status = MemberStatus.ACTIVE,
                            phone = "+966504445566",
                            schoolId = "school_001"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_pr1",
                            orgId = "org_1",
                            name = "أحمد المظفر (ولي أمر)",
                            email = "ahmed.parent@school1.edu",
                            role = EnterpriseRole.PARENT,
                            department = "أولياء الأمور",
                            status = MemberStatus.ACTIVE,
                            phone = "+966505556677",
                            schoolId = "school_001"
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            getUsersForSchoolUseCase("school_002").collect { school2Users ->
                if (school2Users.isEmpty()) {
                    // Seed diverse roles for School 002
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_p2",
                            orgId = "org_1",
                            name = "د. نورة الزهراني",
                            email = "principal@school2.edu",
                            role = EnterpriseRole.PRINCIPAL,
                            department = "الإدارة العليا",
                            status = MemberStatus.ACTIVE,
                            phone = "+966506667788",
                            schoolId = "school_002"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_vp2",
                            orgId = "org_1",
                            name = "م. خالد القحطاني",
                            email = "vp@school2.edu",
                            role = EnterpriseRole.VICE_PRINCIPAL,
                            department = "الشؤون الإدارية",
                            status = MemberStatus.ACTIVE,
                            phone = "+966507778899",
                            schoolId = "school_002"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_t2",
                            orgId = "org_1",
                            name = "م. ريم الشمري",
                            email = "reem@school2.edu",
                            role = EnterpriseRole.TEACHER,
                            department = "الكيمياء والفيزياء",
                            status = MemberStatus.ACTIVE,
                            phone = "+966508889900",
                            schoolId = "school_002"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_s2",
                            orgId = "org_1",
                            name = "سارة خالد العتيبي",
                            email = "sara@school2.edu",
                            role = EnterpriseRole.STUDENT,
                            department = "الصف الثاني الثانوي",
                            status = MemberStatus.ACTIVE,
                            phone = "+966509990011",
                            schoolId = "school_002"
                        )
                    )
                    saveEnterpriseMemberUseCase(
                        EnterpriseMember(
                            id = "usr_pr2",
                            orgId = "org_1",
                            name = "خالد العتيبي (ولي أمر)",
                            email = "khalid.parent@school2.edu",
                            role = EnterpriseRole.PARENT,
                            department = "أولياء الأمور",
                            status = MemberStatus.ACTIVE,
                            phone = "+966500001122",
                            schoolId = "school_002"
                        )
                    )
                }
            }
        }
    }

    private fun observeData() {
        val schoolsFlow = getSchoolsUseCase()
        val userPrefsFlow = preferencesDataStore.userPreferencesFlow

        combine(schoolsFlow, userPrefsFlow) { schools, userPrefs ->
            val activeId = userPrefs.activeSchoolId.ifEmpty { schools.firstOrNull()?.id ?: "school_001" }
            val activeSchool = schools.find { it.id == activeId } ?: schools.firstOrNull()
            setState {
                copy(
                    schools = schools,
                    activeSchoolId = activeId,
                    activeSchool = activeSchool
                )
            }
            activeId
        }.flatMapLatest { activeSchoolId ->
            getUsersForSchoolUseCase(activeSchoolId)
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
        viewModelScope.launch {
            val currentSchoolId = currentState.activeSchoolId
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
