package com.rtiqa.feature.courses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.card.RdsCard

object CourseRoutes {
    const val LIST = "courses/list"
    const val DETAIL = "courses/detail/{courseId}"
}

@Composable
fun CourseItemFoundation(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RdsCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (course.isDownloaded) {
                    RdsBadge(text = "بدون إنترنت", type = RdsBadgeType.OFFLINE)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { course.progressPercent },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
