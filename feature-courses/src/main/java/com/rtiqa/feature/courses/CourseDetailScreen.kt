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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rtiqa.core.design.tokens.RdsIcons
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.button.RdsOutlinedButton
import com.rtiqa.core.ui.button.RdsPrimaryButton
import com.rtiqa.core.ui.card.RdsCard
import com.rtiqa.core.ui.state.RdsErrorState
import com.rtiqa.core.ui.state.RdsLoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel,
    onBackClick: () -> Unit,
    onNavigateToLesson: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.onAction(CourseDetailUiAction.LoadCourseDetail(courseId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.course?.title ?: "تفاصيل المقرر",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(RdsIcons.Back, contentDescription = "رجوع")
                    }
                },
                actions = {
                    uiState.course?.let { course ->
                        IconButton(onClick = { viewModel.onAction(CourseDetailUiAction.BookmarkToggled) }) {
                            Icon(
                                imageVector = if (course.isBookmarked) RdsIcons.Check else RdsIcons.List,
                                contentDescription = "علامة مرجعية",
                                tint = if (course.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier.testTag("course_detail_screen")
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                RdsLoadingState(
                    message = "جاري تحميل تفاصيل المقرر...",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            uiState.errorMessage != null -> {
                RdsErrorState(
                    message = uiState.errorMessage ?: "حدث خطأ أثناء تحميل المقرر",
                    onRetryClick = { viewModel.onAction(CourseDetailUiAction.LoadCourseDetail(courseId)) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            uiState.course != null -> {
                val course = uiState.course!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        RdsCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RdsBadge(
                                        text = course.category,
                                        type = RdsBadgeType.INFO
                                    )
                                    if (course.isDownloaded) {
                                        RdsBadge(
                                            text = "محمل لعدم الاتصال",
                                            type = RdsBadgeType.OFFLINE
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = course.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = course.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "نسبة الإنجاز: ${(course.progressPercent * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${uiState.lessons.size} دروس",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { course.progressPercent },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!course.isEnrolled) {
                                        RdsPrimaryButton(
                                            text = "التسجيل في المقرر",
                                            onClick = { viewModel.onAction(CourseDetailUiAction.EnrollClicked) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    RdsOutlinedButton(
                                        text = if (course.isDownloaded) "محمل" else "تحميل أوفلاين",
                                        onClick = { viewModel.onAction(CourseDetailUiAction.DownloadCourseClicked) },
                                        enabled = !course.isDownloaded,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                RdsPrimaryButton(
                                    text = "بدء اختبار المقرر",
                                    onClick = {
                                        viewModel.onAction(CourseDetailUiAction.StartQuizClicked)
                                        onNavigateToQuiz(course.id)
                                    },
                                    leadingIcon = RdsIcons.Quiz,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "دروس المقرر",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(uiState.lessons, key = { it.id }) { lesson ->
                        LessonItemCard(
                            lesson = lesson,
                            onClick = {
                                viewModel.onAction(CourseDetailUiAction.LessonClicked(lesson.id))
                                onNavigateToLesson(lesson.id)
                            },
                            onToggleComplete = {
                                viewModel.onAction(CourseDetailUiAction.MarkLessonCompleted(lesson.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonItemCard(
    lesson: Lesson,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    RdsCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onToggleComplete) {
                    Icon(
                        imageVector = if (lesson.isCompleted) RdsIcons.Success else RdsIcons.Check,
                        contentDescription = "حالة الدرس",
                        tint = if (lesson.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "الدرس ${lesson.order}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = RdsIcons.Forward,
                contentDescription = "بدء",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
