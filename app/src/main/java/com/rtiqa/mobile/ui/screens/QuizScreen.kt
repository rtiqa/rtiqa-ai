package com.rtiqa.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.mobile.R
import com.rtiqa.mobile.domain.model.QuestionType
import com.rtiqa.mobile.domain.model.QuizQuestion
import com.rtiqa.mobile.ui.viewmodel.QuizUiState

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    question: QuizQuestion,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onToggleHint: () -> Unit,
    onRestartQuiz: () -> Unit,
    onClaimRewards: (Int, Int) -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isCompleted) {
            // Summary Completion Card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "كأس الإنجاز",
                    tint = if (uiState.isPassed) Color(0xFFF59E0B) else Color(0xFFEF4444),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (uiState.isPassed) stringResource(R.string.quiz_completed_title) else "أكملت الاختبار! حاول مرة أخرى لتحسين نتيجتك",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "الدرجة: ${uiState.correctAnswersCount} من ${uiState.quiz.questions.size} (${uiState.scorePercent}%)",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (uiState.isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.quiz_rewards_earned, uiState.xpEarned, uiState.coinsEarned),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onClaimRewards(uiState.xpEarned, uiState.coinsEarned)
                        onRestartQuiz()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_rewards_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.claim_rewards))
                }
            }
        } else {
            // Header Progress & Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.question_progress, uiState.currentQuestionIndex + 1, uiState.quiz.questions.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Timer display
                val mins = uiState.timeLeftSeconds / 60
                val secs = uiState.timeLeftSeconds % 60
                val formattedTimer = String.format("%02d:%02d", mins, secs)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.timeLeftSeconds < 60) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "الوقت المتبقي",
                            tint = if (uiState.timeLeftSeconds < 60) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTimer,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.timeLeftSeconds < 60) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onToggleHint) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "تلميح",
                        tint = if (uiState.showHint) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (question.type == QuestionType.TRUE_FALSE) Color(0xFFF0FDF4) else Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = if (question.type == QuestionType.TRUE_FALSE) "صح / خطأ" else "اختيار من متعدد",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (question.type == QuestionType.TRUE_FALSE) Color(0xFF15803D) else Color(0xFF1D4ED8),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${question.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isArabic) question.questionTextAr else question.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )

                    AnimatedVisibility(visible = uiState.showHint) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${stringResource(R.string.hint)}: ${if (isArabic) question.hintAr else question.hint}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option Items
            val options = if (isArabic) question.optionsAr else question.options
            options.forEachIndexed { index, option ->
                val isSelected = uiState.selectedOptionIndex == index
                val isCorrectAnswer = question.correctAnswerIndex == index

                val borderTint = when {
                    uiState.isSubmitted && isCorrectAnswer -> Color(0xFF10B981)
                    uiState.isSubmitted && isSelected && !uiState.isCorrect -> Color(0xFFEF4444)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(2.dp, borderTint, RoundedCornerShape(16.dp))
                        .clickable { onSelectOption(index) }
                        .testTag("quiz_option_$index"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "${listOf("أ", "ب", "ج", "د", "هـ").getOrElse(index) { "${index + 1}" }}." else "${'A' + index}.",
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

            Spacer(modifier = Modifier.height(12.dp))

            // Explanation Feedback if submitted
            if (uiState.isSubmitted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isCorrect) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (uiState.isCorrect) stringResource(R.string.correct_feedback, question.xpReward) else stringResource(R.string.incorrect_feedback),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) question.explanationAr else question.explanation,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Buttons
            if (!uiState.isSubmitted) {
                Button(
                    onClick = onSubmitAnswer,
                    enabled = uiState.selectedOptionIndex != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_submit_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.submit_answer))
                }
            } else {
                Button(
                    onClick = onNextQuestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_next_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.next_question))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
