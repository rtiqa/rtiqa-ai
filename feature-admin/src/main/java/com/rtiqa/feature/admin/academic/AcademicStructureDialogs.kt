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
fun CreateAcademicYearDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة عام دراسي") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم (مثال: 2026-2027)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("تاريخ البداية (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("تاريخ النهاية (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
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
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}

@Composable
fun CreateSemesterDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة فصل دراسي") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                uiState.selectedYearId?.let { yearId ->
                    if (name.isNotBlank()) {
                        onAction(AcademicStructureAction.SaveSemester(Semester(
                            id = UUID.randomUUID().toString(),
                            academicYearId = yearId,
                            name = name,
                            order = 1
                        )))
                    }
                }
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}

@Composable
fun CreateGradeLevelDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة مرحلة دراسية") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onAction(AcademicStructureAction.SaveGradeLevel(GradeLevel(
                        id = UUID.randomUUID().toString(),
                        schoolId = uiState.currentSchoolId,
                        name = name,
                        code = "G1", levelSequence = 1
                    )))
                }
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}

@Composable
fun CreateDepartmentDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة قسم") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("الرمز") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onAction(AcademicStructureAction.SaveDepartment(Department(
                        id = UUID.randomUUID().toString(),
                        orgId = uiState.currentOrgId,
                        name = name,
                        code = code,
                        headName = ""
                    )))
                }
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}

@Composable
fun CreateMajorDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة تخصص") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                uiState.selectedDepartmentId?.let { deptId ->
                    if (name.isNotBlank()) {
                        onAction(AcademicStructureAction.SaveMajor(Major(
                            id = UUID.randomUUID().toString(),
                            departmentId = deptId,
                            name = name,
                            code = "M1",
                            degreeType = "BACHELOR"
                        )))
                    }
                }
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}

@Composable
fun CreateSubjectDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة مادة") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("الرمز") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onAction(AcademicStructureAction.SaveSubject(Subject(
                        id = UUID.randomUUID().toString(),
                        schoolId = uiState.currentSchoolId,
                        majorId = uiState.selectedMajorId ?: "",
                        name = name,
                        code = code,
                        creditHours = 3
                    )))
                }
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}

@Composable
fun CreateSectionDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة شعبة") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
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
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}

@Composable
fun CreateStudyPlanDialog(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onAction(AcademicStructureAction.CloseDialog) },
        title = { Text("إضافة خطة دراسية") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                uiState.selectedMajorId?.let { majorId ->
                    if (name.isNotBlank()) {
                        onAction(AcademicStructureAction.SaveStudyPlan(StudyPlan(
                            id = UUID.randomUUID().toString(),
                            majorId = majorId,
                            name = name,
                            totalCredits = 120,
                            version = "1.0"
                        )))
                    }
                }
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = { onAction(AcademicStructureAction.CloseDialog) }) { Text("إلغاء") }
        }
    )
}
