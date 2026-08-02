package com.rtiqa.feature.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.OrgType
import com.rtiqa.core.domain.model.Organization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    uiState: AdminDashboardUiState,
    onAction: (AdminDashboardUiAction) -> Unit,
    onBack: () -> Unit,
    onNavigateToAcademicPlatform: () -> Unit = {},
    onNavigateToSchools: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddOrgDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var newCourseTitle by remember { mutableStateOf("") }
    var newCourseDesc by remember { mutableStateOf("") }
    var newCourseCategory by remember { mutableStateOf("الذكاء الاصطناعي") }

    var newOrgName by remember { mutableStateOf("") }
    var newOrgCode by remember { mutableStateOf("") }
    var newOrgType by remember { mutableStateOf(OrgType.UNIVERSITY) }

    var newMemberName by remember { mutableStateOf("") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberDept by remember { mutableStateOf("علوم الحاسب") }
    var newMemberRole by remember { mutableStateOf(EnterpriseRole.TEACHER) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "منصة المؤسسات والتحكم الأكاديمي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSchools) {
                        Icon(Icons.Default.Business, contentDescription = "إدارة المدارس")
                    }
                    IconButton(onClick = onNavigateToAcademicPlatform) {
                        Icon(Icons.Default.School, contentDescription = "المنظومة الأكاديمية")
                    }
                    IconButton(onClick = { onAction(AdminDashboardUiAction.RefreshMetrics) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (uiState.selectedTab) {
                        0 -> showAddCourseDialog = true
                        1 -> showAddOrgDialog = true
                        3 -> showAddMemberDialog = true
                        else -> showAddCourseDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Enterprise Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { onAction(AdminDashboardUiAction.SelectTab(0)) },
                    text = { Text("نظرة عامة والدورات", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { onAction(AdminDashboardUiAction.SelectTab(1)) },
                    text = { Text("المؤسسات التعليمية (${uiState.organizations.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Business, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { onAction(AdminDashboardUiAction.SelectTab(2)) },
                    text = { Text("الهيكل الأكاديمي والفروع", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.School, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 3,
                    onClick = { onAction(AdminDashboardUiAction.SelectTab(3)) },
                    text = { Text("الأعضاء والصلاحيات (${uiState.members.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (uiState.selectedTab) {
                    0 -> OverviewTabContent(
                        uiState = uiState,
                        onAction = onAction,
                        onNavigateToAcademicPlatform = onNavigateToAcademicPlatform,
                        onNavigateToSchools = onNavigateToSchools,
                        onNavigateToUsers = onNavigateToUsers,
                        onNavigateToClasses = onNavigateToClasses
                    )
                    1 -> OrganizationsTabContent(uiState, onAction) { showAddOrgDialog = true }
                    2 -> AcademicStructureTabContent(uiState, onAction)
                    3 -> MembersTabContent(uiState, onAction) { showAddMemberDialog = true }
                }
            }
        }
    }

    // Create Course Dialog
    if (showAddCourseDialog) {
        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text(text = "إضافة دورة جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCourseTitle,
                        onValueChange = { newCourseTitle = it },
                        label = { Text("عنوان الدورة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCourseCategory,
                        onValueChange = { newCourseCategory = it },
                        label = { Text("التصنيف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCourseDesc,
                        onValueChange = { newCourseDesc = it },
                        label = { Text("الوصف المختصر") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(
                            AdminDashboardUiAction.CreateCourse(
                                title = newCourseTitle,
                                description = newCourseDesc,
                                category = newCourseCategory
                            )
                        )
                        newCourseTitle = ""
                        newCourseDesc = ""
                        showAddCourseDialog = false
                    }
                ) {
                    Text("حفظ الدورة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Create Organization Dialog
    if (showAddOrgDialog) {
        AlertDialog(
            onDismissRequest = { showAddOrgDialog = false },
            title = { Text(text = "إضافة مؤسسة تعليمية جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newOrgName,
                        onValueChange = { newOrgName = it },
                        label = { Text("اسم المؤسسة (جامعة / مدرسة / معهد)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newOrgCode,
                        onValueChange = { newOrgCode = it },
                        label = { Text("رمز المؤسسة (مثال: RTQ-UNIV)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("نوع المؤسسة:", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrgType.values().forEach { type ->
                            TextButton(
                                onClick = { newOrgType = type },
                                modifier = Modifier.background(
                                    if (newOrgType == type) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                            ) {
                                Text(
                                    text = when (type) {
                                        OrgType.SCHOOL -> "مدرسة"
                                        OrgType.UNIVERSITY -> "جامعة"
                                        OrgType.INSTITUTE -> "معهد"
                                        OrgType.ACADEMY -> "أكاديمية"
                                    },
                                    color = if (newOrgType == type) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(AdminDashboardUiAction.CreateOrganization(newOrgName, newOrgType, newOrgCode))
                        newOrgName = ""
                        newOrgCode = ""
                        showAddOrgDialog = false
                    }
                ) {
                    Text("حفظ المؤسسة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOrgDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Create Member Dialog
    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text(text = "إضافة عضو وتحديد الصلاحية (RBAC)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("اسم العضو") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMemberDept,
                        onValueChange = { newMemberDept = it },
                        label = { Text("القسم الأكاديمي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("الصلاحية والدور:", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        EnterpriseRole.values().take(4).forEach { role ->
                            TextButton(
                                onClick = { newMemberRole = role },
                                modifier = Modifier.background(
                                    if (newMemberRole == role) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(6.dp)
                                )
                            ) {
                                Text(
                                    text = when (role) {
                                        EnterpriseRole.SUPER_ADMIN -> "مدير عام"
                                        EnterpriseRole.ORG_ADMIN -> "مدير مؤسسة"
                                        EnterpriseRole.PRINCIPAL -> "مدير مدرسة"
                                        EnterpriseRole.VICE_PRINCIPAL -> "وكيل مدرسة"
                                        EnterpriseRole.TEACHER -> "معلم"
                                        EnterpriseRole.STUDENT -> "طالب"
                                        EnterpriseRole.PARENT -> "ولي أمر"
                                        EnterpriseRole.STAFF -> "موظف"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(AdminDashboardUiAction.CreateMember(newMemberName, newMemberEmail, newMemberRole, newMemberDept))
                        newMemberName = ""
                        newMemberEmail = ""
                        showAddMemberDialog = false
                    }
                ) {
                    Text("حفظ العضو")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun OverviewTabContent(
    uiState: AdminDashboardUiState,
    onAction: (AdminDashboardUiAction) -> Unit,
    onNavigateToAcademicPlatform: () -> Unit = {},
    onNavigateToSchools: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الوصول السريع والإدارة",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSchools() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("إدارة المدارس", fontWeight = FontWeight.Bold)
                        Text("التحكم بالمدارس النشطة", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToUsers() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("مستخدمو المدرسة", fontWeight = FontWeight.Bold)
                        Text("مدير، وكيل، معلم، طالب، ولي أمر", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToClasses() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("صفوف المدرسة", fontWeight = FontWeight.Bold)
                        Text("الفصول والشعب والسعة", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "المؤسسات التعليمية",
                    value = "${uiState.organizations.size}",
                    icon = Icons.Default.Business,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "الدورات المتاحة",
                    value = "${uiState.totalCoursesCount}",
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "الفروع الأكاديمية",
                    value = "${uiState.branches.size}",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "إجمالي الكادر والطلاب",
                    value = "${uiState.members.size + 1500}",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI Builder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "منشئ المناهج الذكي من رتقاء",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "توليد الخطة الدراسية وتوصيف المواد والمحتوى بذكاء Gemini الاصطناعي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "قائمة الدورات المعتمدة (${uiState.courses.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.courses, key = { it.id }) { course ->
            AdminCourseRow(
                course = course,
                onDelete = { onAction(AdminDashboardUiAction.DeleteCourseRequested(course.id)) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun OrganizationsTabContent(
    uiState: AdminDashboardUiState,
    onAction: (AdminDashboardUiAction) -> Unit,
    onAddOrgClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إدارة تعدد المؤسسات (Multi-Tenant)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onAddOrgClick) {
                    Text("إضافة مؤسسة")
                }
            }
        }

        items(uiState.organizations, key = { it.id }) { org ->
            OrganizationCard(
                organization = org,
                isSelected = org.id == uiState.selectedOrgId,
                onSelect = { onAction(AdminDashboardUiAction.SelectOrganization(org.id)) },
                onDelete = { onAction(AdminDashboardUiAction.DeleteOrganizationRequested(org.id)) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun AcademicStructureTabContent(
    uiState: AdminDashboardUiState,
    onAction: (AdminDashboardUiAction) -> Unit
) {
    var branchName by remember { mutableStateOf("") }
    var deptName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "إدارة الفروع والأقسام والسنوات الدراسية",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("إضافة فرع جديد للمؤسسة الحالية", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("اسم الفرع (مثال: الفرع الشمالي)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onAction(AdminDashboardUiAction.CreateBranch(branchName, "BR-NEW", "الرياض", "011000111"))
                            branchName = ""
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("حفظ الفرع")
                    }
                }
            }
        }

        item {
            Text("الفروع المسجلة (${uiState.branches.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(uiState.branches, key = { it.id }) { branch ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = branch.name, fontWeight = FontWeight.Bold)
                    Text(text = "الكود: ${branch.code} • العنوان: ${branch.address}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun MembersTabContent(
    uiState: AdminDashboardUiState,
    onAction: (AdminDashboardUiAction) -> Unit,
    onAddMemberClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إدارة الأعضاء والصلاحيات (RBAC)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onAddMemberClick) {
                    Text("إضافة عضو")
                }
            }
        }

        items(uiState.members, key = { it.id }) { member ->
            MemberCard(
                member = member,
                onDelete = { onAction(AdminDashboardUiAction.DeleteMemberRequested(member.id)) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun OrganizationCard(
    organization: Organization,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = organization.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "الرمز: ${organization.code} • الفروع: ${organization.branchesCount} • الطلاب: ${organization.studentsCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: EnterpriseMember,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "${member.email} • ${member.department}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "الدور: ${member.role.name} • الحالة: ${member.status.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminCourseRow(
    course: Course,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${course.category} • ${course.totalLessons} دروس",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف الدورة",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

