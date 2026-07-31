package com.rtiqa.feature.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.domain.model.Question
import com.rtiqa.core.domain.model.QuestionType
import com.rtiqa.core.ui.card.RdsCard

object QuizRoutes {
    const val QUIZ = "quiz/play/{courseId}"
    const val QUIZ_LIST = "quiz/list/{courseId}"
}

@Composable
fun QuizTimerHeader(
    timeLeftSeconds: Int,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val isWarning = timeLeftSeconds < 60

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = "سؤال ${currentQuestionIndex + 1} من $totalQuestions",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isWarning) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "الوقت المتبقي",
                    tint = if (isWarning) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isWarning) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuestionTypeChip(
    type: QuestionType,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, textColor) = when (type) {
        QuestionType.MULTIPLE_CHOICE, QuestionType.MCQ -> Triple("اختيار من متعدد", Color(0xFFEFF6FF), Color(0xFF1D4ED8))
        QuestionType.TRUE_FALSE -> Triple("صح / خطأ", Color(0xFFF0FDF4), Color(0xFF15803D))
        else -> Triple("سؤال برمجي", Color(0xFFFEF3C7), Color(0xFFB45309))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun QuizQuestionFoundation(
    question: Question,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    isSubmitted: Boolean = false,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val questionText = if (isArabic && !question.textAr.isNullOrBlank()) question.textAr!! else question.text
    val optionsList = if (isArabic && !question.optionsAr.isNullOrEmpty()) question.optionsAr!! else question.options

    RdsCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuestionTypeChip(type = question.type)
                Text(
                    text = "${question.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            optionsList.forEachIndexed { index, option ->
                val isSelected = selectedOptionIndex == index
                val isCorrect = question.correctAnswerIndex == index

                val borderTint = when {
                    isSubmitted && isCorrect -> Color(0xFF10B981)
                    isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }

                val bgTint = when {
                    isSubmitted && isCorrect -> Color(0xFFD1FAE5)
                    isSubmitted && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(2.dp, borderTint, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isSubmitted) { onOptionSelected(index) }
                        .testTag("quiz_option_$index"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bgTint)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "${listOf("أ", "ب", "ج", "د").getOrElse(index) { "${index + 1}" }}." else "${'A' + index}.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (isSubmitted && !question.explanation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "توضيح الإجابة الصحيحة:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (if (isArabic && !question.explanationAr.isNullOrBlank()) question.explanationAr else question.explanation) ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultSummaryCard(
    score: Int,
    totalQuestions: Int,
    scorePercent: Int,
    isPassed: Boolean,
    xpEarned: Int,
    onRetry: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.Star else Icons.Default.Clear,
                contentDescription = null,
                tint = if (isPassed) Color(0xFFF59E0B) else Color(0xFFEF4444),
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPassed) "تهانينا! لقد اجتزت الاختبار" else "لم تتجاوز الحد الأدنى للاجتياز",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "الدرجة: $score من $totalQuestions ($scorePercent%)",
                style = MaterialTheme.typography.titleMedium,
                color = if (isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )

            if (isPassed && xpEarned > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "مكافأة الإنجاز: +$xpEarned XP",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
