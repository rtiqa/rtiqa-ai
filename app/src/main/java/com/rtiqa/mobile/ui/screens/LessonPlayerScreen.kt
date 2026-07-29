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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
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
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (lesson == null) return

    var isPlaying by remember { mutableStateOf(false) }

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
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                        onClick = { isPlaying = !isPlaying },
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onToggleComplete(!lesson.isCompleted) },
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
