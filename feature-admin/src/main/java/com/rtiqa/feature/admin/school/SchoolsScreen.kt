package com.rtiqa.feature.admin.school

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.Section
import com.rtiqa.core.domain.model.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolsScreen(
    uiState: SchoolUiState,
    onAction: (SchoolUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddAssessmentDialog by remember { mutableStateOf(false) }

    // Dialog state holders
    var studentName by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var studentDept by remember { mutableStateOf("علوم الحاسب") }

    var teacherName by remember { mutableStateOf("") }
    var teacherEmail by remember { mutableStateOf("") }
    var teacherDept by remember { mutableStateOf("الرياضيات التطبيقية") }

    var courseTitle by remember { mutableStateOf("") }
    var courseCategory by remember { mutableStateOf("علوم البيانات") }

    var assessmentTitle by remember { mutableStateOf("") }
    var assessmentPassingScore by remember { mutableStateOf("60") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "إدارة المدارس (Multi-Tenant)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.activeSchool?.let { school ->
                            Text(
                                text = "المدرسة النشطة: ${school.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                FloatingActionButton(
                    onClick = { onAction(SchoolUiAction.OpenFormDialog(null)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_school_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة مدرسة")
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Active School Highlight Banner
            ActiveSchoolBanner(
                activeSchool = uiState.activeSchool,
                schoolsCount = uiState.schools.size
            )

            // Main Tabs: 0 -> Schools List, 1 -> Active School Scope Data
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { onAction(SchoolUiAction.SelectTab(0)) },
                    text = { Text("قائمة المدارس (${uiState.schools.size})") },
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier.testTag("tab_schools_list")
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { onAction(SchoolUiAction.SelectTab(1)) },
                    text = { Text("بيانات المدرسة النشطة") },
                    icon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.testTag("tab_active_school_data")
                )
            }

            when (uiState.selectedTab) {
                0 -> SchoolsListTab(
                    schools = uiState.schools,
                    activeSchoolId = uiState.activeSchoolId,
                    onSelectActive = { schoolId -> onAction(SchoolUiAction.SelectActiveSchool(schoolId)) },
                    onEditSchool = { school -> onAction(SchoolUiAction.OpenFormDialog(school)) },
                    onDeleteSchool = { schoolId -> onAction(SchoolUiAction.DeleteSchool(schoolId)) }
                )
                1 -> ActiveSchoolDataTab(
                    uiState = uiState,
                    onCategoryTabSelected = { index -> onAction(SchoolUiAction.SelectCategoryTab(index)) },
                    onAddStudentClick = { showAddStudentDialog = true },
                    onAddTeacherClick = { showAddTeacherDialog = true },
                    onAddCourseClick = { showAddCourseDialog = true },
                    onAddAssessmentClick = { showAddAssessmentDialog = true }
                )
            }
        }
    }

    // Add/Edit School Form Dialog
    if (uiState.isFormDialogOpen) {
        AddEditSchoolDialog(
            school = uiState.editingSchool,
            onDismiss = { onAction(SchoolUiAction.CloseFormDialog) },
            onSave = { id, name, code, address, phone, studentsCount, teachersCount ->
                onAction(
                    SchoolUiAction.SaveSchool(
                        id = id,
                        name = name,
                        code = code,
                        address = address,
                        phone = phone,
                        studentsCount = studentsCount,
                        teachersCount = teachersCount
                    )
                )
            }
        )
    }

    // Dialog for adding student to active school
    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("إضافة طالب لمدرسة ${uiState.activeSchool?.name ?: ""}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("اسم الطالب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = studentEmail,
                        onValueChange = { studentEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = studentDept,
                        onValueChange = { studentDept = it },
                        label = { Text("القسم / التخصص") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentName.isNotBlank()) {
                            onAction(
                                SchoolUiAction.AddStudentToActiveSchool(
                                    name = studentName,
                                    email = studentEmail.ifBlank { "student@school.edu" },
                                    department = studentDept
                                )
                            )
                            studentName = ""
                            studentEmail = ""
                            showAddStudentDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog for adding teacher to active school
    if (showAddTeacherDialog) {
        AlertDialog(
            onDismissRequest = { showAddTeacherDialog = false },
            title = { Text("إضافة معلم لمدرسة ${uiState.activeSchool?.name ?: ""}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("اسم المعلم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = teacherEmail,
                        onValueChange = { teacherEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = teacherDept,
                        onValueChange = { teacherDept = it },
                        label = { Text("القسم / المادة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teacherName.isNotBlank()) {
                            onAction(
                                SchoolUiAction.AddTeacherToActiveSchool(
                                    name = teacherName,
                                    email = teacherEmail.ifBlank { "teacher@school.edu" },
                                    department = teacherDept
                                )
                            )
                            teacherName = ""
                            teacherEmail = ""
                            showAddTeacherDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTeacherDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog for adding course to active school
    if (showAddCourseDialog) {
        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("إضافة دورة لمدرسة ${uiState.activeSchool?.name ?: ""}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = courseTitle,
                        onValueChange = { courseTitle = it },
                        label = { Text("عنوان الدورة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = courseCategory,
                        onValueChange = { courseCategory = it },
                        label = { Text("الفئة / التصنيف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (courseTitle.isNotBlank()) {
                            onAction(
                                SchoolUiAction.AddCourseToActiveSchool(
                                    title = courseTitle,
                                    category = courseCategory
                                )
                            )
                            courseTitle = ""
                            showAddCourseDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog for adding assessment to active school
    if (showAddAssessmentDialog) {
        AlertDialog(
            onDismissRequest = { showAddAssessmentDialog = false },
            title = { Text("إضافة اختبار لمدرسة ${uiState.activeSchool?.name ?: ""}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = assessmentTitle,
                        onValueChange = { assessmentTitle = it },
                        label = { Text("عنوان الاختبار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = assessmentPassingScore,
                        onValueChange = { assessmentPassingScore = it },
                        label = { Text("درجة النجاح (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (assessmentTitle.isNotBlank()) {
                            onAction(
                                SchoolUiAction.AddAssessmentToActiveSchool(
                                    title = assessmentTitle,
                                    passingScore = assessmentPassingScore.toIntOrNull() ?: 60
                                )
                            )
                            assessmentTitle = ""
                            showAddAssessmentDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssessmentDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun ActiveSchoolBanner(
    activeSchool: School?,
    schoolsCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeSchool?.name ?: "لم يتم تحديد مدرسة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "رمز المدرسة: ${activeSchool?.code ?: "-"} | العنوان: ${activeSchool?.address ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "نشطة (Current)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SchoolsListTab(
    schools: List<School>,
    activeSchoolId: String,
    onSelectActive: (String) -> Unit,
    onEditSchool: (School) -> Unit,
    onDeleteSchool: (String) -> Unit
) {
    if (schools.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد مدارس مسجلة حتى الآن. اضغط (+) لإضافة مدرسة جديدة.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(schools, key = { it.id }) { school ->
                val isActive = school.id == activeSchoolId
                SchoolCard(
                    school = school,
                    isActive = isActive,
                    onSelectActive = { onSelectActive(school.id) },
                    onEdit = { onEditSchool(school) },
                    onDelete = { onDeleteSchool(school.id) }
                )
            }
        }
    }
}

@Composable
fun SchoolCard(
    school: School,
    isActive: Boolean,
    onSelectActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectActive() }
            .testTag("school_card_${school.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = school.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "كود: ${school.code} | هاتف: ${school.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "المدرسة النشطة",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(text = "الطلاب: ${school.studentsCount}", style = MaterialTheme.typography.bodySmall)
                Text(text = "المعلمون: ${school.teachersCount}", style = MaterialTheme.typography.bodySmall)
                Text(text = "العنوان: ${school.address}", style = MaterialTheme.typography.bodySmall)
            }

            if (!isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSelectActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("set_active_button_${school.id}")
                ) {
                    Text("تعيين كمدرسة نشطة (Active Tenant)")
                }
            }
        }
    }
}

@Composable
fun ActiveSchoolDataTab(
    uiState: SchoolUiState,
    onCategoryTabSelected: (Int) -> Unit,
    onAddStudentClick: () -> Unit,
    onAddTeacherClick: () -> Unit,
    onAddCourseClick: () -> Unit,
    onAddAssessmentClick: () -> Unit
) {
    val categories = listOf("الطلاب", "المعلمون", "الصفوف", "المواد", "الدورات", "الاختبارات")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = uiState.activeCategoryTab,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEachIndexed { index, title ->
                val count = when (index) {
                    0 -> uiState.students.size
                    1 -> uiState.teachers.size
                    2 -> uiState.sections.size
                    3 -> uiState.subjects.size
                    4 -> uiState.courses.size
                    5 -> uiState.assessments.size
                    else -> 0
                }
                Tab(
                    selected = uiState.activeCategoryTab == index,
                    onClick = { onCategoryTabSelected(index) },
                    text = { Text("$title ($count)") },
                    modifier = Modifier.testTag("category_tab_$index")
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (uiState.activeCategoryTab) {
                0 -> CategoryList(
                    items = uiState.students,
                    emptyText = "لا يوجد طلاب مسجلون في المدرسة النشطة حالياً.",
                    onAddClick = onAddStudentClick,
                    itemContent = { student ->
                        EntityItemCard(
                            title = student.name,
                            subtitle = "قسم: ${student.department} | بريد: ${student.email}",
                            badgeText = "طالب",
                            icon = Icons.Default.Person
                        )
                    }
                )
                1 -> CategoryList(
                    items = uiState.teachers,
                    emptyText = "لا يوجد معلمون مسجلون في المدرسة النشطة حالياً.",
                    onAddClick = onAddTeacherClick,
                    itemContent = { teacher ->
                        EntityItemCard(
                            title = teacher.name,
                            subtitle = "تخصص: ${teacher.department} | بريد: ${teacher.email}",
                            badgeText = "معلم",
                            icon = Icons.Default.People
                        )
                    }
                )
                2 -> CategoryList(
                    items = uiState.sections,
                    emptyText = "لا يوجد شعب / صفوف مضافة لهذه المدرسة حتى الآن.",
                    onAddClick = null,
                    itemContent = { section ->
                        EntityItemCard(
                            title = section.name,
                            subtitle = "السعة: ${section.capacity} طالب | عدد الطلاب: ${section.studentsCount}",
                            badgeText = "صف / شعبة",
                            icon = Icons.Default.Class
                        )
                    }
                )
                3 -> CategoryList(
                    items = uiState.subjects,
                    emptyText = "لا يوجد مواد دراسية مضافة لهذه المدرسة.",
                    onAddClick = null,
                    itemContent = { subject ->
                        EntityItemCard(
                            title = subject.name,
                            subtitle = "رمز المادة: ${subject.code} | الساعات المعتمده: ${subject.creditHours}",
                            badgeText = "مادة",
                            icon = Icons.Default.Book
                        )
                    }
                )
                4 -> CategoryList(
                    items = uiState.courses,
                    emptyText = "لا توجد دورات تدريبية مسجلة لهذه المدرسة.",
                    onAddClick = onAddCourseClick,
                    itemContent = { course ->
                        EntityItemCard(
                            title = course.title,
                            subtitle = "تصنيف: ${course.category} | الدروس: ${course.totalLessons}",
                            badgeText = "دورة",
                            icon = Icons.Default.MenuBook
                        )
                    }
                )
                5 -> CategoryList(
                    items = uiState.assessments,
                    emptyText = "لا توجد اختبارات مضافة لهذه المدرسة.",
                    onAddClick = onAddAssessmentClick,
                    itemContent = { assessment ->
                        EntityItemCard(
                            title = assessment.title,
                            subtitle = "درجة النجاح: ${assessment.passingScore}% | الوقت: ${assessment.timeLimitMinutes} دقيقة",
                            badgeText = "اختبار",
                            icon = Icons.Default.Quiz
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun <T> CategoryList(
    items: List<T>,
    emptyText: String,
    onAddClick: (() -> Unit)?,
    itemContent: @Composable (T) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (onAddClick != null) {
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة عنصر جديد للمدرسة النشطة")
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    itemContent(item)
                }
            }
        }
    }
}

@Composable
fun EntityItemCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
