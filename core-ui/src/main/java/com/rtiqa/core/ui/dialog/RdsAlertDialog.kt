package com.rtiqa.core.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.rtiqa.core.design.tokens.RdsCornerRadius
import com.rtiqa.core.ui.button.RdsPrimaryButton
import com.rtiqa.core.ui.button.RdsTextButton

/**
 * Reusable AlertDialog component in RDS
 */
@Composable
fun RdsAlertDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: ImageVector? = null,
    testTag: String = "rds_alert_dialog"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text = text, style = MaterialTheme.typography.bodyMedium) },
        icon = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
        confirmButton = {
            RdsPrimaryButton(
                text = confirmText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            RdsTextButton(
                text = dismissText,
                onClick = onDismiss
            )
        },
        shape = RdsCornerRadius.Card,
        modifier = modifier.testTag(testTag)
    )
}
