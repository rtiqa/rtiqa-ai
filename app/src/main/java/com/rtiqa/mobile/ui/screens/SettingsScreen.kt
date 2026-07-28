package com.rtiqa.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.mobile.BuildConfig
import com.rtiqa.mobile.domain.model.UserProfile

import androidx.compose.ui.res.stringResource
import com.rtiqa.mobile.R

@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    isOnline: Boolean,
    onBack: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleOfflineAutoSync: () -> Unit = {},
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val apiKeyPresent = try {
        val k = BuildConfig.GEMINI_API_KEY
        k.isNotEmpty() && k != "MY_GEMINI_API_KEY" && k != "null"
    } catch (e: Exception) {
        false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.app_language), fontWeight = FontWeight.Bold)
                        Text(if (isArabic) stringResource(R.string.arabic_default) else stringResource(R.string.english_selected), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Switch(
                    checked = isArabic,
                    onCheckedChange = { onToggleLanguage() },
                    modifier = Modifier.testTag("settings_lang_switch")
                )
            }
        }

        // Dark Theme Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.dark_theme), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.dark_theme_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Switch(
                    checked = userProfile.isDarkMode,
                    onCheckedChange = { onToggleTheme() },
                    modifier = Modifier.testTag("settings_theme_switch")
                )
            }
        }

        // Offline Auto-Sync Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.offline_auto_sync), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.auto_sync_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Switch(
                    checked = userProfile.isOfflineAutoSyncEnabled,
                    onCheckedChange = { onToggleOfflineAutoSync() },
                    modifier = Modifier.testTag("settings_sync_switch")
                )
            }
        }

        // Gemini AI Secrets Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Key, contentDescription = "API Key", tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.gemini_status_title), fontWeight = FontWeight.Bold)
                    Text(
                        text = if (apiKeyPresent) stringResource(R.string.gemini_live) else stringResource(R.string.gemini_offline),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Connectivity Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Wifi, contentDescription = "Network", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.network_status_title), fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isOnline) stringResource(R.string.network_online) else stringResource(R.string.network_offline),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
