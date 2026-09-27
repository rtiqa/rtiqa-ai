package com.rtiqa.feature.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.design.tokens.RdsIcons
import com.rtiqa.core.domain.model.Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailsScreen(
    uiState: LessonViewerUiState,
    onAction: (LessonViewerUiAction) -> Unit,
    onBack: () -> Unit,
    onStartQuiz: () -> Unit,
    onNavigateToLesson: (String) -> Unit = {},
    isQuizPassed: Boolean = false,
    durationMinutes: Int? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.lesson?.title ?: "تفاصيل الدرس",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (uiState.courseId.isNotBlank()) {
                            Text(
                                text = "المقرر: ${uiState.courseId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(RdsIcons.Back, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val isCompleted = uiState.isCompleted || (uiState.lesson?.isCompleted == true) || uiState.progressPercent >= 1.0f
            val isInProgress = !isCompleted && uiState.progressPercent > 0f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // شارة رقم الدرس واسم المقرر
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "الدرس ${uiState.lesson?.order ?: 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (uiState.courseId.isNotBlank()) {
                        Text(
                            text = "المقرر: ${uiState.courseId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // عنوان الدرس الرئيسي
                Text(
                    text = uiState.lesson?.title ?: "عنوان الدرس غير متوفر",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // صف بطاقات الحالة والمعلومات (الحالة / المدة / المورد الصوتي)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // شارة حالة التقدم (غير مكتمل / قيد التقدم / مكتمل)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.primaryContainer
                            isInProgress -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.testTag("lesson_status_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isCompleted -> RdsIcons.Success
                                    isInProgress -> RdsIcons.Info
                                    else -> RdsIcons.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when {
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    isInProgress -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isCompleted -> "الحالة: مكتمل"
                                    isInProgress -> "الحالة: قيد التقدم (${(uiState.progressPercent * 100).toInt()}%)"
                                    else -> "الحالة: غير مكتمل"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isInProgress -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // مدة الدرس المقدرة
                    val displayDuration = when {
                        durationMinutes != null && durationMinutes > 0 -> "$durationMinutes دقيقة"
                        else -> {
                            val wordCount = uiState.lesson?.content?.split("\\s+".toRegex())?.size ?: 0
                            val estMin = (wordCount / 30).coerceIn(2, 15)
                            "$estMin د قراءة"
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ $displayDuration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // شارة المورد الصوتي إذا وُجد
                    if (!uiState.lesson?.audioUrl.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "🎙️ صوتي",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // مؤشر تقدم الدرس أثناء المتابعة
                if (isInProgress) {
                    LinearProgressIndicator(
                        progress = { uiState.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .padding(bottom = 12.dp)
                    )
                }

                // مشغل المورد الصوتي للدرس باستخدام AndroidX Media3 / ExoPlayer
                if (!uiState.lesson?.audioUrl.isNullOrBlank()) {
                    LessonAudioPlayer(
                        audioUrl = uiState.lesson?.audioUrl,
                        title = "مقطع صوتي مصاحب للدرس"
                    )
                }

                // حاوية محتوى وشرح الدرس المنظم والمريح للقراءة
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = RdsIcons.Courses,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "شرح ومحتوى الدرس",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        LessonContentRenderer(content = uiState.lesson?.content)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // زر إكمال الدرس
                Button(
                    onClick = {
                        onAction(LessonViewerUiAction.MarkLessonCompleteClicked)
                    },
                    enabled = !isCompleted && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("complete_lesson_button")
                ) {
                    if (isCompleted) {
                        Icon(RdsIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تم إكمال الدرس ✓")
                    } else {
                        Icon(RdsIcons.Success, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إكمال الدرس")
                    }
                }

                // تجربة نهاية الدرس: إذا كان هذا آخر درس في الدورة
                if (uiState.nextLesson == null && !uiState.isLoading) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("last_lesson_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = RdsIcons.Success,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎉 هذا هو آخر درس في المقرر! أحسنت إكمال جميع الدروس.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // أزرار التنقل بين الدروس (السابق والتالي) المبنية على بيانات الدورة الحقيقية
                if (uiState.prevLesson != null || uiState.nextLesson != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.prevLesson != null) {
                            OutlinedButton(
                                onClick = { onNavigateToLesson(uiState.prevLesson.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prev_lesson_button")
                            ) {
                                Icon(
                                    RdsIcons.Back,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الدرس السابق", maxLines = 1)
                            }
                        }
                        if (uiState.nextLesson != null) {
                            Button(
                                onClick = { onNavigateToLesson(uiState.nextLesson.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("next_lesson_button")
                            ) {
                                Text("الدرس التالي", maxLines = 1)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    RdsIcons.Forward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // زر الاختبار
                Button(
                    onClick = onStartQuiz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("start_quiz_button"),
                    colors = if (isQuizPassed) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    if (isQuizPassed) {
                        Icon(RdsIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة الاختبار 🎯")
                    } else {
                        Icon(RdsIcons.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ابدأ الاختبار")
                    }
                }
            }
        }
    }
}

/**
 * منسق محتوى الدرس لتقديم نصوص وشروحات منسقة ومريحة للقراءة باللغة العربية مع دعم العناوين والنقاط
 */
@Composable
private fun LessonContentRenderer(content: String?) {
    if (content.isNullOrBlank()) {
        Text(
            text = "لا يوجد محتوى نصي لهذا الدرس حالياً.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val lines = content.lines()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("#") -> {
                    val headingText = trimmed.replace(Regex("^#+\\s*"), "").trim()
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = headingText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•") -> {
                    val bulletText = trimmed.replace(Regex("^[-*•]\\s*"), "").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = bulletText,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    Text(
                        text = trimmed,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
