package com.rtiqa.feature.admin.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicStructureScreen(
    uiState: AcademicStructureUiState,
    onAction: (AcademicStructureAction) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الهيكل الأكاديمي") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Open Create Dialog based on tab */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(AcademicTab.values()) { tab ->
                    val isSelected = uiState.currentTab == tab
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onAction(AcademicStructureAction.SelectTab(tab)) }
                    ) {
                        Text(
                            text = tab.titleAr,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (uiState.currentTab) {
                    AcademicTab.YEARS -> AcademicYearsList(uiState, onAction)
                    AcademicTab.TERMS -> SemestersList(uiState, onAction)
                    AcademicTab.GRADE_LEVELS -> GradeLevelsList(uiState, onAction)
                    AcademicTab.DEPARTMENTS -> DepartmentsList(uiState, onAction)
                    AcademicTab.MAJORS -> MajorsList(uiState, onAction)
                    AcademicTab.SUBJECTS -> SubjectsList(uiState, onAction)
                    AcademicTab.SECTIONS -> SectionsList(uiState, onAction)
                    AcademicTab.STUDY_PLANS -> StudyPlansList(uiState, onAction)
                }
            }
        }
    }
}
