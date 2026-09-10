package com.rtiqa.feature.admin.academic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AcademicYearsList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.academicYears.isEmpty()) {
        EmptyState("لا توجد أعوام دراسية")
    } else {
        LazyColumn {
            items(uiState.academicYears) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("${item.startDate} - ${item.endDate}") },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { onAction(AcademicStructureAction.DeleteAcademicYear(item.id)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    },
                    modifier = Modifier.clickable { onAction(AcademicStructureAction.SelectAcademicYear(item.id)) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SemestersList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.selectedYearId == null) {
        EmptyState("الرجاء تحديد عام دراسي أولاً")
        return
    }
    if (uiState.semesters.isEmpty()) {
        EmptyState("لا توجد فصول دراسية")
    } else {
        LazyColumn {
            items(uiState.semesters) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("الترتيب: ${item.order}") },
                    trailingContent = {
                        IconButton(onClick = { onAction(AcademicStructureAction.DeleteSemester(item.id)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun GradeLevelsList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.gradeLevels.isEmpty()) {
        EmptyState("لا توجد مراحل دراسية")
    } else {
        LazyColumn {
            items(uiState.gradeLevels) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text(item.stage.labelAr) },
                    trailingContent = {
                        IconButton(onClick = { onAction(AcademicStructureAction.DeleteGradeLevel(item.id)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun DepartmentsList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.departments.isEmpty()) {
        EmptyState("لا توجد أقسام")
    } else {
        LazyColumn {
            items(uiState.departments) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("الرمز: ${item.code}") },
                    trailingContent = {
                        IconButton(onClick = { onAction(AcademicStructureAction.DeleteDepartment(item.id)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    modifier = Modifier.clickable { onAction(AcademicStructureAction.SelectDepartment(item.id)) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun MajorsList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.selectedDepartmentId == null) {
        EmptyState("الرجاء تحديد قسم أولاً")
        return
    }
    if (uiState.majors.isEmpty()) {
        EmptyState("لا توجد تخصصات")
    } else {
        LazyColumn {
            items(uiState.majors) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text(item.degreeType) },
                    trailingContent = {
                        IconButton(onClick = { onAction(AcademicStructureAction.DeleteMajor(item.id)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    modifier = Modifier.clickable { onAction(AcademicStructureAction.SelectMajor(item.id)) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SubjectsList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.subjects.isEmpty()) {
        EmptyState("لا توجد مواد دراسية")
    } else {
        LazyColumn {
            items(uiState.subjects) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("${item.code} - الساعات: ${item.creditHours}") },
                    trailingContent = {
                        IconButton(onClick = { onAction(AcademicStructureAction.DeleteSubject(item.id)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SectionsList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.sections.isEmpty()) {
        EmptyState("لا توجد شعب دراسية")
    } else {
        LazyColumn {
            items(uiState.sections) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("السعة: ${item.capacity}") },
                    trailingContent = {
                        IconButton(onClick = { onAction(AcademicStructureAction.DeleteSection(item.id)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun StudyPlansList(uiState: AcademicStructureUiState, onAction: (AcademicStructureAction) -> Unit) {
    if (uiState.selectedMajorId == null) {
        EmptyState("الرجاء تحديد تخصص أولاً")
        return
    }
    if (uiState.studyPlans.isEmpty()) {
        EmptyState("لا توجد خطط دراسية")
    } else {
        LazyColumn {
            items(uiState.studyPlans) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("الساعات: ${item.totalCredits}") },
                    trailingContent = {
                        IconButton(onClick = { onAction(AcademicStructureAction.DeleteStudyPlan(item.id)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
