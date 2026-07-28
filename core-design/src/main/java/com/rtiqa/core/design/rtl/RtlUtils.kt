package com.rtiqa.core.design.rtl

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * RTL Utilities for Arabic and LTR layout adaptation
 */
object RtlUtils {
    @Composable
    fun isRtl(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl

    @Composable
    fun autoMirrorRotation(): Float = if (isRtl()) 180f else 0f
}
