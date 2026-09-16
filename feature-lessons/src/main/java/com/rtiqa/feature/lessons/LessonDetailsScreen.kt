package com.rtiqa.feature.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rtiqa.core.domain.model.Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailsScreen(
    uiState: LessonViewerUiState,
    onAction: (LessonViewerUiAction) -> Unit,
    onBack: () -> Unit,
    onStartQuiz: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.lesson?.title ?: "تفاصيل الدرس") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.lesson?.title ?: "عنوان الدرس غير متوفر",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // اسم المادة
                Text(
                    text = "المادة: ${uiState.courseId}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // وصف الدرس / محتوى نصي
                Text(
                    text = "وصف الدرس ومحتواه:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.lesson?.content ?: "لا يوجد محتوى لهذا الدرس.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onStartQuiz,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text("ابدأ الاختبار")
                }
            }
        }
    }
}
