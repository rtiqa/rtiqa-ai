package com.rtiqa.feature.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

object HomeRoutes {
    const val DASHBOARD = "home/dashboard"
}

@Composable
fun HomeStreakCardFoundation(
    streakDays: Int,
    xpPoints: Int,
    modifier: Modifier = Modifier
) {
    RdsCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Streak",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            RdsBadge(text = "$streakDays Days", type = RdsBadgeType.SUCCESS)
            Spacer(modifier = Modifier.width(8.dp))
            RdsBadge(text = "$xpPoints XP", type = RdsBadgeType.AI)
        }
    }
}
