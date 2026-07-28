package com.rtiqa.core.design.accessibility

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Accessibility Helpers for Rtiqa Design System
 */
object RdsAccessibility {
    /**
     * Enforces M3 minimum touch target size (48dp x 48dp)
     */
    fun Modifier.rdsTouchTarget(): Modifier = this.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)

    /**
     * Helper to set clean semantics for screen readers
     */
    fun Modifier.rdsAccessibilityLabel(label: String): Modifier = this.semantics {
        contentDescription = label
    }

    /**
     * Marks text headers as accessibility headings
     */
    fun Modifier.rdsHeaderSemantics(): Modifier = this.semantics {
        heading()
    }
}
