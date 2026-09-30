package com.rtiqa.feature.admin.teacher.gradebook

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AcademicGradebookViewModel : BaseViewModel<AcademicGradebookUiState, AcademicGradebookUiAction, AcademicGradebookUiEvent>(
    AcademicGradebookUiState(
        classAverage = 0.0,
        highestScore = 0.0,
        lowestScore = 0.0,
        passRatePercentage = 0
    )
) {

    init {
        loadGradebookData()
    }

    override fun onAction(action: AcademicGradebookUiAction) {
        when (action) {
            is AcademicGradebookUiAction.SelectClass -> {
                setState { copy(selectedClassId = action.classId) }
                loadGradebookData(action.classId)
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
                loadGradebookData(currentState.selectedClassId)
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

    private fun loadGradebookData(classId: String = "cls-101") {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            delay(300) // Simulated quick load for prototype

            val classes = listOf(
                GradebookClassOption("cls-101", "الصف الثالث ثانوي (أ)", "الذكاء الاصطناعي وهياكل البيانات", "الفصل الدراسي الثاني"),
                GradebookClassOption("cls-102", "الصف الثالث ثانوي (ب)", "الذكاء الاصطناعي وهياكل البيانات", "الفصل الدراسي الثاني"),
                GradebookClassOption("cls-201", "الصف الثاني ثانوي (ج)", "مقدمة البرمجة وتقنيات الويب", "الفصل الدراسي الثاني"),
                GradebookClassOption("cls-301", "الصف الأول ثانوي (أ)", "المعلوماتية والمهارات الرقمية", "الفصل الدراسي الثاني")
            )

            val columns = listOf(
                AssessmentColumn("col-hw1", "الواجب 1", 10.0, 10, "واجبات"),
                AssessmentColumn("col-hw2", "الواجب 2", 10.0, 10, "واجبات"),
                AssessmentColumn("col-quiz1", "اختبار قصير 1", 15.0, 15, "اختبارات قصيرة"),
                AssessmentColumn("col-midterm", "الاختبار النصفي", 25.0, 25, "اختبارات فصلية"),
                AssessmentColumn("col-project", "مشروع الذكاء الاصطناعي", 20.0, 20, "مشاريع عملية"),
                AssessmentColumn("col-final", "المشاركة والحضور", 20.0, 20, "أداء صفي")
            )

            val initialStudents = emptyList<StudentGradeRow>()
            val stats = computeStats(initialStudents)

            setState {
                copy(
                    isLoading = false,
                    availableClasses = classes,
                    selectedClassId = classId,
                    columns = columns,
                    students = initialStudents,
                    classAverage = stats.first,
                    highestScore = stats.second,
                    lowestScore = stats.third,
                    passRatePercentage = 0
                )
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
