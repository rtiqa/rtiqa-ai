package com.rtiqa.feature.admin.classes

import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState

data class ClassesUiState(
    val rawClasses: List<SchoolClass> = emptyList(),
    val filteredClasses: List<SchoolClass> = emptyList(),
    val schools: List<School> = emptyList(),
    val activeSchoolId: String = "school_001",
    val activeSchool: School? = null,
    val searchQuery: String = "",
    val selectedGradeFilter: String? = null,
    val selectedClassForDetails: SchoolClass? = null,
    val selectedClassForEdit: SchoolClass? = null,
    val isAddEditOpen: Boolean = false,
    val isDetailsOpen: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : ViewUiState

sealed interface ClassesUiAction : ViewUiAction {
    data class SelectActiveSchool(val schoolId: String) : ClassesUiAction
    data class SearchClasses(val query: String) : ClassesUiAction
    data class FilterByGrade(val gradeLevel: String?) : ClassesUiAction
    object OpenAddClass : ClassesUiAction
    data class OpenEditClass(val schoolClass: SchoolClass) : ClassesUiAction
    data class OpenClassDetails(val schoolClass: SchoolClass) : ClassesUiAction
    object CloseDialogs : ClassesUiAction
    data class SaveClass(
        val id: String?,
        val name: String,
        val gradeLevel: String,
        val sectionName: String,
        val capacity: Int,
        val roomNumber: String
    ) : ClassesUiAction
    data class DeleteClass(val id: String) : ClassesUiAction
    data class MoveClassUp(val schoolClass: SchoolClass) : ClassesUiAction
    data class MoveClassDown(val schoolClass: SchoolClass) : ClassesUiAction
}

sealed interface ClassesUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : ClassesUiEvent
}
