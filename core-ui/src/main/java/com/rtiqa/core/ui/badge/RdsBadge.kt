package com.rtiqa.core.ui.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.rtiqa.core.design.tokens.RdsColor
import com.rtiqa.core.design.tokens.RdsCornerRadius
import com.rtiqa.core.design.tokens.RdsIcons

enum class RdsBadgeType {
    SUCCESS, WARNING, ERROR, AI, OFFLINE, INFO
}

@Composable
fun RdsBadge(
    text: String,
    modifier: Modifier = Modifier,
    type: RdsBadgeType = RdsBadgeType.INFO,
    icon: ImageVector? = null,
    testTag: String = "rds_badge"
) {
    val (bgColor, textColor, defaultIcon) = when (type) {
        RdsBadgeType.SUCCESS -> Triple(RdsColor.SuccessContainer, RdsColor.Success, RdsIcons.Success)
        RdsBadgeType.WARNING -> Triple(RdsColor.WarningContainer, RdsColor.Warning, RdsIcons.Warning)
        RdsBadgeType.ERROR -> Triple(RdsColor.ErrorContainer, RdsColor.Error, RdsIcons.Error)
        RdsBadgeType.AI -> Triple(RdsColor.AiGlowLight, RdsColor.AiPurple, RdsIcons.AiSparkle)
        RdsBadgeType.OFFLINE -> Triple(RdsColor.DarkSurfaceVariant, RdsColor.DarkOnSurfaceVariant, RdsIcons.Offline)
        RdsBadgeType.INFO -> Triple(RdsColor.InfoContainer, RdsColor.Info, RdsIcons.Info)
    }

    val displayIcon = icon ?: defaultIcon

    Box(
        modifier = modifier
            .testTag(testTag)
            .background(bgColor, RdsCornerRadius.Badge)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = displayIcon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}
