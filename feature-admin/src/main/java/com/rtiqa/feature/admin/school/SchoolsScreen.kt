package com.rtiqa.feature.admin.school

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.domain.model.AcademicYear
import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.EducationStage
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.SchoolClass
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

    var deleteConfirmSchoolId by remember { mutableStateOf<String?>(null) }
    var showAddAcademicYearDialog by remember { mutableStateOf(false) }
    var showAddGradeLevelDialog by remember { mutableStateOf(false) }
    var showAddClassDialog by remember { mutableStateOf(false) }
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddAssessmentDialog by remember { mutableStateOf(false) }

    // Dialog Input States
    var ayName by remember { mutableStateOf("") }
    var ayStartDate by remember { mutableStateOf("2024-09-01") }
    var ayEndDate by remember { mutableStateOf("2025-06-30") }

    var gradeName by remember { mutableStateOf("") }
    var gradeCode by remember { mutableStateOf("") }
    var gradeSeq by remember { mutableStateOf("1") }
    var selectedStage by remember { mutableStateOf(EducationStage.SECONDARY) }

    var className by remember { mutableStateOf("") }
    var classGradeLevel by remember { mutableStateOf("") }
    var classRoom by remember { mutableStateOf("") }
    var classCapacity by remember { mutableStateOf("30") }

    var sectionName by remember { mutableStateOf("") }
    var sectionCapacity by remember { mutableStateOf("25") }

    var subjectName by remember { mutableStateOf("") }
    var subjectCode by remember { mutableStateOf("") }
    var subjectCreditHours by remember { mutableStateOf("3") }

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

            // Main Module Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { onAction(SchoolUiAction.SelectTab(0)) },
                    text = { Text("المدارس (${uiState.schools.size})") },
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier.testTag("tab_schools_list")
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { onAction(SchoolUiAction.SelectTab(1)) },
                    text = { Text("نظرة عامة والتفاصيل") },
                    icon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.testTag("tab_active_school_data")
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { onAction(SchoolUiAction.SelectTab(2)) },
                    text = { Text("الأعوام الدراسية (${uiState.academicYears.size})") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.testTag("tab_academic_years")
                )
                Tab(
                    selected = uiState.selectedTab == 3,
                    onClick = { onAction(SchoolUiAction.SelectTab(3)) },
                    text = { Text("المراحل (${uiState.gradeLevels.size})") },
                    icon = { Icon(Icons.Default.Grade, contentDescription = null) },
                    modifier = Modifier.testTag("tab_grade_levels")
                )
                Tab(
                    selected = uiState.selectedTab == 4,
                    onClick = { onAction(SchoolUiAction.SelectTab(4)) },
                    text = { Text("الصفوف (${uiState.schoolClasses.size})") },
                    icon = { Icon(Icons.Default.Class, contentDescription = null) },
                    modifier = Modifier.testTag("tab_classes")
                )
                Tab(
                    selected = uiState.selectedTab == 5,
                    onClick = { onAction(SchoolUiAction.SelectTab(5)) },
                    text = { Text("الشعب (${uiState.sections.size})") },
                    icon = { Icon(Icons.Default.Room, contentDescription = null) },
                    modifier = Modifier.testTag("tab_sections")
                )
                Tab(
                    selected = uiState.selectedTab == 6,
                    onClick = { onAction(SchoolUiAction.SelectTab(6)) },
                    text = { Text("المواد (${uiState.subjects.size})") },
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    modifier = Modifier.testTag("tab_subjects")
                )
            }

            when (uiState.selectedTab) {
                0 -> SchoolsListTab(
                    schools = uiState.schools,
                    activeSchoolId = uiState.activeSchoolId,
                    onSelectActive = { schoolId -> onAction(SchoolUiAction.SelectActiveSchool(schoolId)) },
                    onEditSchool = { school -> onAction(SchoolUiAction.OpenFormDialog(school)) },
                    onDeleteSchool = { schoolId -> deleteConfirmSchoolId = schoolId }
                )
                1 -> SchoolDetailsOverviewTab(
                    uiState = uiState,
                    onCategoryTabSelected = { index -> onAction(SchoolUiAction.SelectCategoryTab(index)) },
                    onAddStudentClick = { showAddStudentDialog = true },
                    onAddTeacherClick = { showAddTeacherDialog = true },
                    onAddCourseClick = { showAddCourseDialog = true },
                    onAddAssessmentClick = { showAddAssessmentDialog = true }
                )
                2 -> CategoryList(
                    items = uiState.academicYears,
                    emptyText = "لا توجد أعوام دراسية مسجلة.",
                    onAddClick = { showAddAcademicYearDialog = true },
                    addButtonText = "إضافة عام دراسي جديد",
                    itemContent = { ay ->
                        EntityItemCard(
                            title = ay.name,
                            subtitle = "من ${ay.startDate} إلى ${ay.endDate}",
                            badgeText = if (ay.isCurrent) "الحالي" else "سابق",
                            icon = Icons.Default.CalendarToday,
                            onDelete = { onAction(SchoolUiAction.DeleteAcademicYear(ay.id)) }
                        )
                    }
                )
                3 -> CategoryList(
                    items = uiState.gradeLevels,
                    emptyText = "لا توجد مراحل دراسية مضافة لهذه المدرسة.",
                    onAddClick = { showAddGradeLevelDialog = true },
                    addButtonText = "إضافة مرحلة دراسية",
                    itemContent = { gl ->
                        EntityItemCard(
                            title = gl.name,
                            subtitle = "الكود: ${gl.code} | الترتيب: ${gl.levelSequence} | المرحلة: ${gl.stage.name}",
                            badgeText = "مرحلة",
                            icon = Icons.Default.Grade,
                            onDelete = { onAction(SchoolUiAction.DeleteGradeLevel(gl.id)) }
                        )
                    }
                )
                4 -> CategoryList(
                    items = uiState.schoolClasses,
                    emptyText = "لا توجد صفوف دراسية مضافة لهذه المدرسة.",
                    onAddClick = { showAddClassDialog = true },
                    addButtonText = "إضافة صف دراسي جديد",
                    itemContent = { cls ->
                        EntityItemCard(
                            title = cls.name,
                            subtitle = "المرحلة: ${cls.gradeLevel} | القاعة: ${cls.roomNumber} | السعة: ${cls.capacity} طالب",
                            badgeText = "صف",
                            icon = Icons.Default.Class,
                            onDelete = { onAction(SchoolUiAction.DeleteSchoolClass(cls.id)) }
                        )
                    }
                )
                5 -> CategoryList(
                    items = uiState.sections,
                    emptyText = "لا توجد شعب مضافة لهذه المدرسة.",
                    onAddClick = { showAddSectionDialog = true },
                    addButtonText = "إضافة شعبة جديدة",
                    itemContent = { sec ->
                        EntityItemCard(
                            title = sec.name,
                            subtitle = "السعة القصوى: ${sec.capacity} طالب | المسجلون: ${sec.studentsCount}",
                            badgeText = "شعبة",
                            icon = Icons.Default.Room,
                            onDelete = { onAction(SchoolUiAction.DeleteSection(sec.id)) }
                        )
                    }
                )
                6 -> CategoryList(
                    items = uiState.subjects,
                    emptyText = "لا توجد مواد دراسية مضافة لهذه المدرسة.",
                    onAddClick = { showAddSubjectDialog = true },
                    addButtonText = "إضافة مادة دراسية جديدة",
                    itemContent = { sub ->
                        EntityItemCard(
                            title = sub.name,
                            subtitle = "الكود: ${sub.code} | الساعات المعتمدة: ${sub.creditHours}",
                            badgeText = "مادة",
                            icon = Icons.Default.Book,
                            onDelete = { onAction(SchoolUiAction.DeleteSubject(sub.id)) }
                        )
                    }
                )
            }
        }
    }

    // Add/Edit School Dialog
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

    // Delete Confirmation Dialog for School
    deleteConfirmSchoolId?.let { schoolId ->
        AlertDialog(
            onDismissRequest = { deleteConfirmSchoolId = null },
            title = { Text("تأكيد حذف المدرسة") },
            text = { Text("هل أنت تأكد من رغبتك في حذف هذه المدرسة كافة بياناتها المعرفة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(SchoolUiAction.DeleteSchool(schoolId))
                        deleteConfirmSchoolId = null
                    }
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmSchoolId = null }) { Text("إلغاء") }
            }
        )
    }

    // Add Academic Year Dialog
    if (showAddAcademicYearDialog) {
        AlertDialog(
            onDismissRequest = { showAddAcademicYearDialog = false },
            title = { Text("إضافة عام دراسي") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ayName,
                        onValueChange = { ayName = it },
                        label = { Text("اسم العام الدراسي (مثال: 2024-2025)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ayStartDate,
                        onValueChange = { ayStartDate = it },
                        label = { Text("تاريخ البداية (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ayEndDate,
                        onValueChange = { ayEndDate = it },
                        label = { Text("تاريخ النهاية (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ayName.isNotBlank()) {
                            onAction(
                                SchoolUiAction.SaveAcademicYear(
                                    id = null,
                                    name = ayName,
                                    startDate = ayStartDate,
                                    endDate = ayEndDate,
                                    isCurrent = true
                                )
                            )
                            ayName = ""
                            showAddAcademicYearDialog = false
                        }
                    }
                ) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAcademicYearDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Grade Level Dialog
    if (showAddGradeLevelDialog) {
        AlertDialog(
            onDismissRequest = { showAddGradeLevelDialog = false },
            title = { Text("إضافة مرحلة دراسية") },
            text = {
                Column {
                    OutlinedTextField(
                        value = gradeName,
                        onValueChange = { gradeName = it },
                        label = { Text("اسم المرحلة (مثال: الصف الأول الثانوي)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gradeCode,
                        onValueChange = { gradeCode = it },
                        label = { Text("كود المرحلة (SEC-1)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gradeSeq,
                        onValueChange = { gradeSeq = it },
                        label = { Text("الترتيب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (gradeName.isNotBlank()) {
                            onAction(
                                SchoolUiAction.SaveGradeLevel(
                                    id = null,
                                    name = gradeName,
                                    code = gradeCode.ifBlank { "GL-${gradeSeq}" },
                                    sequence = gradeSeq.toIntOrNull() ?: 1,
                                    stage = selectedStage
                                )
                            )
                            gradeName = ""
                            gradeCode = ""
                            showAddGradeLevelDialog = false
                        }
                    }
                ) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGradeLevelDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add School Class Dialog
    if (showAddClassDialog) {
        AlertDialog(
            onDismissRequest = { showAddClassDialog = false },
            title = { Text("إضافة صف دراسي جديد") },
            text = {
                Column {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("اسم الصف (مثال: 101 ثانٍ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = classGradeLevel,
                        onValueChange = { classGradeLevel = it },
                        label = { Text("المرحلة الدراسية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = classRoom,
                        onValueChange = { classRoom = it },
                        label = { Text("رقم القاعة / الغرفة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = classCapacity,
                        onValueChange = { classCapacity = it },
                        label = { Text("السعة الاستيعابية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (className.isNotBlank()) {
                            onAction(
                                SchoolUiAction.SaveSchoolClass(
                                    id = null,
                                    name = className,
                                    gradeLevel = classGradeLevel.ifBlank { "الصف الأول" },
                                    roomNumber = classRoom.ifBlank { "A-1" },
                                    capacity = classCapacity.toIntOrNull() ?: 30
                                )
                            )
                            className = ""
                            classRoom = ""
                            showAddClassDialog = false
                        }
                    }
                ) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddClassDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Section Dialog
    if (showAddSectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text("إضافة شعبة جديدة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        label = { Text("اسم الشعبة (مثال: شعبة أ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sectionCapacity,
                        onValueChange = { sectionCapacity = it },
                        label = { Text("السعة الاستيعابية للشعبة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sectionName.isNotBlank()) {
                            onAction(
                                SchoolUiAction.SaveSection(
                                    id = null,
                                    name = sectionName,
                                    capacity = sectionCapacity.toIntOrNull() ?: 25
                                )
                            )
                            sectionName = ""
                            showAddSectionDialog = false
                        }
                    }
                ) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("إضافة مادة دراسية") },
            text = {
                Column {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("اسم المادة (مثال: الرياضيات)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("كود المادة (MATH-101)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subjectCreditHours,
                        onValueChange = { subjectCreditHours = it },
                        label = { Text("الساعات المعتمده") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            onAction(
                                SchoolUiAction.SaveSubject(
                                    id = null,
                                    name = subjectName,
                                    code = subjectCode.ifBlank { "SUB-1" },
                                    creditHours = subjectCreditHours.toIntOrNull() ?: 3
                                )
                            )
                            subjectName = ""
                            subjectCode = ""
                            showAddSubjectDialog = false
                        }
                    }
                ) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Student Dialog
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
                ) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Teacher Dialog
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
                ) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTeacherDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Course Dialog
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
                ) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Assessment Dialog
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
                ) { Text("إضافة") }
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
fun SchoolDetailsOverviewTab(
    uiState: SchoolUiState,
    onCategoryTabSelected: (Int) -> Unit,
    onAddStudentClick: () -> Unit,
    onAddTeacherClick: () -> Unit,
    onAddCourseClick: () -> Unit,
    onAddAssessmentClick: () -> Unit
) {
    val categories = listOf("الطلاب", "المعلمون", "الشعب", "المواد", "الدورات", "الاختبارات")

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
                    addButtonText = "إضافة طالب جديد للمدرسة",
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
                    addButtonText = "إضافة معلم جديد للمدرسة",
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
                    emptyText = "لا يوجد شعب مضافة لهذه المدرسة.",
                    onAddClick = null,
                    itemContent = { section ->
                        EntityItemCard(
                            title = section.name,
                            subtitle = "السعة: ${section.capacity} طالب | عدد الطلاب: ${section.studentsCount}",
                            badgeText = "شعبة",
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
                    addButtonText = "إضافة دورة تدريبية للمدرسة",
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
                    addButtonText = "إضافة اختبار جديد للمدرسة",
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
    addButtonText: String = "إضافة عنصر جديد",
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
                Text(addButtonText)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDelete: (() -> Unit)? = null
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
            if (onDelete != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
