package com.rtiqa.feature.quiz

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPlayScreen(
    uiState: QuizPlayUiState,
    onAction: (QuizPlayUiAction) -> Unit,
    onBack: () -> Unit,
    onQuizCompleted: (isPassed: Boolean, xpEarned: Int) -> Unit = { _, _ -> },
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val quizTitle = if (isArabic && !uiState.quiz?.titleAr.isNullOrBlank()) {
        uiState.quiz!!.titleAr!!
    } else {
        uiState.quiz?.title ?: if (isArabic) "اختبار المقرر" else "Quiz"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quizTitle) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("quiz_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isArabic) "رجوع" else "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier.testTag("quiz_play_screen")
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.quiz == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onAction(
                                QuizPlayUiAction.LoadQuizForCourse(
                                    uiState.courseId,
                                    uiState.lessonId
                                )
                            )
                        },
                        modifier = Modifier.testTag("quiz_retry_load_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "إعادة المحاولة" else "Retry")
                    }
                }
            }

            uiState.isSubmitted -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    QuizResultSummaryCard(
                        score = uiState.score,
                        totalQuestions = uiState.totalQuestions,
                        scorePercent = uiState.scorePercent,
                        isPassed = uiState.isPassed,
                        xpEarned = uiState.xpEarned,
                        onRetry = {
                            onAction(
                                QuizPlayUiAction.LoadQuizForCourse(
                                    uiState.courseId,
                                    uiState.lessonId
                                )
                            )
                        },
                        onDone = {
                            onQuizCompleted(uiState.isPassed, uiState.xpEarned)
                            onBack()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onQuizCompleted(uiState.isPassed, uiState.xpEarned)
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_finish_button")
                    ) {
                        Text(if (isArabic) "العودة للدرس" else "Back to Lesson")
                    }

                    if (!uiState.isPassed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                onAction(
                                    QuizPlayUiAction.LoadQuizForCourse(
                                        uiState.courseId,
                                        uiState.lessonId
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_retry_button")
                        ) {
                            Text(if (isArabic) "إعادة الاختبار" else "Retry Quiz")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    QuizTimerHeader(
                        timeLeftSeconds = uiState.timeLeftSeconds,
                        currentQuestionIndex = uiState.currentQuestionIndex,
                        totalQuestions = uiState.totalQuestions
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val currentQuestion = uiState.quiz?.questions?.getOrNull(uiState.currentQuestionIndex)
                    if (currentQuestion != null) {
                        QuizQuestionFoundation(
                            question = currentQuestion,
                            selectedOptionIndex = uiState.selectedAnswers[uiState.currentQuestionIndex],
                            onOptionSelected = { optionIndex ->
                                onAction(
                                    QuizPlayUiAction.AnswerSelected(
                                        uiState.currentQuestionIndex,
                                        optionIndex
                                    )
                                )
                            },
                            isSubmitted = false,
                            isArabic = isArabic
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = { onAction(QuizPlayUiAction.PreviousQuestionClicked) },
                                modifier = Modifier.testTag("quiz_prev_button")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "السابق" else "Previous")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        val isLastQuestion = uiState.currentQuestionIndex >= uiState.totalQuestions - 1
                        if (isLastQuestion) {
                            Button(
                                onClick = { onAction(QuizPlayUiAction.SubmitQuizClicked) },
                                modifier = Modifier.testTag("quiz_submit_button")
                            ) {
                                Text(if (isArabic) "تسليم الاختبار" else "Submit Quiz")
                            }
                        } else {
                            Button(
                                onClick = { onAction(QuizPlayUiAction.NextQuestionClicked) },
                                modifier = Modifier.testTag("quiz_next_button")
                            ) {
                                Text(if (isArabic) "التالي" else "Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
