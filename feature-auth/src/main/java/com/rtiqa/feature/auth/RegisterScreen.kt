package com.rtiqa.feature.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.ui.button.RdsPrimaryButton
import com.rtiqa.core.ui.input.RdsTextField
import com.rtiqa.feature.auth.R

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is RegisterUiEvent.NavigateToHome -> onNavigateToHome()
                is RegisterUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("register_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AuthTopAppBar(
                title = stringResource(R.string.create_account),
                onBack = onBack,
                backButtonTestTag = "register_back_button"
            )
        }
    ) { innerPadding ->
        AuthScreenLayout(innerPadding = innerPadding) {
            AuthHeader(
                icon = Icons.Default.Person,
                iconContentDescription = "تسجيل جديد",
                title = stringResource(R.string.create_account),
                subtitle = stringResource(R.string.welcome_subtitle),
                titleFontSize = 26.sp
            )

            AuthCard {
                RdsTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onAction(RegisterUiAction.NameChanged(it)) },
                    label = stringResource(R.string.full_name),
                    placeholder = "مثال: أحمد علي",
                    leadingIcon = Icons.Default.Person,
                    testTag = "register_name_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthEmailField(
                    value = uiState.email,
                    onValueChange = { viewModel.onAction(RegisterUiAction.EmailChanged(it)) },
                    testTag = "register_email_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthPasswordField(
                    value = uiState.password,
                    onValueChange = { viewModel.onAction(RegisterUiAction.PasswordChanged(it)) },
                    isPasswordVisible = isPasswordVisible,
                    testTag = "register_password_input"
                )

                AuthErrorMessage(message = uiState.errorMessage)

                Spacer(modifier = Modifier.height(24.dp))

                RdsPrimaryButton(
                    text = stringResource(R.string.create_account),
                    onClick = { viewModel.onAction(RegisterUiAction.SubmitRegister) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "register_submit_button"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AuthBottomPrompt(
                promptText = stringResource(R.string.already_have_account),
                actionText = stringResource(R.string.login),
                onActionClick = onNavigateToLogin,
                testTag = "register_login_link"
            )
        }
    }
}
