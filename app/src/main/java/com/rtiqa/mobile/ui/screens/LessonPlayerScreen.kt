package com.rtiqa.mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.mobile.domain.model.Lesson

import androidx.compose.ui.res.stringResource
import com.rtiqa.mobile.R

@Composable
fun LessonPlayerScreen(
    lesson: Lesson?,
    onBack: () -> Unit,
    onToggleComplete: (Boolean) -> Unit,
    onAskAiAboutLesson: (String) -> Unit,
    onNextLesson: (() -> Unit)? = null,
    onSaveProgress: ((Float) -> Unit)? = null,
    onStartQuiz: (() -> Unit)? = null,
    hasNextLesson: Boolean = true,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (lesson == null) return

    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    val isQuizLocked = lesson.hasQuiz && !lesson.isQuizPassed

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("lesson_player_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isArabic) lesson.titleAr else lesson.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Player Simulator Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            isPlaying = !isPlaying
                            if (isPlaying) {
                                onSaveProgress?.invoke(0.75f)
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل الدرس",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isPlaying) stringResource(R.string.playing_stream_offline) else stringResource(R.string.tap_to_play),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quiz Callout Banner if lesson has a quiz
        if (lesson.hasQuiz || onStartQuiz != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (lesson.isQuizPassed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = "اختبار",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "اختبار الدرس التقييمي" else "Lesson Quiz",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lesson.isQuizPassed)
                                (if (isArabic) "تم اجتياز الاختبار بنجاح 🎉" else "Quiz Passed Successfully! 🎉")
                            else
                                (if (isArabic) "يجب إنهاء الاختبار للانتقال للدرس التالي" else "Pass quiz to unlock next lesson"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { onStartQuiz?.invoke() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lesson.isQuizPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.testTag("start_lesson_quiz_button")
                    ) {
                        Text(
                            text = if (lesson.isQuizPassed) (if (isArabic) "إعادة الاختبار" else "Retake Quiz")
                            else (if (isArabic) "ابدأ الاختبار" else "Start Quiz"),
                            fontSize = 13.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val newStatus = !lesson.isCompleted
                    onToggleComplete(newStatus)
                    if (newStatus) {
                        onSaveProgress?.invoke(1.0f)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("complete_lesson_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "إكمال",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (lesson.isCompleted) 
                        stringResource(R.string.completed_check)
                    else 
                        stringResource(R.string.mark_complete)
                )
            }

            if (onNextLesson != null && hasNextLesson) {
                Button(
                    onClick = {
                        if (isQuizLocked) {
                            Toast.makeText(
                                context,
                                if (isArabic) "لا يمكن الانتقال للدرس التالي إلا بعد إنهاء واجتياز اختبار هذا الدرس"
                                else "You must pass this lesson's quiz before proceeding to the next lesson",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onNextLesson()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isQuizLocked) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f) else MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("next_lesson_button")
                ) {
                    if (isQuizLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "مغلق",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(if (isArabic) "الدرس التالي ➔" else "Next Lesson ➔")
                }
            }

            OutlinedButton(
                onClick = {
                    val prompt = "اشرح المفاهيم الرئيسية لدرس '${if (isArabic) lesson.titleAr else lesson.title}' بوضوح"
                    onAskAiAboutLesson(prompt)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("ai_summary_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "الملخص الذكي",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.ai_summary))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.lesson_content_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) lesson.contentMarkdownAr else lesson.contentMarkdown,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
