package com.rtiqa.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rtiqa.mobile.ui.navigation.RtiqaApp
import com.rtiqa.mobile.ui.theme.RtiqaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RtiqaTheme {
                RtiqaApp()
            }
        }
    }
}

