package com.rtiqa.core.design.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Rtiqa Design System Shapes
 */
val RdsShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object RdsCornerRadius {
    val Pill = RoundedCornerShape(999.dp)
    val Card = RoundedCornerShape(16.dp)
    val Sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val Input = RoundedCornerShape(12.dp)
    val Badge = RoundedCornerShape(8.dp)
}
