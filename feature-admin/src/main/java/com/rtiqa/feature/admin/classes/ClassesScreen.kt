package com.rtiqa.feature.admin.classes

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.domain.model.SchoolClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    uiState: ClassesUiState,
    onAction: (ClassesUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSchoolDropdown by remember { mutableStateOf(false) }
    var classToDelete by remember { mutableStateOf<SchoolClass?>(null) }

    LaunchedEffect(Unit) {
        // Collect single events if any
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "إدارة الصفوف الدراسية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.activeSchool?.let { school ->
                            Text(
                                text = "المدرسة النشطة: ${school.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("classes_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(ClassesUiAction.OpenAddClass) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_class_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة صف")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active School Selector Banner
            ActiveSchoolSelector(
                schools = uiState.schools,
                activeSchoolId = uiState.activeSchoolId,
                activeSchoolName = uiState.activeSchool?.name ?: "اختر مدرسة",
                isExpanded = showSchoolDropdown,
                onToggleDropdown = { showSchoolDropdown = !showSchoolDropdown },
                onSelectSchool = { schoolId ->
                    showSchoolDropdown = false
                    onAction(ClassesUiAction.SelectActiveSchool(schoolId))
                }
            )

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onAction(ClassesUiAction.SearchClasses(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("classes_search_input"),
                placeholder = { Text("بحث باسم الصف، المرحلة، أو القاعة...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "بحث")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onAction(ClassesUiAction.SearchClasses("")) }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Grade Level Filter Chips
            GradeLevelFilters(
                selectedGrade = uiState.selectedGradeFilter,
                onSelectGrade = { onAction(ClassesUiAction.FilterByGrade(it)) }
            )

            // Content List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة للبحث" else "لا توجد صفوف دراسية في هذه المدرسة",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = uiState.filteredClasses,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        ClassItemCard(
                            schoolClass = item,
                            isFirst = index == 0,
                            isLast = index == uiState.filteredClasses.size - 1,
                            onClick = { onAction(ClassesUiAction.OpenClassDetails(item)) },
                            onEdit = { onAction(ClassesUiAction.OpenEditClass(item)) },
                            onDelete = { classToDelete = item },
                            onMoveUp = { onAction(ClassesUiAction.MoveClassUp(item)) },
                            onMoveDown = { onAction(ClassesUiAction.MoveClassDown(item)) }
                        )
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (uiState.isAddEditOpen) {
            AddEditClassScreen(
                schoolClass = uiState.selectedClassForEdit,
                errorMessage = uiState.errorMessage,
                onDismiss = { onAction(ClassesUiAction.CloseDialogs) },
                onSave = { name, grade, section, capacity, room ->
                    onAction(
                        ClassesUiAction.SaveClass(
                            id = uiState.selectedClassForEdit?.id,
                            name = name,
                            gradeLevel = grade,
                            sectionName = section,
                            capacity = capacity,
                            roomNumber = room
                        )
                    )
                }
            )
        }

        // Details Dialog
        if (uiState.isDetailsOpen && uiState.selectedClassForDetails != null) {
            ClassDetailsScreen(
                schoolClass = uiState.selectedClassForDetails,
                activeSchoolName = uiState.activeSchool?.name,
                onDismiss = { onAction(ClassesUiAction.CloseDialogs) }
            )
        }

        // Delete Confirmation Dialog
        classToDelete?.let { targetClass ->
            AlertDialog(
                onDismissRequest = { classToDelete = null },
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت تأكد من رغبتك في حذف الصف (${targetClass.name})؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            onAction(ClassesUiAction.DeleteClass(targetClass.id))
                            classToDelete = null
                        },
                        modifier = Modifier.testTag("confirm_delete_class_button")
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { classToDelete = null }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

@Composable
private fun ActiveSchoolSelector(
    schools: List<com.rtiqa.core.domain.model.School>,
    activeSchoolId: String,
    activeSchoolName: String,
    isExpanded: Boolean,
    onToggleDropdown: () -> Unit,
    onSelectSchool: (String) -> Unit
) {
    Surface(
        onClick = onToggleDropdown,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_school_selector_chip")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "المدرسة النشطة الحالية",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeSchoolName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "تغيير المدرسة",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )

                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = onToggleDropdown
                ) {
                    schools.forEach { school ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = school.name,
                                    fontWeight = if (school.id == activeSchoolId) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { onSelectSchool(school.id) },
                            modifier = Modifier.testTag("school_option_${school.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeLevelFilters(
    selectedGrade: String?,
    onSelectGrade: (String?) -> Unit
) {
    val grades = listOf("الكل", "الابتدائي", "المتوسط", "الثانوي")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(grades) { grade ->
            val isSelected = if (grade == "الكل") selectedGrade == null else selectedGrade == grade
            FilterChip(
                selected = isSelected,
                onClick = {
                    onSelectGrade(if (grade == "الكل") null else grade)
                },
                label = { Text(grade) },
                modifier = Modifier.testTag("filter_chip_$grade")
            )
        }
    }
}

@Composable
private fun ClassItemCard(
    schoolClass: SchoolClass,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("class_item_${schoolClass.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${schoolClass.displayOrder}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = schoolClass.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (schoolClass.gradeLevel.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = schoolClass.gradeLevel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (schoolClass.roomNumber.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "قاعة ${schoolClass.roomNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${schoolClass.studentsCount}/${schoolClass.capacity} طالب",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reorder controls
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "رفع الصف للأعلى",
                        tint = if (!isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "إنزال الصف للأسفل",
                        tint = if (!isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_class_${schoolClass.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_class_${schoolClass.id}")
                ) {
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
