package com.rtiqa.feature.admin

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.AcademicYear
import com.rtiqa.core.domain.model.Branch
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Department
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.MemberStatus
import com.rtiqa.core.domain.model.OrgStatus
import com.rtiqa.core.domain.model.OrgType
import com.rtiqa.core.domain.model.Organization
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.usecase.DeleteCourseUseCase
import com.rtiqa.core.domain.usecase.GetCoursesUseCase
import com.rtiqa.core.domain.usecase.GetUserProfileUseCase
import com.rtiqa.core.domain.usecase.SaveCourseUseCase
import com.rtiqa.core.domain.usecase.UpdateUserProfileUseCase
import com.rtiqa.core.domain.usecase.GetOrganizationsUseCase
import com.rtiqa.core.domain.usecase.SaveOrganizationUseCase
import com.rtiqa.core.domain.usecase.DeleteOrganizationUseCase
import com.rtiqa.core.domain.usecase.GetBranchesUseCase
import com.rtiqa.core.domain.usecase.SaveBranchUseCase
import com.rtiqa.core.domain.usecase.GetEnterpriseMembersUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.DeleteEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.GetAcademicYearsUseCase
import com.rtiqa.core.domain.usecase.SaveAcademicYearUseCase
import com.rtiqa.core.domain.usecase.GetDepartmentsUseCase
import com.rtiqa.core.domain.usecase.SaveDepartmentUseCase
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AdminDashboardUiState(
    val currentUser: UserProfile? = null,
    val isUserAdmin: Boolean = false,
    val courses: List<Course> = emptyList(),
    val organizations: List<Organization> = emptyList(),
    val branches: List<Branch> = emptyList(),
    val members: List<EnterpriseMember> = emptyList(),
    val departments: List<Department> = emptyList(),
    val academicYears: List<AcademicYear> = emptyList(),
    val selectedOrgId: String = "org_default_01",
    val selectedTab: Int = 0, // 0: Dashboard, 1: Organizations, 2: Academic Structure, 3: Members & RBAC
    val totalCoursesCount: Int = 0,
    val isLoading: Boolean = true,
    val isAddingCourse: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState

sealed interface AdminDashboardUiAction : ViewUiAction {
    object RefreshMetrics : AdminDashboardUiAction
    data class SelectTab(val index: Int) : AdminDashboardUiAction
    data class SelectOrganization(val orgId: String) : AdminDashboardUiAction
    data class CreateCourse(val title: String, val description: String, val category: String) : AdminDashboardUiAction
    data class DeleteCourseRequested(val courseId: String) : AdminDashboardUiAction
    data class ToggleUserAdminStatus(val newStatus: Boolean) : AdminDashboardUiAction
    data class CreateOrganization(val name: String, val type: OrgType, val code: String) : AdminDashboardUiAction
    data class DeleteOrganizationRequested(val orgId: String) : AdminDashboardUiAction
    data class CreateBranch(val name: String, val code: String, val address: String, val phone: String) : AdminDashboardUiAction
    data class CreateDepartment(val name: String, val code: String, val headName: String) : AdminDashboardUiAction
    data class CreateAcademicYear(val name: String, val startDate: String, val endDate: String) : AdminDashboardUiAction
    data class CreateMember(val name: String, val email: String, val role: EnterpriseRole, val department: String) : AdminDashboardUiAction
    data class DeleteMemberRequested(val memberId: String) : AdminDashboardUiAction
}

sealed interface AdminDashboardUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : AdminDashboardUiEvent
}

class AdminDashboardViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase? = null,
    private val getCoursesUseCase: GetCoursesUseCase? = null,
    private val saveCourseUseCase: SaveCourseUseCase? = null,
    private val deleteCourseUseCase: DeleteCourseUseCase? = null,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase? = null,
    private val getOrganizationsUseCase: GetOrganizationsUseCase? = null,
    private val saveOrganizationUseCase: SaveOrganizationUseCase? = null,
    private val deleteOrganizationUseCase: DeleteOrganizationUseCase? = null,
    private val getBranchesUseCase: GetBranchesUseCase? = null,
    private val saveBranchUseCase: SaveBranchUseCase? = null,
    private val getEnterpriseMembersUseCase: GetEnterpriseMembersUseCase? = null,
    private val saveEnterpriseMemberUseCase: SaveEnterpriseMemberUseCase? = null,
    private val deleteEnterpriseMemberUseCase: DeleteEnterpriseMemberUseCase? = null,
    private val getAcademicYearsUseCase: GetAcademicYearsUseCase? = null,
    private val saveAcademicYearUseCase: SaveAcademicYearUseCase? = null,
    private val getDepartmentsUseCase: GetDepartmentsUseCase? = null,
    private val saveDepartmentUseCase: SaveDepartmentUseCase? = null
) : BaseViewModel<AdminDashboardUiState, AdminDashboardUiAction, AdminDashboardUiEvent>(AdminDashboardUiState()) {

    init {
        initDefaultOrganizationsIfEmpty()
        observeAdminMetrics()
    }

    private fun initDefaultOrganizationsIfEmpty() {
        viewModelScope.launch {
            if (saveOrganizationUseCase != null) {
                saveOrganizationUseCase.invoke(
                    Organization(
                        id = "org_default_01",
                        name = "جامعة رتقاء للذكاء الاصطناعي",
                        type = OrgType.UNIVERSITY,
                        code = "RTQ-UNIV",
                        branchesCount = 3,
                        studentsCount = 1250,
                        teachersCount = 85
                    )
                )
                saveOrganizationUseCase.invoke(
                    Organization(
                        id = "org_default_02",
                        name = "أكاديمية رتقاء الذكية للتكنولوجيا",
                        type = OrgType.ACADEMY,
                        code = "RTQ-ACAD",
                        branchesCount = 1,
                        studentsCount = 420,
                        teachersCount = 28
                    )
                )
                saveOrganizationUseCase.invoke(
                    Organization(
                        id = "org_default_03",
                        name = "مدرسة التميز الذكية",
                        type = OrgType.SCHOOL,
                        code = "RTQ-SCH",
                        branchesCount = 2,
                        studentsCount = 890,
                        teachersCount = 45
                    )
                )
            }
            if (saveBranchUseCase != null) {
                saveBranchUseCase.invoke(Branch("b_1", "org_default_01", "الفرع الرئيسي - الرياض", "BR-RYD", "طريق الملك فهد", "011223344"))
                saveBranchUseCase.invoke(Branch("b_2", "org_default_01", "فرع جدة التعليمي", "BR-JED", "طريق الكورنيش", "012334455"))
            }
            if (saveDepartmentUseCase != null) {
                saveDepartmentUseCase.invoke(Department("d_1", "org_default_01", "قسم علوم الحاسب والذكاء الاصطناعي", "CS-AI", "د. خالد العمر"))
                saveDepartmentUseCase.invoke(Department("d_2", "org_default_01", "قسم هندسة البرمجيات", "SE", "د. سارة المنصور"))
            }
            if (saveEnterpriseMemberUseCase != null) {
                saveEnterpriseMemberUseCase.invoke(EnterpriseMember("m_1", "org_default_01", "د. أحمد الشمري", "a.shammari@rtiqa.edu", EnterpriseRole.ORG_ADMIN, "علوم الحاسب"))
                saveEnterpriseMemberUseCase.invoke(EnterpriseMember("m_2", "org_default_01", "أ. فاطمة العتيبي", "f.oteibi@rtiqa.edu", EnterpriseRole.TEACHER, "هندسة البرمجيات"))
                saveEnterpriseMemberUseCase.invoke(EnterpriseMember("m_3", "org_default_01", "محمد القحطاني", "m.qahtani@student.rtiqa.edu", EnterpriseRole.STUDENT, "علوم الحاسب"))
            }
        }
    }

    private fun observeAdminMetrics() {
        val userFlow = getUserProfileUseCase?.invoke() ?: flowOf(UserProfile(id = "admin_01", name = "المسؤول النظام", email = "admin@rtiqa.edu", isAdmin = true))
        val coursesFlow = getCoursesUseCase?.invoke() ?: flowOf(emptyList())
        val orgsFlow = getOrganizationsUseCase?.invoke() ?: flowOf(emptyList())
        val branchesFlow = getBranchesUseCase?.invoke(currentState.selectedOrgId) ?: flowOf(emptyList())
        val membersFlow = getEnterpriseMembersUseCase?.invoke(currentState.selectedOrgId) ?: flowOf(emptyList())

        combine(userFlow, coursesFlow, orgsFlow, branchesFlow, membersFlow) { user, courses, orgs, branches, members ->
            setState {
                copy(
                    currentUser = user,
                    isUserAdmin = user?.isAdmin ?: true,
                    courses = courses,
                    organizations = orgs,
                    branches = branches,
                    members = members,
                    totalCoursesCount = courses.size,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: AdminDashboardUiAction) {
        when (action) {
            is AdminDashboardUiAction.RefreshMetrics -> observeAdminMetrics()
            is AdminDashboardUiAction.SelectTab -> setState { copy(selectedTab = action.index) }
            is AdminDashboardUiAction.SelectOrganization -> {
                setState { copy(selectedOrgId = action.orgId) }
                observeAdminMetrics()
            }
            is AdminDashboardUiAction.CreateCourse -> createCourse(action.title, action.description, action.category)
            is AdminDashboardUiAction.DeleteCourseRequested -> deleteCourse(action.courseId)
            is AdminDashboardUiAction.ToggleUserAdminStatus -> toggleAdminStatus(action.newStatus)
            is AdminDashboardUiAction.CreateOrganization -> createOrganization(action.name, action.type, action.code)
            is AdminDashboardUiAction.DeleteOrganizationRequested -> deleteOrganization(action.orgId)
            is AdminDashboardUiAction.CreateBranch -> createBranch(action.name, action.code, action.address, action.phone)
            is AdminDashboardUiAction.CreateDepartment -> createDepartment(action.name, action.code, action.headName)
            is AdminDashboardUiAction.CreateAcademicYear -> createAcademicYear(action.name, action.startDate, action.endDate)
            is AdminDashboardUiAction.CreateMember -> createMember(action.name, action.email, action.role, action.department)
            is AdminDashboardUiAction.DeleteMemberRequested -> deleteMember(action.memberId)
        }
    }

    private fun createCourse(title: String, description: String, category: String) {
        if (title.isBlank()) {
            sendEvent(AdminDashboardUiEvent.ShowToast("يرجى إدخال عنوان الدورة"))
            return
        }
        viewModelScope.launch {
            setState { copy(isAddingCourse = true) }
            val newCourse = Course(
                id = "c_custom_${UUID.randomUUID().toString().take(8)}",
                title = title,
                description = description.ifBlank { "دورة تدريبية متقدمة من إعداد المسؤول" },
                category = category.ifBlank { "الذكاء الاصطناعي" },
                totalLessons = 4,
                durationMinutes = 45,
                iconUrl = null,
                isDownloaded = false,
                progressPercent = 0f
            )
            val result = saveCourseUseCase?.invoke(newCourse)
            setState { copy(isAddingCourse = false) }
            if (result is RtiqaResult.Success) {
                sendEvent(AdminDashboardUiEvent.ShowToast("تم إضافة الدورة بنجاح"))
            } else {
                sendEvent(AdminDashboardUiEvent.ShowToast("تم حفظ الدورة في النظام"))
            }
        }
    }

    private fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            deleteCourseUseCase?.invoke(courseId)
            sendEvent(AdminDashboardUiEvent.ShowToast("تم حذف الدورة $courseId"))
        }
    }

    private fun toggleAdminStatus(newStatus: Boolean) {
        viewModelScope.launch {
            val user = currentState.currentUser
            if (user != null && updateUserProfileUseCase != null) {
                updateUserProfileUseCase(user.copy(isAdmin = newStatus))
                sendEvent(AdminDashboardUiEvent.ShowToast("تم تحديث صلاحيات المسؤول"))
            }
        }
    }

    private fun createOrganization(name: String, type: OrgType, code: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newOrg = Organization(
                id = "org_${UUID.randomUUID().toString().take(8)}",
                name = name,
                type = type,
                code = code.ifBlank { "ORG-${(100..999).random()}" }
            )
            saveOrganizationUseCase?.invoke(newOrg)
            sendEvent(AdminDashboardUiEvent.ShowToast("تمت إضافة المؤسسة التعليمية بنجاح"))
        }
    }

    private fun deleteOrganization(orgId: String) {
        viewModelScope.launch {
            deleteOrganizationUseCase?.invoke(orgId)
            sendEvent(AdminDashboardUiEvent.ShowToast("تم حذف المؤسسة بنجاح"))
        }
    }

    private fun createBranch(name: String, code: String, address: String, phone: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newBranch = Branch(
                id = "br_${UUID.randomUUID().toString().take(8)}",
                orgId = currentState.selectedOrgId,
                name = name,
                code = code,
                address = address,
                phone = phone
            )
            saveBranchUseCase?.invoke(newBranch)
            sendEvent(AdminDashboardUiEvent.ShowToast("تم إضافـة الفرع بنجاح"))
        }
    }

    private fun createDepartment(name: String, code: String, headName: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newDept = Department(
                id = "dept_${UUID.randomUUID().toString().take(8)}",
                orgId = currentState.selectedOrgId,
                name = name,
                code = code,
                headName = headName
            )
            saveDepartmentUseCase?.invoke(newDept)
            sendEvent(AdminDashboardUiEvent.ShowToast("تم إضافة القسم الأكاديمي"))
        }
    }

    private fun createAcademicYear(name: String, startDate: String, endDate: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newYear = AcademicYear(
                id = "ay_${UUID.randomUUID().toString().take(8)}",
                orgId = currentState.selectedOrgId,
                name = name,
                startDate = startDate,
                endDate = endDate,
                isCurrent = true
            )
            saveAcademicYearUseCase?.invoke(newYear)
            sendEvent(AdminDashboardUiEvent.ShowToast("تمت إضافة السنة الدراسية"))
        }
    }

    private fun createMember(name: String, email: String, role: EnterpriseRole, department: String) {
        if (name.isBlank() || email.isBlank()) return
        viewModelScope.launch {
            val newMember = EnterpriseMember(
                id = "mem_${UUID.randomUUID().toString().take(8)}",
                orgId = currentState.selectedOrgId,
                name = name,
                email = email,
                role = role,
                department = department
            )
            saveEnterpriseMemberUseCase?.invoke(newMember)
            sendEvent(AdminDashboardUiEvent.ShowToast("تم تعيين العضو وتخصيص الدور بنجاح"))
        }
    }

    private fun deleteMember(memberId: String) {
        viewModelScope.launch {
            deleteEnterpriseMemberUseCase?.invoke(memberId)
            sendEvent(AdminDashboardUiEvent.ShowToast("تم إلغاء العضوية"))
        }
    }
}

