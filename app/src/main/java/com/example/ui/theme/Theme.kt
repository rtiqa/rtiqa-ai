package com.example.ui.theme

import androidx.compose.runtime.Composable
import com.rtiqa.core.design.theme.RdsTheme

@Composable
fun RtiqaTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RdsTheme(
        darkTheme = darkTheme,
        content = content
    )
}

