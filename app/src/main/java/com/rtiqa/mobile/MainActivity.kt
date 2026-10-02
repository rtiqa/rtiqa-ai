package com.rtiqa.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rtiqa.mobile.ui.navigation.RtiqaApp
import com.rtiqa.mobile.ui.theme.RtiqaTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.rtiqa.core.data.di.AppDiContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appDiContainer = remember { AppDiContainer(applicationContext) }
            val preferences by appDiContainer.preferencesDataStore.userPreferencesFlow.collectAsState(
                initial = com.rtiqa.core.data.datastore.UserPreferences(false, false, null, 0L)
            )
            RtiqaTheme(darkTheme = preferences.isDarkTheme) {
                RtiqaApp(appDiContainer = appDiContainer)
            }
        }
    }
}
