package com.rtiqa.feature.admin.teacher

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.design.tokens.RdsColor
import com.rtiqa.core.design.tokens.RdsCornerRadius
import com.rtiqa.core.design.tokens.RdsIcons
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.card.RdsCard
import com.rtiqa.core.ui.state.RdsEmptyState
import com.rtiqa.core.ui.state.RdsLoadingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    uiState: TeacherDashboardUiState,
    uiEvent: Flow<TeacherDashboardUiEvent>? = null,
    onAction: (TeacherDashboardUiAction) -> Unit,
    onBack: () -> Unit,
    onNavigateToGradebook: (() -> Unit)? = null,
    onNavigateToClasses: (() -> Unit)? = null,
    onNavigateToAcademicPlatform: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Listen to one-time events
    LaunchedEffect(key1 = uiEvent) {
        uiEvent?.collectLatest { event ->
            when (event) {
                is TeacherDashboardUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is TeacherDashboardUiEvent.NavigateToClassDetail -> {
                    Toast.makeText(context, "الانتقال إلى الفصل: ${event.classId}", Toast.LENGTH_SHORT).show()
                }
                is TeacherDashboardUiEvent.NavigateToGrading -> {
                    Toast.makeText(context, "فتح تصحيح التسليم: ${event.submissionId}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "لوحة تحكم المعلم",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            RdsBadge(
                                text = "بوابة المعلم",
                                type = RdsBadgeType.AI,
                                testTag = "teacher_portal_badge"
                            )
                        }
                        Text(
                            text = "${uiState.teacherName} • ${uiState.schoolName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("teacher_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(TeacherDashboardUiAction.Refresh) },
                        modifier = Modifier.testTag("teacher_refresh_button")
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(TeacherDashboardUiAction.OpenQuickNoticeDialog(null))
                },
                modifier = Modifier.testTag("teacher_fab_notice"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = "إرسال تعميم")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "تعميم سريع", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier.testTag("teacher_dashboard_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Bar
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.testTag("teacher_tab_row")
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { onAction(TeacherDashboardUiAction.SelectTab(0)) },
                    text = { Text("نظرة عامة والجدول", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_overview")
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { onAction(TeacherDashboardUiAction.SelectTab(1)) },
                    text = { Text("الفصول والشعب (${uiState.classes.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_classes")
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { onAction(TeacherDashboardUiAction.SelectTab(2)) },
                    text = { Text("قائمة التصحيح (${uiState.pendingSubmissions.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_grading")
                )
                Tab(
                    selected = uiState.selectedTab == 3,
                    onClick = { onAction(TeacherDashboardUiAction.SelectTab(3)) },
                    text = { Text("التحليلات والمتابعة", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_analytics")
                )
            }

            if (uiState.isLoading) {
                RdsLoadingState(
                    message = "جاري تحميل بيانات لوحة المعلم...",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                when (uiState.selectedTab) {
                    0 -> TeacherOverviewTab(
                        uiState = uiState,
                        onAction = onAction,
                        onNavigateToGradebook = onNavigateToGradebook,
                        onNavigateToClasses = onNavigateToClasses,
                        onNavigateToAcademicPlatform = onNavigateToAcademicPlatform
                    )
                    1 -> TeacherClassesTab(
                        uiState = uiState,
                        onAction = onAction
                    )
                    2 -> TeacherGradingTab(
                        uiState = uiState,
                        onAction = onAction
                    )
                    3 -> TeacherAnalyticsTab(
                        uiState = uiState,
                        onNavigateToGradebook = onNavigateToGradebook
                    )
                }
            }
        }
    }

    // Quick Notice Broadcast Dialog
    if (uiState.showQuickNoticeDialog) {
        QuickNoticeDialog(
            classes = uiState.classes,
            selectedClass = uiState.selectedClassForNotice,
            onDismiss = { onAction(TeacherDashboardUiAction.CloseQuickNoticeDialog) },
            onSend = { target, msg ->
                onAction(TeacherDashboardUiAction.SendQuickNotice(target, msg))
            }
        )
    }
}

@Composable
private fun TeacherOverviewTab(
    uiState: TeacherDashboardUiState,
    onAction: (TeacherDashboardUiAction) -> Unit,
    onNavigateToGradebook: (() -> Unit)?,
    onNavigateToClasses: (() -> Unit)?,
    onNavigateToAcademicPlatform: (() -> Unit)?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Profile Header Card
        item {
            RdsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_header_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.teacherName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.department,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.schoolName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Quick KPI Metrics Grid
        item {
            Text(
                text = "مؤشرات الأداء السريع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val m1 = uiState.metrics.getOrNull(0)
                    if (m1 != null) {
                        TeacherKpiCard(
                            metric = m1,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_card_0"
                        )
                    }
                    val m2 = uiState.metrics.getOrNull(1)
                    if (m2 != null) {
                        TeacherKpiCard(
                            metric = m2,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_card_1"
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val m3 = uiState.metrics.getOrNull(2)
                    if (m3 != null) {
                        TeacherKpiCard(
                            metric = m3,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_card_2"
                        )
                    }
                    val m4 = uiState.metrics.getOrNull(3)
                    if (m4 != null) {
                        TeacherKpiCard(
                            metric = m4,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_card_3"
                        )
                    }
                }
            }
        }

        // Quick Navigation Shortcuts
        item {
            Text(
                text = "الإجراءات الأكاديمية السريعة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToGradebook?.invoke() }
                        .testTag("quick_action_gradebook"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.Grade,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("كشف الدرجات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("رصد وتقييم الطلاب", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (onNavigateToClasses != null) onNavigateToClasses()
                            else onAction(TeacherDashboardUiAction.SelectTab(1))
                        }
                        .testTag("quick_action_classes"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("الفصول والشعب", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("الحضور والتوزيع", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAcademicPlatform?.invoke() }
                        .testTag("quick_action_academic_platform"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("المناهج والخطط", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("المحتوى والواجبات", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Upcoming Schedule Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "جدول الحصص والمحاضرات اليوم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onAction(TeacherDashboardUiAction.SelectTab(1)) }) {
                    Text("عرض الكل")
                }
            }
        }

        items(uiState.upcomingSchedule, key = { it.id }) { item ->
            ScheduleRowCard(scheduleItem = item)
        }
    }
}

@Composable
private fun TeacherClassesTab(
    uiState: TeacherDashboardUiState,
    onAction: (TeacherDashboardUiAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("teacher_classes_tab")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search in assigned classes
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onAction(TeacherDashboardUiAction.SearchQueryChanged(it)) },
            placeholder = { Text("بحث في الفصول أو المواد الموكلة...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("teacher_class_search_field"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        val filteredClasses = if (uiState.searchQuery.isBlank()) {
            uiState.classes
        } else {
            uiState.classes.filter {
                it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                it.subject.contains(uiState.searchQuery, ignoreCase = true) ||
                it.gradeLevel.contains(uiState.searchQuery, ignoreCase = true)
            }
        }

        if (filteredClasses.isEmpty()) {
            RdsEmptyState(
                title = "لا توجد فصول مطابقة",
                description = "جرب البحث باسم فصل أو مادة أخرى"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredClasses, key = { it.id }) { classItem ->
                    ClassCard(
                        classItem = classItem,
                        onClick = { onAction(TeacherDashboardUiAction.ClassClicked(classItem.id)) },
                        onSendNotice = { onAction(TeacherDashboardUiAction.OpenQuickNoticeDialog(classItem)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherGradingTab(
    uiState: TeacherDashboardUiState,
    onAction: (TeacherDashboardUiAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("teacher_grading_tab")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "قائمة التسليمات التي تتطلب رصد درجات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            RdsBadge(
                text = "${uiState.pendingSubmissions.size} متبقية",
                type = if (uiState.pendingSubmissions.isNotEmpty()) RdsBadgeType.WARNING else RdsBadgeType.SUCCESS
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.pendingSubmissions.isEmpty()) {
            RdsEmptyState(
                title = "تم الانتهاء من جميع التصحيحات! 🎉",
                description = "لا توجد أي تسليمات بانتظار التقييم حاليًا."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.pendingSubmissions, key = { it.id }) { sub ->
                    SubmissionCard(
                        submission = sub,
                        onGradeClick = { onAction(TeacherDashboardUiAction.GradeSubmissionClicked(sub.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherAnalyticsTab(
    uiState: TeacherDashboardUiState,
    onNavigateToGradebook: (() -> Unit)?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("teacher_analytics_tab"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RdsCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نظرة تحليلية شاملة للأداء الأكاديمي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تحليل حضور الطلاب ومستوى تسليم الواجبات عبر كافة الفصول",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "معدل حضور الطلاب حسب الفصول",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.classes, key = { it.id }) { classItem ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = classItem.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(classItem.attendanceRate * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${classItem.gradeLevel} • ${classItem.section}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { classItem.attendanceRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (classItem.attendanceRate >= 0.95f) RdsColor.Success else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = { onNavigateToGradebook?.invoke() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_analytics_goto_gradebook"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Assessment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("فتح كشف الدرجات الأكاديمي التفصيلي", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TeacherKpiCard(
    metric: TeacherMetric,
    modifier: Modifier = Modifier,
    testTag: String = "teacher_kpi_card"
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = metric.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = metric.subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ScheduleRowCard(
    scheduleItem: UpcomingScheduleItem,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("schedule_card_${scheduleItem.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (scheduleItem.isLiveNow) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (scheduleItem.isLiveNow) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (scheduleItem.isLiveNow) Icons.Default.PlayCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (scheduleItem.isLiveNow) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = scheduleItem.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (scheduleItem.isLiveNow) {
                        Spacer(modifier = Modifier.width(6.dp))
                        RdsBadge(text = "جارية الآن", type = RdsBadgeType.AI)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${scheduleItem.className} • ${scheduleItem.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = scheduleItem.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ClassCard(
    classItem: TeacherClassItem,
    onClick: () -> Unit,
    onSendNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    RdsCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("class_card_${classItem.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = classItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${classItem.gradeLevel} • ${classItem.section}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RdsBadge(
                    text = "${classItem.studentCount} طالبًا",
                    type = RdsBadgeType.INFO
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = classItem.scheduleTime, style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Room,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = classItem.room, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onSendNotice,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال تعميم للطلاب", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: PendingSubmissionItem,
    onGradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("submission_card_${submission.id}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RdsBadge(
                    text = submission.type,
                    type = RdsBadgeType.WARNING
                )
                Text(
                    text = submission.submittedTimeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = submission.assignmentTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "الطالب: ${submission.studentName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = submission.className,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الدرجة القصوى: ${submission.maxScore}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = onGradeClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تقييم ورصد", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuickNoticeDialog(
    classes: List<TeacherClassItem>,
    selectedClass: TeacherClassItem?,
    onDismiss: () -> Unit,
    onSend: (target: String, message: String) -> Unit
) {
    var noticeMessage by remember { mutableStateOf("") }
    var selectedTarget by remember {
        mutableStateOf(selectedClass?.name ?: "كافة الفصول والشعب")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إرسال تعميم أو تنبيه للطلاب", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الجهة المستهدفة: $selectedTarget",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = noticeMessage,
                    onValueChange = { noticeMessage = it },
                    label = { Text("نص التنبيه أو التعميم") },
                    placeholder = { Text("مثال: تذكير بموعد تسليم المشروع النهائي غداً الساعة 10 مساءً...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (noticeMessage.isNotBlank()) {
                        onSend(selectedTarget, noticeMessage)
                    }
                },
                enabled = noticeMessage.isNotBlank()
            ) {
                Text("إرسال الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
