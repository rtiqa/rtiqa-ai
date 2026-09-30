package com.rtiqa.feature.admin.teacher.gradebook

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.GetClassGradebookUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AcademicGradebookViewModel(
    private val getClassGradebookUseCase: GetClassGradebookUseCase? = null,
    initialClassId: String? = null
) : BaseViewModel<AcademicGradebookUiState, AcademicGradebookUiAction, AcademicGradebookUiEvent>(
    AcademicGradebookUiState(
        selectedClassId = initialClassId ?: "",
        classAverage = 0.0,
        highestScore = 0.0,
        lowestScore = 0.0,
        passRatePercentage = 0
    )
) {

    init {
        initialClassId?.takeIf { it.isNotBlank() }?.let { loadGradebookData(it) }
    }

    override fun onAction(action: AcademicGradebookUiAction) {
        when (action) {
            is AcademicGradebookUiAction.SelectClass -> {
                if (action.classId.isNotBlank()) {
                    setState { copy(selectedClassId = action.classId) }
                    loadGradebookData(action.classId)
                } else {
                    setState {
                        copy(
                            selectedClassId = "",
                            students = emptyList(),
                            columns = emptyList(),
                            classAverage = 0.0,
                            highestScore = 0.0,
                            lowestScore = 0.0,
                            passRatePercentage = 0,
                            errorMessage = null
                        )
                    }
                }
            }
            is AcademicGradebookUiAction.SelectTerm -> {
                setState { copy(selectedTerm = action.term) }
            }
            is AcademicGradebookUiAction.SearchQueryChanged -> {
                setState { copy(searchQuery = action.query) }
            }
            is AcademicGradebookUiAction.FilterStatus -> {
                setState { copy(statusFilter = action.status) }
            }
            is AcademicGradebookUiAction.SelectViewMode -> {
                setState { copy(selectedViewMode = action.modeIndex) }
            }
            is AcademicGradebookUiAction.Refresh -> {
                val currentClassId = currentState.selectedClassId.takeIf { it.isNotBlank() }
                if (currentClassId != null) {
                    loadGradebookData(currentClassId)
                }
            }
            is AcademicGradebookUiAction.OpenScoreEditor -> {
                val currentScore = action.student.scores[action.column.id]?.score?.toString() ?: ""
                setState {
                    copy(
                        editingStudentScore = action.student to action.column,
                        currentEditScoreValue = currentScore
                    )
                }
            }
            is AcademicGradebookUiAction.UpdateScoreValue -> {
                setState { copy(currentEditScoreValue = action.value) }
            }
            is AcademicGradebookUiAction.SaveScoreEditor -> {
                val editPair = currentState.editingStudentScore ?: return
                val student = editPair.first
                val column = editPair.second
                val newScoreVal = currentState.currentEditScoreValue.toDoubleOrNull()

                if (newScoreVal != null && (newScoreVal < 0 || newScoreVal > column.maxScore)) {
                    sendEvent(AcademicGradebookUiEvent.ShowToast("الدرجة يجب أن تكون بين 0 و ${column.maxScore}"))
                    return
                }

                // Update score in students list
                val updatedStudents = currentState.students.map { s ->
                    if (s.studentId == student.studentId) {
                        val newScores = s.scores.toMutableMap()
                        newScores[column.id] = StudentAssessmentScore(
                            columnId = column.id,
                            score = newScoreVal,
                            isExcused = false
                        )
                        // Recompute total
                        val total = newScores.values.mapNotNull { it.score }.sum()
                        s.copy(
                            scores = newScores,
                            totalScore = total,
                            percentage = (total / s.maxPossibleScore) * 100.0
                        )
                    } else {
                        s
                    }
                }

                // Recompute class stats
                val averages = computeStats(updatedStudents)

                setState {
                    copy(
                        students = updatedStudents,
                        editingStudentScore = null,
                        currentEditScoreValue = "",
                        classAverage = averages.first,
                        highestScore = averages.second,
                        lowestScore = averages.third
                    )
                }
                sendEvent(AcademicGradebookUiEvent.ShowToast("تم حفظ درجة الطالب ${student.studentName} بنجاح"))
            }
            is AcademicGradebookUiAction.DismissScoreEditor -> {
                setState { copy(editingStudentScore = null, currentEditScoreValue = "") }
            }
            is AcademicGradebookUiAction.OpenAddAssessmentDialog -> {
                setState { copy(showAddAssessmentDialog = true) }
            }
            is AcademicGradebookUiAction.DismissAddAssessmentDialog -> {
                setState { copy(showAddAssessmentDialog = false) }
            }
            is AcademicGradebookUiAction.SaveNewAssessment -> {
                val newCol = AssessmentColumn(
                    id = "col-${System.currentTimeMillis()}",
                    title = action.title,
                    maxScore = action.maxScore,
                    weightPercentage = action.weight,
                    category = action.category
                )
                val updatedCols = currentState.columns + newCol
                setState {
                    copy(
                        columns = updatedCols,
                        showAddAssessmentDialog = false
                    )
                }
                sendEvent(AcademicGradebookUiEvent.ShowToast("تمت إضافة التقييم الجديد: ${action.title}"))
            }
            is AcademicGradebookUiAction.OpenExportDialog -> {
                setState { copy(showExportDialog = true) }
            }
            is AcademicGradebookUiAction.DismissExportDialog -> {
                setState { copy(showExportDialog = false) }
            }
            is AcademicGradebookUiAction.ExportGradebook -> {
                setState { copy(showExportDialog = false) }
                sendEvent(AcademicGradebookUiEvent.ShowToast("جاري تصدير كشف الدرجات بصيغة ${action.format} وتم حفظه بنجاح"))
            }
        }
    }

    private fun loadGradebookData(classId: String) {
        if (classId.isBlank()) {
            setState {
                copy(
                    isLoading = false,
                    selectedClassId = "",
                    students = emptyList(),
                    columns = emptyList(),
                    classAverage = 0.0,
                    highestScore = 0.0,
                    lowestScore = 0.0,
                    passRatePercentage = 0,
                    errorMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            if (getClassGradebookUseCase == null) {
                setState {
                    copy(
                        isLoading = false,
                        selectedClassId = classId,
                        students = emptyList(),
                        columns = emptyList(),
                        classAverage = 0.0,
                        highestScore = 0.0,
                        lowestScore = 0.0,
                        passRatePercentage = 0
                    )
                }
                return@launch
            }

            try {
                when (val result = getClassGradebookUseCase(classId)) {
                    is RtiqaResult.Success -> {
                        val gradebook = result.data
                        val mappedColumns = gradebook.assessments.map { assessment ->
                            val weightPct = assessment.weight?.let { w ->
                                if (w <= 1.0) (w * 100).toInt() else w.toInt()
                            } ?: 0
                            AssessmentColumn(
                                id = assessment.assessmentId,
                                title = assessment.title,
                                maxScore = assessment.maxScore,
                                weightPercentage = weightPct,
                                category = ""
                            )
                        }

                        val scoresByStudent = gradebook.scores.groupBy { it.studentId }
                        val maxPossible = mappedColumns.sumOf { it.maxScore }.takeIf { it > 0 } ?: 100.0

                        val mappedStudents = gradebook.students.map { student ->
                            val studentScores = scoresByStudent[student.studentId] ?: emptyList()
                            val scoreMap = mappedColumns.associate { col ->
                                val scoreObj = studentScores.find { it.assessmentId == col.id }
                                col.id to StudentAssessmentScore(
                                    columnId = col.id,
                                    score = scoreObj?.score,
                                    isExcused = false,
                                    note = null
                                )
                            }
                            val totalScore = scoreMap.values.mapNotNull { it.score }.sum()

                            StudentGradeRow(
                                studentId = student.studentId,
                                studentName = student.displayName,
                                studentNumber = student.studentNumber ?: "",
                                avatarInitial = student.displayName.take(1),
                                scores = scoreMap,
                                totalScore = totalScore,
                                maxPossibleScore = maxPossible,
                                percentage = if (maxPossible > 0) (totalScore / maxPossible) * 100.0 else 0.0
                            )
                        }

                        val stats = computeStats(mappedStudents)
                        val passRate = if (mappedStudents.isEmpty()) 0 else {
                            val passing = mappedStudents.count { it.percentage >= 60.0 }
                            ((passing.toDouble() / mappedStudents.size) * 100.0).toInt()
                        }

                        setState {
                            copy(
                                isLoading = false,
                                selectedClassId = classId,
                                columns = mappedColumns,
                                students = mappedStudents,
                                classAverage = stats.first,
                                highestScore = stats.second,
                                lowestScore = stats.third,
                                passRatePercentage = passRate,
                                errorMessage = null
                            )
                        }
                    }
                    is RtiqaResult.Error -> {
                        setState {
                            copy(
                                isLoading = false,
                                selectedClassId = classId,
                                students = emptyList(),
                                columns = emptyList(),
                                classAverage = 0.0,
                                highestScore = 0.0,
                                lowestScore = 0.0,
                                passRatePercentage = 0,
                                errorMessage = result.error.message
                            )
                        }
                        sendEvent(AcademicGradebookUiEvent.ShowToast(result.error.message))
                    }
                    is RtiqaResult.Loading -> {
                        // Already in loading state
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMsg = e.message ?: "An unexpected error occurred"
                setState {
                    copy(
                        isLoading = false,
                        students = emptyList(),
                        columns = emptyList(),
                        errorMessage = errorMsg
                    )
                }
                sendEvent(AcademicGradebookUiEvent.ShowToast(errorMsg))
            }
        }
    }

    private fun computeStats(studentList: List<StudentGradeRow>): Triple<Double, Double, Double> {
        if (studentList.isEmpty()) return Triple(0.0, 0.0, 0.0)
        val totals = studentList.map { it.totalScore }
        val avg = totals.average()
        val high = totals.maxOrNull() ?: 0.0
        val low = totals.minOrNull() ?: 0.0
        return Triple(
            Math.round(avg * 10.0) / 10.0,
            Math.round(high * 10.0) / 10.0,
            Math.round(low * 10.0) / 10.0
        )
    }
}
