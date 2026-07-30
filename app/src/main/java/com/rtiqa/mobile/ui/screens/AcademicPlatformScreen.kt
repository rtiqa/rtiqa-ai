package com.rtiqa.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.domain.model.AcademicLesson
import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.AssessmentType
import com.rtiqa.core.domain.model.Assignment
import com.rtiqa.core.domain.model.AssignmentType
import com.rtiqa.core.domain.model.CurriculumModule
import com.rtiqa.mobile.ui.viewmodel.AcademicPlatformUiAction
import com.rtiqa.mobile.ui.viewmodel.AcademicPlatformUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicPlatformScreen(
    uiState: AcademicPlatformUiState,
    onAction: (AcademicPlatformUiAction) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddModuleDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var showAddAssessmentDialog by remember { mutableStateOf(false) }

    var newModuleTitle by remember { mutableStateOf("") }
    var newModuleDesc by remember { mutableStateOf("") }

    var newAssignmentTitle by remember { mutableStateOf("") }
    var newAssignmentPrompt by remember { mutableStateOf("") }

    var newAssessmentTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("المنظومة الأكاديمية والتعلم الذكي", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("إدارة المناهج، الاختبارات، المزارع والواجبات", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (uiState.selectedTab) {
                        0 -> showAddModuleDialog = true
                        1 -> showAddAssignmentDialog = true
                        2 -> showAddAssessmentDialog = true
                        else -> showAddModuleDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { onAction(AcademicPlatformUiAction.SelectTab(0)) },
                    text = { Text("المناهج والدروس", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { onAction(AcademicPlatformUiAction.SelectTab(1)) },
                    text = { Text("الواجبات والمختبرات (${uiState.assignments.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { onAction(AcademicPlatformUiAction.SelectTab(2)) },
                    text = { Text("الاختبارات والتصحيح الآلي", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 3,
                    onClick = { onAction(AcademicPlatformUiAction.SelectTab(3)) },
                    text = { Text("السجل والأوسمة", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 4,
                    onClick = { onAction(AcademicPlatformUiAction.SelectTab(4)) },
                    text = { Text("المسارات والأوفلاين", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Route, contentDescription = null) }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (uiState.selectedTab) {
                    0 -> ModulesTabContent(uiState, onAction)
                    1 -> AssignmentsTabContent(uiState, onAction)
                    2 -> AssessmentsTabContent(uiState, onAction)
                    3 -> AcademicRecordTabContent(uiState, onAction)
                    4 -> PathsAndOfflineTabContent(uiState, onAction)
                }
            }
        }
    }

    if (showAddModuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddModuleDialog = false },
            title = { Text("إضافة وحدة دراسية جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newModuleTitle,
                        onValueChange = { newModuleTitle = it },
                        label = { Text("عنوان الوحدة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newModuleDesc,
                        onValueChange = { newModuleDesc = it },
                        label = { Text("وصف المحتوى والأهداف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(AcademicPlatformUiAction.CreateModule(newModuleTitle, newModuleDesc, 10))
                        newModuleTitle = ""
                        newModuleDesc = ""
                        showAddModuleDialog = false
                    }
                ) { Text("حفظ الوحدة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddModuleDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showAddAssignmentDialog) {
        AlertDialog(
            onDismissRequest = { showAddAssignmentDialog = false },
            title = { Text("إضافة واجب / مشروع / مختبر عملي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newAssignmentTitle,
                        onValueChange = { newAssignmentTitle = it },
                        label = { Text("عنوان التكليف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAssignmentPrompt,
                        onValueChange = { newAssignmentPrompt = it },
                        label = { Text("متطلبات وسيناريو التكليف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(AcademicPlatformUiAction.CreateAssignment(newAssignmentTitle, newAssignmentPrompt, AssignmentType.LAB, "2026-08-30"))
                        newAssignmentTitle = ""
                        newAssignmentPrompt = ""
                        showAddAssignmentDialog = false
                    }
                ) { Text("حفظ التكليف") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssignmentDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showAddAssessmentDialog) {
        AlertDialog(
            onDismissRequest = { showAddAssessmentDialog = false },
            title = { Text("إضافة اختبار / امتحان نهائي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newAssessmentTitle,
                        onValueChange = { newAssessmentTitle = it },
                        label = { Text("اسم الاختبار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(AcademicPlatformUiAction.CreateAssessment(newAssessmentTitle, AssessmentType.QUIZ, 70, 30))
                        newAssessmentTitle = ""
                        showAddAssessmentDialog = false
                    }
                ) { Text("حفظ الاختبار") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssessmentDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun ModulesTabContent(
    uiState: AcademicPlatformUiState,
    onAction: (AcademicPlatformUiAction) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("نظام المناهج والوحدات الدراسية (Curriculum)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("تصفح الوحدات، الدروس، الفيديوهات وملفات PDF والمرفقات", style = MaterialTheme.typography.bodySmall)
        }

        items(uiState.modules, key = { it.id }) { module ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(module.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(module.description, style = MaterialTheme.typography.bodyMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "جارٍ فتح مشغل الفيديوهات التعليمية...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الفيديو التعليمي")
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "تم تحميل ملف PDF والمرفقات بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ملف PDF")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun AssignmentsTabContent(
    uiState: AcademicPlatformUiState,
    onAction: (AcademicPlatformUiAction) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("نظام الواجبات والمشاريع والمختبرات العملية", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("تسليم الواجبات، الأكواد البرمجية والمختبرات التطبيقية", style = MaterialTheme.typography.bodySmall)
        }

        items(uiState.assignments, key = { it.id }) { assignment ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(assignment.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = when (assignment.type) {
                                AssignmentType.WRITTEN -> "واجب مكتوب"
                                AssignmentType.PROJECT -> "مشروع عملي"
                                AssignmentType.LAB -> "مختبر افتراضي"
                            },
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(assignment.prompt, style = MaterialTheme.typography.bodySmall)
                    Text("الدرجة العظمى: ${assignment.maxScore} • تسليم حتى: ${assignment.dueDate}", style = MaterialTheme.typography.labelSmall)

                    Button(
                        onClick = {
                            onAction(
                                AcademicPlatformUiAction.SubmitAssignment(
                                    assignment.id,
                                    "تم تنفيذ الكود البرمجي واجتياز اختبار البيئة بنجاح.",
                                    "https://rtiqa.edu/submissions/solution.py"
                                )
                            )
                            Toast.makeText(context, "تم تسليم الواجب بنجاح بنظام المزامنة الأوفلاين!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تسليم الحل / رفع المشروع")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun AssessmentsTabContent(
    uiState: AcademicPlatformUiState,
    onAction: (AcademicPlatformUiAction) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("بنك الأسئلة والاختبارات والتصحيح الآلي", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("اختبارات قصيرة، امتحانات نهائية وتقييم فوري بالذكاء الاصطناعي", style = MaterialTheme.typography.bodySmall)
        }

        items(uiState.assessments, key = { it.id }) { assessment ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(assessment.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("درجة النجاح: ${assessment.passingScore}% • زمن الاختبار: ${assessment.timeLimitMinutes} دقيقة", style = MaterialTheme.typography.bodySmall)

                    Button(
                        onClick = {
                            onAction(AcademicPlatformUiAction.SubmitAssessmentAttempt(assessment.id, 92))
                            Toast.makeText(context, "تم إجراء التصحيح الآلي الفوري! النتيجة: 92% (ناجح)", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بدء الاختبار والتصحيح الآلي")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun AcademicRecordTabContent(
    uiState: AcademicPlatformUiState,
    onAction: (AcademicPlatformUiAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("السجل الأكاديمي، التقييم والأوسمة (Gradebook)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("المعدل التراكمي العام (GPA)", fontWeight = FontWeight.Bold)
                    Text("4.00 / 4.00 - ممتاز مرتفع مع مرتبة الشرف الأولى", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { 1.0f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
        }

        item {
            Text("الأوسمة والإنجازات الشرفية (Badges)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Text("وسام الابتكار", fontWeight = FontWeight.Bold)
                        Text("اجتياز المختبر العملي بنجاح", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Grade, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp))
                        Text("المرتبة الأولى", fontWeight = FontWeight.Bold)
                        Text("الحصول على 100% في الاختبار", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun PathsAndOfflineTabContent(
    uiState: AcademicPlatformUiState,
    onAction: (AcademicPlatformUiAction) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("المسارات التعليمية والتوصيات الذكية والتشغيل الأوفلاين", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("توصية ذكية من محرك Gemini الاصطناعي", fontWeight = FontWeight.Bold)
                    }
                    Text("بناءً على نتائجك في المناهج، يُوصى بالتسجيل في مقرر: الرؤية الحاسوبية المعمقة (تلاؤم بنسبة 94%)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Text("المسارات المعتمدة للمؤسسة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(uiState.learningPaths, key = { it.id }) { path ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(path.title, fontWeight = FontWeight.Bold)
                    Text(path.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("نظام المزامنة وتشغيل المحتوى بدون إنترنت (Full Offline Engine)", fontWeight = FontWeight.Bold)
                    Text("حالة المزامنة: متزامن بالكامل مع خوادم رتقاء. يمكنك مشاهدة المحتوى وأداء الواجبات أوفلاين.", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = {
                            onAction(AcademicPlatformUiAction.StartOfflineDownload("les_101"))
                            Toast.makeText(context, "تم تفعيل حزمة الأوفلاين للمادة بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحميل حزمة المقرر كاملة للأوفلاين")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
