package com.rtiqa.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class AdminViewModelFactory(
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
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminDashboardViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getCoursesUseCase = getCoursesUseCase,
            saveCourseUseCase = saveCourseUseCase,
            deleteCourseUseCase = deleteCourseUseCase,
            updateUserProfileUseCase = updateUserProfileUseCase,
            getOrganizationsUseCase = getOrganizationsUseCase,
            saveOrganizationUseCase = saveOrganizationUseCase,
            deleteOrganizationUseCase = deleteOrganizationUseCase,
            getBranchesUseCase = getBranchesUseCase,
            saveBranchUseCase = saveBranchUseCase,
            getEnterpriseMembersUseCase = getEnterpriseMembersUseCase,
            saveEnterpriseMemberUseCase = saveEnterpriseMemberUseCase,
            deleteEnterpriseMemberUseCase = deleteEnterpriseMemberUseCase,
            getAcademicYearsUseCase = getAcademicYearsUseCase,
            saveAcademicYearUseCase = saveAcademicYearUseCase,
            getDepartmentsUseCase = getDepartmentsUseCase,
            saveDepartmentUseCase = saveDepartmentUseCase
        ) as T
    }
}
