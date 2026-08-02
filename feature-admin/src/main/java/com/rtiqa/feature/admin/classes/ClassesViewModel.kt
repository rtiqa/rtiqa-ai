package com.rtiqa.feature.admin.classes

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.domain.usecase.DeleteClassUseCase
import com.rtiqa.core.domain.usecase.GetClassesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.ReorderClassesUseCase
import com.rtiqa.core.domain.usecase.SaveClassUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ClassesViewModel(
    private val getClassesForSchoolUseCase: GetClassesForSchoolUseCase,
    private val saveClassUseCase: SaveClassUseCase,
    private val deleteClassUseCase: DeleteClassUseCase,
    private val reorderClassesUseCase: ReorderClassesUseCase,
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val preferencesDataStore: RtiqaPreferencesDataStore
) : BaseViewModel<ClassesUiState, ClassesUiAction, ClassesUiEvent>(ClassesUiState()) {

    init {
        observeActiveSchoolAndClasses()
    }

    private fun observeActiveSchoolAndClasses() {
        val schoolsFlow = getSchoolsUseCase()
        val userPrefsFlow = preferencesDataStore.userPreferencesFlow

        combine(schoolsFlow, userPrefsFlow) { schools, userPrefs ->
            val activeId = userPrefs.activeSchoolId.ifBlank { "school_001" }
            val activeSchool = schools.find { it.id == activeId } ?: schools.firstOrNull()
            schools to (activeSchool ?: schools.firstOrNull())
        }.flatMapLatest { (schools, activeSchool) ->
            val activeId = activeSchool?.id ?: "school_001"
            setState {
                copy(
                    schools = schools,
                    activeSchoolId = activeId,
                    activeSchool = activeSchool
                )
            }
            getClassesForSchoolUseCase(activeId)
        }.onEach { classes ->
            if (classes.isEmpty()) {
                seedInitialClasses(currentState.activeSchoolId)
            } else {
                setState {
                    copy(
                        rawClasses = classes,
                        isLoading = false
                    )
                }
                applyFilterAndSearch()
            }
        }.launchIn(viewModelScope)
    }

    private fun seedInitialClasses(schoolId: String) {
        viewModelScope.launch {
            val defaults = listOf(
                SchoolClass(
                    id = "cls_${schoolId}_1",
                    schoolId = schoolId,
                    name = "الصف الأول الابتدائي - أ",
                    gradeLevel = "الابتدائي",
                    sectionName = "أ",
                    capacity = 30,
                    studentsCount = 25,
                    roomNumber = "101",
                    displayOrder = 1
                ),
                SchoolClass(
                    id = "cls_${schoolId}_2",
                    schoolId = schoolId,
                    name = "الصف الثاني الابتدائي - ب",
                    gradeLevel = "الابتدائي",
                    sectionName = "ب",
                    capacity = 28,
                    studentsCount = 22,
                    roomNumber = "102",
                    displayOrder = 2
                ),
                SchoolClass(
                    id = "cls_${schoolId}_3",
                    schoolId = schoolId,
                    name = "الصف الثالث المتوسط - أ",
                    gradeLevel = "المتوسط",
                    sectionName = "أ",
                    capacity = 35,
                    studentsCount = 30,
                    roomNumber = "201",
                    displayOrder = 3
                )
            )
            defaults.forEach { saveClassUseCase(it) }
        }
    }

    override fun onAction(action: ClassesUiAction) {
        when (action) {
            is ClassesUiAction.SelectActiveSchool -> {
                viewModelScope.launch {
                    preferencesDataStore.setActiveSchoolId(action.schoolId)
                }
            }
            is ClassesUiAction.SearchClasses -> {
                setState { copy(searchQuery = action.query) }
                applyFilterAndSearch()
            }
            is ClassesUiAction.FilterByGrade -> {
                setState { copy(selectedGradeFilter = action.gradeLevel) }
                applyFilterAndSearch()
            }
            ClassesUiAction.OpenAddClass -> {
                setState {
                    copy(
                        isAddEditOpen = true,
                        selectedClassForEdit = null,
                        errorMessage = null
                    )
                }
            }
            is ClassesUiAction.OpenEditClass -> {
                setState {
                    copy(
                        isAddEditOpen = true,
                        selectedClassForEdit = action.schoolClass,
                        errorMessage = null
                    )
                }
            }
            is ClassesUiAction.OpenClassDetails -> {
                setState {
                    copy(
                        isDetailsOpen = true,
                        selectedClassForDetails = action.schoolClass
                    )
                }
            }
            ClassesUiAction.CloseDialogs -> {
                setState {
                    copy(
                        isAddEditOpen = false,
                        isDetailsOpen = false,
                        selectedClassForEdit = null,
                        selectedClassForDetails = null,
                        errorMessage = null
                    )
                }
            }
            is ClassesUiAction.SaveClass -> {
                saveClass(action)
            }
            is ClassesUiAction.DeleteClass -> {
                deleteClass(action.id)
            }
            is ClassesUiAction.MoveClassUp -> {
                moveClass(action.schoolClass, isUp = true)
            }
            is ClassesUiAction.MoveClassDown -> {
                moveClass(action.schoolClass, isUp = false)
            }
        }
    }

    private fun applyFilterAndSearch() {
        val query = currentState.searchQuery.trim().lowercase()
        val gradeFilter = currentState.selectedGradeFilter

        val filtered = currentState.rawClasses.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.name.lowercase().contains(query) ||
                    item.gradeLevel.lowercase().contains(query) ||
                    item.roomNumber.lowercase().contains(query) ||
                    item.sectionName.lowercase().contains(query)

            val matchesGrade = gradeFilter.isNullOrBlank() || item.gradeLevel == gradeFilter
            matchesQuery && matchesGrade
        }.sortedBy { it.displayOrder }

        setState { copy(filteredClasses = filtered) }
    }

    private fun saveClass(action: ClassesUiAction.SaveClass) {
        viewModelScope.launch {
            val existingId = action.id ?: "cls_${UUID.randomUUID()}"
            val existingClass = currentState.rawClasses.find { it.id == existingId }
            val nextOrder = existingClass?.displayOrder ?: ((currentState.rawClasses.maxOfOrNull { it.displayOrder } ?: 0) + 1)

            val newClass = SchoolClass(
                id = existingId,
                schoolId = currentState.activeSchoolId,
                name = action.name.trim(),
                gradeLevel = action.gradeLevel.trim(),
                sectionName = action.sectionName.trim(),
                capacity = action.capacity,
                studentsCount = existingClass?.studentsCount ?: 0,
                roomNumber = action.roomNumber.trim(),
                displayOrder = nextOrder
            )

            val result = saveClassUseCase(newClass)
            result.onSuccess {
                setState { copy(isAddEditOpen = false, errorMessage = null) }
                sendEvent(ClassesUiEvent.ShowToast("تم حفظ الصف بنجاح"))
            }.onFailure { ex ->
                val errorText = ex.message ?: "فشل حفظ الصف"
                setState { copy(errorMessage = errorText) }
                sendEvent(ClassesUiEvent.ShowToast(errorText))
            }
        }
    }

    private fun deleteClass(id: String) {
        viewModelScope.launch {
            deleteClassUseCase(id)
            sendEvent(ClassesUiEvent.ShowToast("تم حذف الصف بنجاح"))
        }
    }

    private fun moveClass(schoolClass: SchoolClass, isUp: Boolean) {
        viewModelScope.launch {
            val list = currentState.rawClasses.sortedBy { it.displayOrder }.toMutableList()
            val index = list.indexOfFirst { it.id == schoolClass.id }
            if (index == -1) return@launch

            val targetIndex = if (isUp) index - 1 else index + 1
            if (targetIndex in 0 until list.size) {
                val temp = list[index]
                list[index] = list[targetIndex]
                list[targetIndex] = temp
                reorderClassesUseCase(list)
            }
        }
    }
}
