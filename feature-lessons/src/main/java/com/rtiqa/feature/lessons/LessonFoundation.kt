package com.rtiqa.feature.lessons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.ui.card.RdsCard

object LessonRoutes {
    const val VIEWER = "lessons/viewer/{lessonId}"
}

@Composable
fun LessonContentViewerFoundation(
    lesson: Lesson,
    modifier: Modifier = Modifier
) {
    RdsCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(text = lesson.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = lesson.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
