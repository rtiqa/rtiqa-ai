package com.rtiqa.feature.admin.users

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.MemberStatus
import com.rtiqa.core.domain.model.School

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    uiState: UsersUiState,
    onAction: (UsersUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSchoolMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "إدارة مستخدمي المدرسة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.activeSchool?.name ?: "المدرسة النشطة: ${uiState.activeSchoolId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    Box {
                        OutlinedButton(
                            onClick = { showSchoolMenu = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تبديل المدرسة",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        DropdownMenu(
                            expanded = showSchoolMenu,
                            onDismissRequest = { showSchoolMenu = false }
                        ) {
                            uiState.schools.forEach { school ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(school.name, fontWeight = FontWeight.SemiBold)
                                            Text("رمز: ${school.code}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        showSchoolMenu = false
                                        onAction(UsersUiAction.SelectActiveSchool(school.id))
                                    },
                                    leadingIcon = {
                                        if (school.id == uiState.activeSchoolId) {
                                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(UsersUiAction.OpenAddUserDialog) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مستخدم جديد")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Active School Info Header Banner
            ActiveSchoolBanner(
                activeSchool = uiState.activeSchool,
                usersCount = uiState.rawUsers.size
            )

            // Search Bar
            PaddingSearchField(
                query = uiState.searchQuery,
                onQueryChange = { onAction(UsersUiAction.SearchUsers(it)) }
            )

            // Roles Filter Bar
            RolesFilterChipsRow(
                selectedRole = uiState.roleFilter,
                onSelectRole = { onAction(UsersUiAction.FilterByRole(it)) }
            )

            // Stats summary row
            UserStatsSummaryRow(users = uiState.rawUsers)

            // Content List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredUsers.isEmpty()) {
                EmptyUsersView(
                    searchQuery = uiState.searchQuery,
                    roleFilter = uiState.roleFilter,
                    onClearFilter = {
                        onAction(UsersUiAction.SearchUsers(""))
                        onAction(UsersUiAction.FilterByRole(null))
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.filteredUsers,
                        key = { it.id }
                    ) { user ->
                        UserCardItem(
                            user = user,
                            onCardClick = { onAction(UsersUiAction.OpenUserDetails(user)) },
                            onEditClick = { onAction(UsersUiAction.OpenEditUserDialog(user)) },
                            onDeleteClick = { onAction(UsersUiAction.DeleteUser(user.id)) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (uiState.isAddEditOpen) {
        AddEditUserDialog(
            user = uiState.editingUser,
            activeSchoolName = uiState.activeSchool?.name ?: uiState.activeSchoolId,
            onDismiss = { onAction(UsersUiAction.CloseAddEditDialog) },
            onSave = { name, email, role, department, phone, status ->
                onAction(
                    UsersUiAction.SaveUser(
                        id = uiState.editingUser?.id,
                        name = name,
                        email = email,
                        role = role,
                        department = department,
                        phone = phone,
                        status = status
                    )
                )
            }
        )
    }

    // Details Dialog
    if (uiState.isDetailsOpen && uiState.selectedUserForDetails != null) {
        UserDetailsDialog(
            user = uiState.selectedUserForDetails,
            schoolName = uiState.activeSchool?.name ?: uiState.activeSchoolId,
            onDismiss = { onAction(UsersUiAction.CloseUserDetails) },
            onEdit = {
                onAction(UsersUiAction.OpenEditUserDialog(uiState.selectedUserForDetails))
            },
            onDelete = {
                onAction(UsersUiAction.DeleteUser(uiState.selectedUserForDetails.id))
            }
        )
    }
}

@Composable
fun ActiveSchoolBanner(
    activeSchool: School?,
    usersCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    text = activeSchool?.name ?: "المدرسة النشطة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "رمز المدرسة: ${activeSchool?.code ?: "-"} | إجمالي المستخدمين المسجلين: $usersCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PaddingSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("بحث عن مستخدم بالاسم، البريد، الهاتف...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
fun RolesFilterChipsRow(
    selectedRole: EnterpriseRole?,
    onSelectRole: (EnterpriseRole?) -> Unit
) {
    val roles = listOf(
        null to "الكل",
        EnterpriseRole.PRINCIPAL to EnterpriseRole.PRINCIPAL.labelAr,
        EnterpriseRole.VICE_PRINCIPAL to EnterpriseRole.VICE_PRINCIPAL.labelAr,
        EnterpriseRole.TEACHER to EnterpriseRole.TEACHER.labelAr,
        EnterpriseRole.STUDENT to EnterpriseRole.STUDENT.labelAr,
        EnterpriseRole.PARENT to EnterpriseRole.PARENT.labelAr
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(roles) { (role, label) ->
            val isSelected = selectedRole == role
            FilterChip(
                selected = isSelected,
                onClick = { onSelectRole(role) },
                label = { Text(label) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun UserStatsSummaryRow(users: List<EnterpriseMember>) {
    val principalCount = users.count { it.role == EnterpriseRole.PRINCIPAL }
    val vicePrincipalCount = users.count { it.role == EnterpriseRole.VICE_PRINCIPAL }
    val teacherCount = users.count { it.role == EnterpriseRole.TEACHER }
    val studentCount = users.count { it.role == EnterpriseRole.STUDENT }
    val parentCount = users.count { it.role == EnterpriseRole.PARENT }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatBadge(label = "مدير", count = principalCount, color = MaterialTheme.colorScheme.primary)
        StatBadge(label = "وكيل", count = vicePrincipalCount, color = MaterialTheme.colorScheme.secondary)
        StatBadge(label = "معلم", count = teacherCount, color = MaterialTheme.colorScheme.tertiary)
        StatBadge(label = "طالب", count = studentCount, color = Color(0xFF2E7D32))
        StatBadge(label = "ولي أمر", count = parentCount, color = Color(0xFFE65100))
    }
}

@Composable
fun StatBadge(label: String, count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $count",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UserCardItem(
    user: EnterpriseMember,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val roleColor = getRoleColor(user.role)
    val roleIcon = getRoleIcon(user.role)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(roleColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = roleIcon,
                    contentDescription = null,
                    tint = roleColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RoleBadge(role = user.role)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (user.department.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Grade,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.department,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Buttons
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
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

@Composable
fun RoleBadge(role: EnterpriseRole) {
    val color = getRoleColor(role)
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = role.labelAr,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyUsersView(
    searchQuery: String,
    roleFilter: EnterpriseRole?,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isNotEmpty() || roleFilter != null) "لا توجد نتائج مطابقة للبحث أو التصفية" else "لا يوجد مستخدمون مسجلون في هذه المدرسة حتى الآن",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (searchQuery.isNotEmpty() || roleFilter != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onClearFilter) {
                Text("إعادة ضبط البحث والتصفية")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUserDialog(
    user: EnterpriseMember?,
    activeSchoolName: String,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, role: EnterpriseRole, department: String, phone: String, status: MemberStatus) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var department by remember { mutableStateOf(user?.department ?: "") }
    var role by remember { mutableStateOf(user?.role ?: EnterpriseRole.STUDENT) }
    var status by remember { mutableStateOf(user?.status ?: MemberStatus.ACTIVE) }

    var roleExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val rolesList = listOf(
        EnterpriseRole.PRINCIPAL,
        EnterpriseRole.VICE_PRINCIPAL,
        EnterpriseRole.TEACHER,
        EnterpriseRole.STUDENT,
        EnterpriseRole.PARENT
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (user == null) "إضافة مستخدم جديد" else "تعديل بيانات المستخدم",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Banner
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "المدرسة النشطة: $activeSchoolName",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("القسم / الصف الدراسي") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Dropdown Selector
                Box {
                    OutlinedButton(
                        onClick = { roleExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("الدور: ${role.labelAr}")
                    }
                    DropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        rolesList.forEach { itemRole ->
                            DropdownMenuItem(
                                text = { Text(itemRole.labelAr) },
                                onClick = {
                                    role = itemRole
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Status Selector
                Box {
                    OutlinedButton(
                        onClick = { statusExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("الحالة: ${status.labelAr}")
                    }
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        MemberStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.labelAr) },
                                onClick = {
                                    status = st
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onSave(name, email, role, department, phone, status)
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text(if (user == null) "إضافة" else "حفظ")
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
fun UserDetailsDialog(
    user: EnterpriseMember,
    schoolName: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val roleColor = getRoleColor(user.role)
    val roleIcon = getRoleIcon(user.role)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = roleIcon, contentDescription = null, tint = roleColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    RoleBadge(role = user.role)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailItemRow(icon = Icons.Default.Email, label = "البريد الإلكتروني", value = user.email)
                DetailItemRow(icon = Icons.Default.Phone, label = "رقم الهاتف", value = user.phone.ifEmpty { "غير محدد" })
                DetailItemRow(icon = Icons.Default.Grade, label = "القسم / الصف", value = user.department.ifEmpty { "عام" })
                DetailItemRow(icon = Icons.Default.School, label = "المدرسة النشطة", value = schoolName)
                DetailItemRow(icon = Icons.Default.Badge, label = "معرف المستخدم", value = user.id)
                DetailItemRow(icon = Icons.Default.Info, label = "حالة الحساب", value = user.status.labelAr)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun DetailItemRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun getRoleColor(role: EnterpriseRole): Color {
    return when (role) {
        EnterpriseRole.PRINCIPAL -> Color(0xFF673AB7) // Deep Purple
        EnterpriseRole.VICE_PRINCIPAL -> Color(0xFF3F51B5) // Indigo
        EnterpriseRole.TEACHER -> Color(0xFF00897B) // Teal
        EnterpriseRole.STUDENT -> Color(0xFF2E7D32) // Green
        EnterpriseRole.PARENT -> Color(0xFFE65100) // Deep Orange
        else -> Color(0xFF607D8B) // Blue Grey
    }
}

fun getRoleIcon(role: EnterpriseRole): ImageVector {
    return when (role) {
        EnterpriseRole.PRINCIPAL -> Icons.Default.SupervisorAccount
        EnterpriseRole.VICE_PRINCIPAL -> Icons.Default.Badge
        EnterpriseRole.TEACHER -> Icons.Default.Person
        EnterpriseRole.STUDENT -> Icons.Default.School
        EnterpriseRole.PARENT -> Icons.Default.Group
        else -> Icons.Default.Person
    }
}
