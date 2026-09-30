package com.rtiqa.feature.admin.teacher.gradebook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rtiqa.core.design.tokens.RdsCornerRadius
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.card.RdsCard
import com.rtiqa.core.ui.card.RdsOutlinedCard
import com.rtiqa.core.ui.state.RdsEmptyState
import com.rtiqa.core.ui.state.RdsErrorState
import com.rtiqa.core.ui.state.RdsLoadingState

/**
 * Real production Academic Gradebook Screen.
 * Fully state-driven Composable following RTIQA Material 3 and RTL conventions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicGradebookScreen(
    uiState: AcademicGradebookUiState,
    onAction: (AcademicGradebookUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سجل الدرجات الأكاديمي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val classLabel = uiState.availableClasses
                            .find { it.classId == uiState.selectedClassId }?.className
                            ?: uiState.selectedClassId.takeIf { it.isNotBlank() }
                        if (classLabel != null) {
                            Text(
                                text = "$classLabel • ${uiState.selectedTerm}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("gradebook_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(AcademicGradebookUiAction.OpenAddAssessmentDialog) },
                        modifier = Modifier.testTag("gradebook_add_assessment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة تقييم"
                        )
                    }
                    IconButton(
                        onClick = { onAction(AcademicGradebookUiAction.OpenExportDialog) },
                        modifier = Modifier.testTag("gradebook_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "تصدير كشف الدرجات"
                        )
                    }
                    IconButton(
                        onClick = { onAction(AcademicGradebookUiAction.Refresh) },
                        modifier = Modifier.testTag("gradebook_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.testTag("gradebook_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    RdsLoadingState(
                        message = "جاري تحميل سجل الدرجات...",
                        testTag = "gradebook_loading_state"
                    )
                }
                uiState.errorMessage != null -> {
                    RdsErrorState(
                        message = uiState.errorMessage,
                        onRetryClick = { onAction(AcademicGradebookUiAction.Refresh) },
                        testTag = "gradebook_error_state"
                    )
                }
                uiState.selectedClassId.isBlank() -> {
                    GradebookNoClassSelectedState(
                        availableClasses = uiState.availableClasses,
                        onSelectClass = { onAction(AcademicGradebookUiAction.SelectClass(it)) }
                    )
                }
                uiState.students.isEmpty() && uiState.columns.isEmpty() -> {
                    RdsEmptyState(
                        title = "لا توجد بيانات درجات",
                        description = "لم يتم رصد أي درجات أو تقييمات لهذا الفصل حتى الآن",
                        actionText = "إضافة تقييم",
                        onActionClick = { onAction(AcademicGradebookUiAction.OpenAddAssessmentDialog) },
                        testTag = "gradebook_empty_data_state"
                    )
                }
                else -> {
                    GradebookContent(
                        uiState = uiState,
                        onAction = onAction
                    )
                }
            }
        }
    }

    // Score Editor Dialog
    if (uiState.editingStudentScore != null) {
        val (student, column) = uiState.editingStudentScore
        AlertDialog(
            onDismissRequest = { onAction(AcademicGradebookUiAction.DismissScoreEditor) },
            title = {
                Text(
                    text = "رصد درجة الطالب",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "الطالب: ${student.studentName} (${student.studentNumber})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "التقييم: ${column.title} (الدرجة العظمى: ${column.maxScore})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = uiState.currentEditScoreValue,
                        onValueChange = { onAction(AcademicGradebookUiAction.UpdateScoreValue(it)) },
                        label = { Text("الدرجة المستحقة") },
                        placeholder = { Text("مثال: 8.5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gradebook_score_edit_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onAction(AcademicGradebookUiAction.SaveScoreEditor) },
                    modifier = Modifier.testTag("gradebook_save_score_button")
                ) {
                    Text("حفظ الدرجة")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(AcademicGradebookUiAction.DismissScoreEditor) },
                    modifier = Modifier.testTag("gradebook_dismiss_score_button")
                ) {
                    Text("إلغاء")
                }
            },
            modifier = Modifier.testTag("gradebook_score_editor_dialog")
        )
    }

    // Add Assessment Dialog
    if (uiState.showAddAssessmentDialog) {
        var title by remember { mutableStateOf("") }
        var maxScoreText by remember { mutableStateOf("10.0") }
        var weightText by remember { mutableStateOf("10") }
        var selectedCategory by remember { mutableStateOf("واجبات") }
        val categories = listOf("واجبات", "اختبارات قصيرة", "مشروع عملي", "اختبار نهائي")

        AlertDialog(
            onDismissRequest = { onAction(AcademicGradebookUiAction.DismissAddAssessmentDialog) },
            title = {
                Text(
                    text = "إضافة عمود تقييم جديد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان التقييم") },
                        placeholder = { Text("مثال: الواجب الأول") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gradebook_add_assessment_title_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = maxScoreText,
                            onValueChange = { maxScoreText = it },
                            label = { Text("الدرجة العظمى") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gradebook_add_assessment_max_score_input")
                        )
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("الوزن (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gradebook_add_assessment_weight_input")
                        )
                    }
                    Text(
                        text = "التصنيف",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val maxScore = maxScoreText.toDoubleOrNull() ?: 10.0
                        val weight = weightText.toIntOrNull() ?: 10
                        onAction(
                            AcademicGradebookUiAction.SaveNewAssessment(
                                title = title.trim(),
                                maxScore = maxScore,
                                weight = weight,
                                category = selectedCategory
                            )
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.testTag("gradebook_save_assessment_button")
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(AcademicGradebookUiAction.DismissAddAssessmentDialog) },
                    modifier = Modifier.testTag("gradebook_dismiss_assessment_button")
                ) {
                    Text("إلغاء")
                }
            },
            modifier = Modifier.testTag("gradebook_add_assessment_dialog")
        )
    }

    // Export Dialog
    if (uiState.showExportDialog) {
        AlertDialog(
            onDismissRequest = { onAction(AcademicGradebookUiAction.DismissExportDialog) },
            title = {
                Text(
                    text = "تصدير كشف الدرجات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "اختر صيغة الملف لتصدير درجات الفصل:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { onAction(AcademicGradebookUiAction.ExportGradebook("PDF")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gradebook_export_pdf_button")
                    ) {
                        Text("تصدير بصيغة PDF")
                    }
                    Button(
                        onClick = { onAction(AcademicGradebookUiAction.ExportGradebook("EXCEL")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gradebook_export_excel_button")
                    ) {
                        Text("تصدير بصيغة Excel (XLSX)")
                    }
                    OutlinedButton(
                        onClick = { onAction(AcademicGradebookUiAction.ExportGradebook("CSV")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gradebook_export_csv_button")
                    ) {
                        Text("تصدير بصيغة CSV")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { onAction(AcademicGradebookUiAction.DismissExportDialog) },
                    modifier = Modifier.testTag("gradebook_dismiss_export_button")
                ) {
                    Text("إلغاء")
                }
            },
            modifier = Modifier.testTag("gradebook_export_dialog")
        )
    }
}

@Composable
private fun GradebookNoClassSelectedState(
    availableClasses: List<GradebookClassOption>,
    onSelectClass: (String) -> Unit
) {
    if (availableClasses.isEmpty()) {
        RdsEmptyState(
            title = "لم يتم تحديد فصل دراسي",
            description = "يرجى اختيار فصل دراسي لعرض سجل الدرجات",
            testTag = "gradebook_empty_class_state"
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "اختر فصلاً دراسياً لعرض الدرجات:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableClasses, key = { it.classId }) { cls ->
                    RdsOutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gradebook_select_class_${cls.classId}"),
                        onClick = { onSelectClass(cls.classId) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = cls.className,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${cls.subject} • ${cls.academicTerm}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(onClick = { onSelectClass(cls.classId) }) {
                                Text("اختيار")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradebookContent(
    uiState: AcademicGradebookUiState,
    onAction: (AcademicGradebookUiAction) -> Unit
) {
    val filteredStudents = remember(
        uiState.students,
        uiState.searchQuery,
        uiState.statusFilter,
        uiState.selectedViewMode
    ) {
        uiState.students.filter { student ->
            val matchesQuery = uiState.searchQuery.isBlank() ||
                student.studentName.contains(uiState.searchQuery, ignoreCase = true) ||
                student.studentNumber.contains(uiState.searchQuery, ignoreCase = true)
            val matchesStatus = uiState.statusFilter == null || student.status == uiState.statusFilter
            val matchesViewMode = when (uiState.selectedViewMode) {
                2 -> student.status == GradeStatus.CRITICAL || student.status == GradeStatus.NEEDS_SUPPORT
                else -> true
            }
            matchesQuery && matchesStatus && matchesViewMode
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Available Classes Selector row if multiple exist
        if (uiState.availableClasses.size > 1) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.availableClasses, key = { it.classId }) { cls ->
                        FilterChip(
                            selected = uiState.selectedClassId == cls.classId,
                            onClick = { onAction(AcademicGradebookUiAction.SelectClass(cls.classId)) },
                            label = { Text(cls.className) },
                            modifier = Modifier.testTag("gradebook_class_chip_${cls.classId}")
                        )
                    }
                }
            }
        }

        // View Mode Tabs
        item {
            TabRow(
                selectedTabIndex = uiState.selectedViewMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gradebook_view_mode_tab_row")
            ) {
                Tab(
                    selected = uiState.selectedViewMode == 0,
                    onClick = { onAction(AcademicGradebookUiAction.SelectViewMode(0)) },
                    text = { Text("كشف الدرجات", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    modifier = Modifier.testTag("gradebook_tab_matrix")
                )
                Tab(
                    selected = uiState.selectedViewMode == 1,
                    onClick = { onAction(AcademicGradebookUiAction.SelectViewMode(1)) },
                    text = { Text("إحصائيات الفصل", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = null) },
                    modifier = Modifier.testTag("gradebook_tab_statistics")
                )
                Tab(
                    selected = uiState.selectedViewMode == 2,
                    onClick = { onAction(AcademicGradebookUiAction.SelectViewMode(2)) },
                    text = { Text("المتعثرون", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    modifier = Modifier.testTag("gradebook_tab_at_risk")
                )
            }
        }

        // Statistics Summary Bar
        item {
            GradebookStatisticsBar(
                classAverage = uiState.classAverage,
                highestScore = uiState.highestScore,
                lowestScore = uiState.lowestScore,
                passRatePercentage = uiState.passRatePercentage
            )
        }

        // Search & Status Filters for Modes 0 & 2
        if (uiState.selectedViewMode != 1) {
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onAction(AcademicGradebookUiAction.SearchQueryChanged(it)) },
                    placeholder = { Text("بحث باسم الطالب أو الرقم الأكاديمي...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gradebook_search_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.statusFilter == null,
                            onClick = { onAction(AcademicGradebookUiAction.FilterStatus(null)) },
                            label = { Text("الكل") },
                            modifier = Modifier.testTag("gradebook_filter_all")
                        )
                    }
                    items(GradeStatus.values()) { status ->
                        FilterChip(
                            selected = uiState.statusFilter == status,
                            onClick = { onAction(AcademicGradebookUiAction.FilterStatus(status)) },
                            label = { Text(status.labelAr) },
                            modifier = Modifier.testTag("gradebook_status_filter_chip_${status.name}")
                        )
                    }
                }
            }
        }

        // Content by Mode
        when (uiState.selectedViewMode) {
            1 -> {
                item {
                    GradebookDetailedAnalytics(
                        students = uiState.students,
                        passRate = uiState.passRatePercentage
                    )
                }
            }
            else -> {
                if (filteredStudents.isEmpty()) {
                    item {
                        RdsEmptyState(
                            title = if (uiState.selectedViewMode == 2) "لا يوجد طلاب متعثرون" else "لا توجد نتائج مطابقة",
                            description = if (uiState.selectedViewMode == 2) "جميع الطلاب في هذا الفصل محققون للحد الأدنى من معايير النجاح" else "جرّب تغيير معايير البحث أو التصفية",
                            testTag = "gradebook_filtered_empty_state"
                        )
                    }
                } else {
                    items(filteredStudents, key = { it.studentId }) { student ->
                        StudentGradeCard(
                            student = student,
                            columns = uiState.columns,
                            onEditScore = { col ->
                                onAction(AcademicGradebookUiAction.OpenScoreEditor(student, col))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradebookStatisticsBar(
    classAverage: Double,
    highestScore: Double,
    lowestScore: Double,
    passRatePercentage: Int,
    modifier: Modifier = Modifier
) {
    RdsCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gradebook_statistics_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatisticTile(title = "المتوسط العام", value = "$classAverage")
            StatisticTile(title = "أعلى درجة", value = "$highestScore")
            StatisticTile(title = "أدنى درجة", value = "$lowestScore")
            StatisticTile(title = "نسبة النجاح", value = "$passRatePercentage%")
        }
    }
}

@Composable
private fun StatisticTile(
    title: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StudentGradeCard(
    student: StudentGradeRow,
    columns: List<AssessmentColumn>,
    onEditScore: (AssessmentColumn) -> Unit
) {
    RdsOutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gradebook_student_card_${student.studentId}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Avatar, Name, Number, Total & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.avatarInitial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الرقم الأكاديمي: ${student.studentNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val badgeType = when (student.status) {
                        GradeStatus.EXCELLENT -> RdsBadgeType.SUCCESS
                        GradeStatus.VERY_GOOD -> RdsBadgeType.SUCCESS
                        GradeStatus.GOOD -> RdsBadgeType.INFO
                        GradeStatus.NEEDS_SUPPORT -> RdsBadgeType.WARNING
                        GradeStatus.CRITICAL -> RdsBadgeType.ERROR
                    }
                    RdsBadge(
                        text = student.status.labelAr,
                        type = badgeType,
                        testTag = "gradebook_badge_${student.studentId}"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${student.totalScore} / ${student.maxPossibleScore} (${student.percentage.toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Assessment Scores Matrix for this student
            if (columns.isNotEmpty()) {
                Text(
                    text = "الدرجات المرصودة:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    columns.forEach { col ->
                        val scoreObj = student.scores[col.id]
                        val scoreVal = scoreObj?.score
                        OutlinedCard(
                            onClick = { onEditScore(col) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gradebook_score_chip_${student.studentId}_${col.id}"),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (scoreVal != null) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = col.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${col.category} • العظمى: ${col.maxScore} (${col.weightPercentage}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (scoreVal != null) {
                                        Text(
                                            text = "$scoreVal / ${col.maxScore}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = "غير مرصود",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "رصد الدرجة",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradebookDetailedAnalytics(
    students: List<StudentGradeRow>,
    passRate: Int
) {
    RdsCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gradebook_detailed_analytics_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "توزيع التقديرات الأكاديمية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val totalStudents = students.size.coerceAtLeast(1)

            GradeStatus.values().forEach { status ->
                val count = students.count { it.status == status }
                val ratio = count.toFloat() / totalStudents

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = status.labelAr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$count طالب (${(ratio * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when (status) {
                            GradeStatus.EXCELLENT, GradeStatus.VERY_GOOD -> MaterialTheme.colorScheme.primary
                            GradeStatus.GOOD -> MaterialTheme.colorScheme.secondary
                            GradeStatus.NEEDS_SUPPORT -> MaterialTheme.colorScheme.tertiary
                            GradeStatus.CRITICAL -> MaterialTheme.colorScheme.error
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "نسبة اجتياز الفصل:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "$passRate%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
