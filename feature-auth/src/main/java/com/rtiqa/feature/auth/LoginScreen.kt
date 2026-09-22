package com.rtiqa.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import com.rtiqa.core.ui.button.RdsTextButton
import com.rtiqa.feature.auth.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
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
                is LoginUiEvent.NavigateToHome -> onNavigateToHome()
                is LoginUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("login_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AuthTopAppBar(
                title = stringResource(R.string.login),
                onBack = onBack,
                backButtonTestTag = "login_back_button"
            )
        }
    ) { innerPadding ->
        AuthScreenLayout(innerPadding = innerPadding) {
            AuthHeader(
                icon = Icons.Default.Lock,
                iconContentDescription = "قفل",
                title = stringResource(R.string.welcome_back),
                subtitle = stringResource(R.string.login_subtitle),
                titleFontSize = 26.sp
            )

            AuthCard {
                AuthEmailField(
                    value = uiState.email,
                    onValueChange = { viewModel.onAction(LoginUiAction.EmailChanged(it)) },
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError,
                    testTag = "login_email_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthPasswordField(
                    value = uiState.password,
                    onValueChange = { viewModel.onAction(LoginUiAction.PasswordChanged(it)) },
                    isPasswordVisible = isPasswordVisible,
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError,
                    testTag = "login_password_input"
                )

                AuthErrorMessage(message = uiState.errorMessage)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    RdsTextButton(
                        text = stringResource(R.string.forgot_password_question),
                        onClick = onNavigateToForgotPassword,
                        testTag = "login_forgot_password_button"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                RdsPrimaryButton(
                    text = stringResource(R.string.login),
                    onClick = { viewModel.onAction(LoginUiAction.SubmitLogin) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "login_submit_button"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AuthBottomPrompt(
                promptText = stringResource(R.string.no_account_question),
                actionText = stringResource(R.string.register_now),
                onActionClick = onNavigateToRegister,
                testTag = "login_register_link"
            )
        }
    }
}
