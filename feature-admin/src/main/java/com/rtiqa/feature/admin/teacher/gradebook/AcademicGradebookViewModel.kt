package com.rtiqa.feature.admin.teacher.gradebook

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AcademicGradebookViewModel : BaseViewModel<AcademicGradebookUiState, AcademicGradebookUiAction, AcademicGradebookUiEvent>(
    AcademicGradebookUiState()
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

            val initialStudents = generateMockStudents(columns)
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
                    passRatePercentage = 94
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

    private fun generateMockStudents(cols: List<AssessmentColumn>): List<StudentGradeRow> {
        val names = listOf(
            "أحمد عبد الرحمن الزهراني" to "4410101",
            "سلطان بن فهد الدوسري" to "4410102",
            "عمر إبراهيم القحطاني" to "4410103",
            "محمد عبد العزيز الخالدي" to "4410104",
            "فيصل منصور الغامدي" to "4410105",
            "خالد يوسف العتيبي" to "4410106",
            "عبد الله سعود المالكي" to "4410107",
            "تركي راشد الشمري" to "4410108",
            "ياسر ناصر السبيعي" to "4410109",
            "مشاري بدر المطيري" to "4410110",
            "سعد وليد القرني" to "4410111",
            "فهد حسن الحارثي" to "4410112"
        )

        val sampleScores = listOf(
            mapOf("col-hw1" to 10.0, "col-hw2" to 9.5, "col-quiz1" to 14.0, "col-midterm" to 24.5, "col-project" to 20.0, "col-final" to 19.0),
            mapOf("col-hw1" to 9.0, "col-hw2" to 9.0, "col-quiz1" to 13.0, "col-midterm" to 23.0, "col-project" to 19.0, "col-final" to 18.0),
            mapOf("col-hw1" to 10.0, "col-hw2" to 10.0, "col-quiz1" to 15.0, "col-midterm" to 25.0, "col-project" to 20.0, "col-final" to 20.0),
            mapOf("col-hw1" to 8.0, "col-hw2" to 8.5, "col-quiz1" to 12.0, "col-midterm" to 20.0, "col-project" to 17.5, "col-final" to 17.0),
            mapOf("col-hw1" to 7.0, "col-hw2" to 6.5, "col-quiz1" to 9.0, "col-midterm" to 15.0, "col-project" to 14.0, "col-final" to 15.0),
            mapOf("col-hw1" to 9.5, "col-hw2" to 10.0, "col-quiz1" to 14.5, "col-midterm" to 24.0, "col-project" to 19.5, "col-final" to 19.5),
            mapOf("col-hw1" to 6.0, "col-hw2" to 7.0, "col-quiz1" to 8.5, "col-midterm" to 14.0, "col-project" to 13.0, "col-final" to 14.0),
            mapOf("col-hw1" to 10.0, "col-hw2" to 10.0, "col-quiz1" to 15.0, "col-midterm" to 24.0, "col-project" to 19.0, "col-final" to 19.0),
            mapOf("col-hw1" to 5.0, "col-hw2" to 5.5, "col-quiz1" to 7.0, "col-midterm" to 12.0, "col-project" to 11.0, "col-final" to 12.0),
            mapOf("col-hw1" to 8.5, "col-hw2" to 9.0, "col-quiz1" to 13.0, "col-midterm" to 22.0, "col-project" to 18.0, "col-final" to 18.0),
            mapOf("col-hw1" to 9.0, "col-hw2" to 8.0, "col-quiz1" to 12.5, "col-midterm" to 21.0, "col-project" to 17.0, "col-final" to 17.5),
            mapOf("col-hw1" to 10.0, "col-hw2" to 9.5, "col-quiz1" to 14.0, "col-midterm" to 23.5, "col-project" to 18.5, "col-final" to 19.0)
        )

        return names.mapIndexed { index, (name, number) ->
            val scoreMap = sampleScores[index % sampleScores.size]
            val mappedScores = cols.associate { col ->
                col.id to StudentAssessmentScore(
                    columnId = col.id,
                    score = scoreMap[col.id] ?: (col.maxScore * 0.8)
                )
            }
            val total = mappedScores.values.mapNotNull { it.score }.sum()

            StudentGradeRow(
                studentId = "std-${1000 + index}",
                studentName = name,
                studentNumber = number,
                scores = mappedScores,
                totalScore = total,
                maxPossibleScore = 100.0,
                percentage = total,
                attendanceRate = if (total > 80) 0.96f else if (total > 65) 0.90f else 0.82f
            )
        }
    }
}
