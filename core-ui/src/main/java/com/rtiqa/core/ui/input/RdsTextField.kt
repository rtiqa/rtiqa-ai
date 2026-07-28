package com.rtiqa.core.ui.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rtiqa.core.design.accessibility.RdsAccessibility.rdsTouchTarget
import com.rtiqa.core.design.tokens.RdsCornerRadius

/**
 * Reusable TextField component in RDS
 */
@Composable
fun RdsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    testTag: String = "rds_text_field"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            placeholder = placeholder?.let { { Text(text = it) } },
            modifier = Modifier
                .fillMaxWidth()
                .rdsTouchTarget()
                .testTag(testTag),
            shape = RdsCornerRadius.Input,
            singleLine = singleLine,
            isError = isError,
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = trailingIcon?.let { icon ->
                {
                    if (onTrailingIconClick != null) {
                        IconButton(onClick = onTrailingIconClick) {
                            Icon(imageVector = icon, contentDescription = null)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
