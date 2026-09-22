package com.rtiqa.feature.admin.academic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID
import com.rtiqa.core.domain.model.*

@Composable
fun AcademicStructureDialogs(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    val tab = uiState.activeDialogTab ?: return
    
    when (tab) {
        AcademicTab.YEARS -> CreateAcademicYearDialog(uiState, onAction)
        AcademicTab.TERMS -> CreateSemesterDialog(uiState, onAction)
        AcademicTab.GRADE_LEVELS -> CreateGradeLevelDialog(uiState, onAction)
        AcademicTab.DEPARTMENTS -> CreateDepartmentDialog(uiState, onAction)
        AcademicTab.MAJORS -> CreateMajorDialog(uiState, onAction)
        AcademicTab.SUBJECTS -> CreateSubjectDialog(uiState, onAction)
        AcademicTab.SECTIONS -> CreateSectionDialog(uiState, onAction)
        AcademicTab.STUDY_PLANS -> CreateStudyPlanDialog(uiState, onAction)
    }
}

@Composable
private fun AcademicStructureBaseDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun SingleFieldAcademicDialog(
    title: String,
    label: String = "الاسم",
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AcademicStructureBaseDialog(
        title = title,
        onDismiss = onDismiss,
        onConfirm = {
            if (name.isNotBlank()) {
                onSave(name)
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TwoFieldAcademicDialog(
    title: String,
    nameLabel: String = "الاسم",
    codeLabel: String = "الرمز",
    onDismiss: () -> Unit,
    onSave: (name: String, code: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    AcademicStructureBaseDialog(
        title = title,
        onDismiss = onDismiss,
        onConfirm = {
            if (name.isNotBlank()) {
                onSave(name, code)
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(nameLabel) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text(codeLabel) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CreateAcademicYearDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }

    AcademicStructureBaseDialog(
        title = "إضافة عام دراسي",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onConfirm = {
            if (name.isNotBlank()) {
                onAction(AcademicStructureAction.SaveAcademicYear(AcademicYear(
                    id = UUID.randomUUID().toString(),
                    orgId = uiState.currentOrgId,
                    name = name,
                    startDate = start,
                    endDate = end,
                    isCurrent = false
                )))
            }
        }
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم (مثال: 2026-2027)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("تاريخ البداية (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("تاريخ النهاية (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun CreateSemesterDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    SingleFieldAcademicDialog(
        title = "إضافة فصل دراسي",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onSave = { name ->
            uiState.selectedYearId?.let { yearId ->
                onAction(AcademicStructureAction.SaveSemester(Semester(
                    id = UUID.randomUUID().toString(),
                    academicYearId = yearId,
                    name = name,
                    order = 1
                )))
            }
        }
    )
}

@Composable
fun CreateGradeLevelDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    SingleFieldAcademicDialog(
        title = "إضافة مرحلة دراسية",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onSave = { name ->
            onAction(AcademicStructureAction.SaveGradeLevel(GradeLevel(
                id = UUID.randomUUID().toString(),
                schoolId = uiState.currentSchoolId,
                name = name,
                code = "G1",
                levelSequence = 1
            )))
        }
    )
}

@Composable
fun CreateDepartmentDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    TwoFieldAcademicDialog(
        title = "إضافة قسم",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onSave = { name, code ->
            onAction(AcademicStructureAction.SaveDepartment(Department(
                id = UUID.randomUUID().toString(),
                orgId = uiState.currentOrgId,
                name = name,
                code = code,
                headName = ""
            )))
        }
    )
}

@Composable
fun CreateMajorDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    SingleFieldAcademicDialog(
        title = "إضافة تخصص",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onSave = { name ->
            uiState.selectedDepartmentId?.let { deptId ->
                onAction(AcademicStructureAction.SaveMajor(Major(
                    id = UUID.randomUUID().toString(),
                    departmentId = deptId,
                    name = name,
                    code = "M1",
                    degreeType = "BACHELOR"
                )))
            }
        }
    )
}

@Composable
fun CreateSubjectDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    TwoFieldAcademicDialog(
        title = "إضافة مادة",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onSave = { name, code ->
            onAction(AcademicStructureAction.SaveSubject(Subject(
                id = UUID.randomUUID().toString(),
                schoolId = uiState.currentSchoolId,
                majorId = uiState.selectedMajorId ?: "",
                name = name,
                code = code,
                creditHours = 3
            )))
        }
    )
}

@Composable
fun CreateSectionDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    SingleFieldAcademicDialog(
        title = "إضافة شعبة",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onSave = { name ->
            onAction(AcademicStructureAction.SaveSection(Section(
                id = UUID.randomUUID().toString(),
                schoolId = uiState.currentSchoolId,
                majorId = uiState.selectedMajorId ?: "",
                semesterId = uiState.selectedYearId ?: "",
                branchId = "",
                name = name,
                capacity = 30
            )))
        }
    )
}

@Composable
fun CreateStudyPlanDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    SingleFieldAcademicDialog(
        title = "إضافة خطة دراسية",
        onDismiss = { onAction(AcademicStructureAction.CloseDialog) },
        onSave = { name ->
            uiState.selectedMajorId?.let { majorId ->
                onAction(AcademicStructureAction.SaveStudyPlan(StudyPlan(
                    id = UUID.randomUUID().toString(),
                    majorId = majorId,
                    name = name,
                    totalCredits = 120,
                    version = "1.0"
                )))
            }
        }
    )
}
