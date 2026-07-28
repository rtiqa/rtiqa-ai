package com.rtiqa.feature.admin

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rtiqa.core.ui.badge.RdsBadge
import com.rtiqa.core.ui.badge.RdsBadgeType
import com.rtiqa.core.ui.card.RdsCard

object AdminRoutes {
    const val DASHBOARD = "admin/dashboard"
}

@Composable
fun AdminMetricFoundation(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    RdsCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            RdsBadge(text = value, type = RdsBadgeType.SUCCESS)
        }
    }
}
