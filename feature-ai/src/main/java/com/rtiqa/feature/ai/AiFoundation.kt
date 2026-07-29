package com.rtiqa.feature.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.card.RdsCard

object AiRoutes {
    const val TUTOR = "ai/tutor"
}

@Composable
fun AiResponseCardFoundation(
    prompt: String,
    response: String,
    modifier: Modifier = Modifier
) {
    RdsCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "السؤال: $prompt",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                RdsBadge(text = "مُنَشَأ بالذكاء الاصطناعي", type = RdsBadgeType.AI)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = response,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
