package com.rtiqa.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.mobile.domain.model.QuizQuestion
import com.rtiqa.mobile.ui.viewmodel.QuizUiState

import androidx.compose.ui.res.stringResource
import com.rtiqa.mobile.R

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
    isArabic: Boolean = false,
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
                    contentDescription = "Trophy",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.quiz_completed_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

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
            // Header Progress
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

                IconButton(onClick = onToggleHint) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hint",
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
                            text = "${'A' + index}.",
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
