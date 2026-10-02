package com.rtiqa.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.feature.profile.ProfileUiAction
import com.rtiqa.feature.profile.ProfileUiEvent
import com.rtiqa.feature.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateToLogin -> onNavigateToLogin()
                is ProfileUiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.profile == null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text(if (isArabic) "الملف الشخصي غير متاح" else "Profile unavailable")
                uiState.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                val profile = requireNotNull(uiState.profile)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("profile_header_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(72.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = if (isArabic) "الصورة الشخصية" else "Profile",
                                        modifier = Modifier.size(60.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(Modifier.height(12.dp))

                                if (uiState.isEditing) {
                                    OutlinedTextField(
                                        value = uiState.editName,
                                        onValueChange = { viewModel.onAction(ProfileUiAction.NameInputChanged(it)) },
                                        label = { Text(if (isArabic) "الاسم" else "Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { viewModel.onAction(ProfileUiAction.CancelEditing) },
                                            enabled = !uiState.isSaving
                                        ) { Text(if (isArabic) "إلغاء" else "Cancel") }
                                        Button(
                                            onClick = { viewModel.onAction(ProfileUiAction.SaveProfileClicked) },
                                            enabled = !uiState.isSaving && uiState.editName.isNotBlank()
                                        ) {
                                            Text(if (uiState.isSaving) "…" else if (isArabic) "حفظ" else "Save")
                                        }
                                    }
                                } else {
                                    Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(
                                        profile.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.onAction(ProfileUiAction.StartEditing) },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isArabic) "تعديل الاسم" else "Edit name")
                                    }
                                }

                                uiState.errorMessage?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }

                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    ProfileStat("XP", profile.levelXp.toString())
                                    ProfileStat(if (isArabic) "المستوى" else "Level", profile.calculateLevel().toString())
                                    ProfileStat(if (isArabic) "التتابع" else "Streak", profile.streakDays.toString())
                                }

                                if (profile.isAdmin) {
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = onNavigateToAdmin,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (isArabic) "لوحة تحكم المسؤول" else "Admin dashboard")
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.onAction(ProfileUiAction.LogoutClicked) },
                                    modifier = Modifier.fillMaxWidth().testTag("profile_logout_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (isArabic) "تسجيل الخروج" else "Log out", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}
