package com.rtiqa.feature.offline

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.card.RdsCard

object OfflineRoutes {
    const val DOWNLOADS = "offline/downloads"
}

@Composable
fun OfflineStatusItemFoundation(
    title: String,
    sizeMb: Double,
    modifier: Modifier = Modifier
) {
    RdsCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            RdsBadge(text = "${sizeMb}MB", type = RdsBadgeType.OFFLINE)
        }
    }
}
