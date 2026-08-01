package com.rtiqa.feature.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rtiqa.core.design.tokens.RdsIcons
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.card.RdsCard
import com.rtiqa.core.ui.state.RdsEmptyState
import com.rtiqa.core.ui.state.RdsErrorState
import com.rtiqa.core.ui.state.RdsLoadingState

val COURSE_CATEGORIES = listOf(
    "الكل" to null,
    "ذكاء اصطناعي" to "AI",
    "برمجة" to "Programming",
    "علوم الحاسب" to "ComputerScience",
    "شبكات" to "Networking",
    "أمن المعلومات" to "Security"
)

@Composable
fun CoursesListScreen(
    viewModel: CoursesListViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("courses_list_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المقررات الدراسية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = { viewModel.onAction(CoursesListUiAction.SyncRequested) },
                modifier = Modifier.testTag("sync_courses_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "مزامنة",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { query ->
                viewModel.onAction(CoursesListUiAction.SearchQueryChanged(query))
            },
            placeholder = { Text("ابحث عن مقرر أو موضوع...") },
            leadingIcon = { Icon(RdsIcons.Search, contentDescription = "بحث") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("course_search_field"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(COURSE_CATEGORIES) { (label, categoryKey) ->
                val isSelected = uiState.selectedCategory == categoryKey
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.onAction(CoursesListUiAction.CategoryFilterSelected(categoryKey))
                    },
                    label = {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("filter_chip_${categoryKey ?: "all"}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                RdsLoadingState(message = "جاري تحميل المقررات...")
            }
            uiState.errorMessage != null -> {
                RdsErrorState(
                    message = uiState.errorMessage ?: "تعذر جلب المقررات",
                    onRetryClick = { viewModel.onAction(CoursesListUiAction.SyncRequested) }
                )
            }
            uiState.courses.isEmpty() -> {
                RdsEmptyState(
                    title = "لا توجد مقررات",
                    description = "لم يتم العثور على مقررات تطابق بحثك"
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.courses, key = { it.id }) { course ->
                        CourseListItemCard(
                            course = course,
                            onClick = {
                                viewModel.onAction(CoursesListUiAction.CourseClicked(course.id))
                                onNavigateToDetail(course.id)
                            },
                            onToggleBookmark = {
                                viewModel.onAction(
                                    CoursesListUiAction.BookmarkToggled(course.id, !course.isBookmarked)
                                )
                            },
                            onDownloadRequest = {
                                viewModel.onAction(
                                    CoursesListUiAction.DownloadCourseRequested(course.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseListItemCard(
    course: Course,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDownloadRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    RdsCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    if (course.isDownloaded) {
                        RdsBadge(text = "متاح أوفلاين", type = RdsBadgeType.OFFLINE)
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (course.isBookmarked) RdsIcons.Check else RdsIcons.List,
                            contentDescription = "محفوظات",
                            tint = if (course.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = course.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التقدم: ${(course.progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!course.isDownloaded) {
                    IconButton(onClick = onDownloadRequest) {
                        Icon(
                            imageVector = RdsIcons.Download,
                            contentDescription = "تحميل للعمل بدون إنترنت",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { course.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
