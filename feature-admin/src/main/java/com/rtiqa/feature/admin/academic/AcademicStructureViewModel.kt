package com.rtiqa.feature.admin.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AcademicStructureViewModel(
    private val getAcademicYearsUseCase: GetAcademicYearsUseCase,
    private val saveAcademicYearUseCase: SaveAcademicYearUseCase,
    private val deleteAcademicYearUseCase: DeleteAcademicYearUseCase,
    private val getSemestersUseCase: GetSemestersUseCase,
    private val saveSemesterUseCase: SaveSemesterUseCase,
    private val deleteSemesterUseCase: DeleteSemesterUseCase,
    private val getGradeLevelsForSchoolUseCase: GetGradeLevelsForSchoolUseCase,
    private val saveGradeLevelUseCase: SaveGradeLevelUseCase,
    private val deleteGradeLevelUseCase: DeleteGradeLevelUseCase,
    private val getDepartmentsUseCase: GetDepartmentsUseCase,
    private val saveDepartmentUseCase: SaveDepartmentUseCase,
    private val deleteDepartmentUseCase: DeleteDepartmentUseCase,
    private val getMajorsUseCase: GetMajorsUseCase,
    private val saveMajorUseCase: SaveMajorUseCase,
    private val deleteMajorUseCase: DeleteMajorUseCase,
    private val getSubjectsForSchoolUseCase: GetSubjectsForSchoolUseCase,
    private val saveSubjectUseCase: SaveSubjectUseCase,
    private val deleteSubjectUseCase: DeleteSubjectUseCase,
    private val getSectionsForSchoolUseCase: GetSectionsForSchoolUseCase,
    private val saveSectionUseCase: SaveSectionUseCase,
    private val deleteSectionUseCase: DeleteSectionUseCase,
    private val getStudyPlansUseCase: GetStudyPlansUseCase,
    private val saveStudyPlanUseCase: SaveStudyPlanUseCase,
    private val deleteStudyPlanUseCase: DeleteStudyPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicStructureUiState())
    val uiState: StateFlow<AcademicStructureUiState> = _uiState.asStateFlow()

    init {
        loadRootData()
    }

    private fun loadRootData() {
        val orgId = _uiState.value.currentOrgId
        val schoolId = _uiState.value.currentSchoolId

        getAcademicYearsUseCase(orgId).onEach { years ->
            _uiState.update { it.copy(academicYears = years) }
            if (years.isNotEmpty() && _uiState.value.selectedYearId == null) {
                onAction(AcademicStructureAction.SelectAcademicYear(years.first().id))
            }
        }.launchIn(viewModelScope)

        getGradeLevelsForSchoolUseCase(schoolId).onEach { levels ->
            _uiState.update { it.copy(gradeLevels = levels) }
        }.launchIn(viewModelScope)

        getDepartmentsUseCase(orgId).onEach { depts ->
            _uiState.update { it.copy(departments = depts) }
            if (depts.isNotEmpty() && _uiState.value.selectedDepartmentId == null) {
                onAction(AcademicStructureAction.SelectDepartment(depts.first().id))
            }
        }.launchIn(viewModelScope)

        getSubjectsForSchoolUseCase(schoolId).onEach { subs ->
            _uiState.update { it.copy(subjects = subs) }
        }.launchIn(viewModelScope)

        getSectionsForSchoolUseCase(schoolId).onEach { secs ->
            _uiState.update { it.copy(sections = secs) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: AcademicStructureAction) {
        when (action) {
            is AcademicStructureAction.SelectTab -> {
                _uiState.update { it.copy(currentTab = action.tab) }
            }
            is AcademicStructureAction.SelectAcademicYear -> {
                _uiState.update { it.copy(selectedYearId = action.id) }
                action.id?.let { loadSemesters(it) } ?: _uiState.update { it.copy(semesters = emptyList()) }
            }
            is AcademicStructureAction.SelectDepartment -> {
                _uiState.update { it.copy(selectedDepartmentId = action.id) }
                action.id?.let { loadMajors(it) } ?: _uiState.update { it.copy(majors = emptyList()) }
            }
            is AcademicStructureAction.SelectMajor -> {
                _uiState.update { it.copy(selectedMajorId = action.id) }
                action.id?.let { loadStudyPlans(it) } ?: _uiState.update { it.copy(studyPlans = emptyList()) }
            }
            is AcademicStructureAction.ClearError -> _uiState.update { it.copy(error = null) }
            is AcademicStructureAction.OpenDialog -> _uiState.update { it.copy(isDialogOpen = true, activeDialogTab = action.tab) }
            is AcademicStructureAction.CloseDialog -> _uiState.update { it.copy(isDialogOpen = false, activeDialogTab = null) }
            
            is AcademicStructureAction.SaveAcademicYear -> viewModelScope.launch { saveAcademicYearUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteAcademicYear -> viewModelScope.launch { deleteAcademicYearUseCase(action.id) }
            
            is AcademicStructureAction.SaveSemester -> viewModelScope.launch { saveSemesterUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteSemester -> viewModelScope.launch { deleteSemesterUseCase(action.id) }
            
            is AcademicStructureAction.SaveGradeLevel -> viewModelScope.launch { saveGradeLevelUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteGradeLevel -> viewModelScope.launch { deleteGradeLevelUseCase(action.id) }
            
            is AcademicStructureAction.SaveDepartment -> viewModelScope.launch { saveDepartmentUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteDepartment -> viewModelScope.launch { deleteDepartmentUseCase(action.id) }
            
            is AcademicStructureAction.SaveMajor -> viewModelScope.launch { saveMajorUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteMajor -> viewModelScope.launch { deleteMajorUseCase(action.id) }
            
            is AcademicStructureAction.SaveSubject -> viewModelScope.launch { saveSubjectUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteSubject -> viewModelScope.launch { deleteSubjectUseCase(action.id) }
            
            is AcademicStructureAction.SaveSection -> viewModelScope.launch { saveSectionUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteSection -> viewModelScope.launch { deleteSectionUseCase(action.id) }
            
            is AcademicStructureAction.SaveStudyPlan -> viewModelScope.launch { saveStudyPlanUseCase(action.item); onAction(AcademicStructureAction.CloseDialog) }
            is AcademicStructureAction.DeleteStudyPlan -> viewModelScope.launch { deleteStudyPlanUseCase(action.id) }
        }
    }

    private var semestersJob: kotlinx.coroutines.Job? = null
    private fun loadSemesters(yearId: String) {
        semestersJob?.cancel()
        semestersJob = getSemestersUseCase(yearId).onEach { items ->
            _uiState.update { it.copy(semesters = items) }
        }.launchIn(viewModelScope)
    }

    private var majorsJob: kotlinx.coroutines.Job? = null
    private fun loadMajors(departmentId: String) {
        majorsJob?.cancel()
        majorsJob = getMajorsUseCase(departmentId).onEach { items ->
            _uiState.update { it.copy(majors = items) }
            if (items.isNotEmpty() && _uiState.value.selectedMajorId == null) {
                onAction(AcademicStructureAction.SelectMajor(items.first().id))
            } else if (items.isEmpty()) {
                onAction(AcademicStructureAction.SelectMajor(null))
            }
        }.launchIn(viewModelScope)
    }
    
    private var studyPlansJob: kotlinx.coroutines.Job? = null
    private fun loadStudyPlans(majorId: String) {
        studyPlansJob?.cancel()
        studyPlansJob = getStudyPlansUseCase(majorId).onEach { items ->
            _uiState.update { it.copy(studyPlans = items) }
        }.launchIn(viewModelScope)
    }
}
