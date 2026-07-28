package com.rtiqa.core.design.tokens

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Rtiqa Design System Animation Tokens
 */
object RdsAnimations {
    const val DurationFast = 150
    const val DurationDefault = 300
    const val DurationSlow = 500

    fun <T> fast(): AnimationSpec<T> = tween(
        durationMillis = DurationFast,
        easing = FastOutSlowInEasing
    )

    fun <T> default(): AnimationSpec<T> = tween(
        durationMillis = DurationDefault,
        easing = FastOutSlowInEasing
    )

    fun <T> slow(): AnimationSpec<T> = tween(
        durationMillis = DurationSlow,
        easing = FastOutSlowInEasing
    )

    fun <T> bouncy(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}
