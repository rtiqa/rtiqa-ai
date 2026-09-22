package com.rtiqa.feature.auth

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.core.ui.button.RdsPrimaryButton
import com.rtiqa.feature.auth.R

@Composable
fun ForgotPasswordScreen(
    viewModel: LoginViewModel,
    onBack: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("forgot_password_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AuthTopAppBar(
                title = stringResource(R.string.reset_password),
                onBack = onBack,
                backButtonTestTag = "forgot_password_back_button"
            )
        }
    ) { innerPadding ->
        AuthScreenLayout(innerPadding = innerPadding) {
            AuthHeader(
                icon = Icons.Default.Lock,
                iconContentDescription = "استعادة كلمة المرور",
                title = stringResource(R.string.forgot_password_question),
                subtitle = stringResource(R.string.reset_password_instruction),
                titleFontSize = 24.sp
            )

            AuthCard {
                AuthEmailField(
                    value = uiState.email,
                    onValueChange = { viewModel.onAction(LoginUiAction.EmailChanged(it)) },
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError,
                    testTag = "forgot_password_email_input"
                )

                AuthErrorMessage(message = uiState.errorMessage)

                Spacer(modifier = Modifier.height(24.dp))

                RdsPrimaryButton(
                    text = stringResource(R.string.send_reset_link),
                    onClick = { viewModel.onAction(LoginUiAction.RequestPasswordReset) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "forgot_password_submit_button"
                )
            }
        }
    }
}
