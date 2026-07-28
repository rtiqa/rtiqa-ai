package com.rtiqa.feature.quiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rtiqa.core.domain.model.Question
import com.rtiqa.core.ui.button.RdsOutlinedButton
import com.rtiqa.core.ui.card.RdsCard

object QuizRoutes {
    const val QUIZ = "quiz/play/{courseId}"
}

@Composable
fun QuizQuestionFoundation(
    question: Question,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    RdsCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(text = question.text, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            question.options.forEachIndexed { index, option ->
                RdsOutlinedButton(
                    text = option,
                    onClick = { onOptionSelected(index) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
