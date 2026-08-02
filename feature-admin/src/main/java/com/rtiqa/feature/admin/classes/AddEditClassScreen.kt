package com.rtiqa.feature.admin.classes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rtiqa.core.domain.model.SchoolClass

@Composable
fun AddEditClassScreen(
    schoolClass: SchoolClass?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, gradeLevel: String, sectionName: String, capacity: Int, roomNumber: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(schoolClass) { mutableStateOf(schoolClass?.name ?: "") }
    var gradeLevel by remember(schoolClass) { mutableStateOf(schoolClass?.gradeLevel ?: "الابتدائي") }
    var sectionName by remember(schoolClass) { mutableStateOf(schoolClass?.sectionName ?: "أ") }
    var capacityText by remember(schoolClass) { mutableStateOf(schoolClass?.capacity?.toString() ?: "30") }
    var roomNumber by remember(schoolClass) { mutableStateOf(schoolClass?.roomNumber ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isEditing = schoolClass != null
    val title = if (isEditing) "تعديل بيانات الصف" else "إضافة صف دراسي جديد"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("add_edit_class_dialog_title")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null || validationError != null) {
                    Text(
                        text = errorMessage ?: validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("class_error_message")
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("اسم الصف الدراسي *") },
                    placeholder = { Text("مثال: الصف الأول الابتدائي - أ") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_name_input")
                )

                OutlinedTextField(
                    value = gradeLevel,
                    onValueChange = { gradeLevel = it },
                    label = { Text("المرحلة الدراسية") },
                    placeholder = { Text("الابتدائي / المتوسط / الثانوي") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_grade_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        label = { Text("الشعبة/الفصل") },
                        placeholder = { Text("أ، ب، 1") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("class_section_input")
                    )

                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("رقم القاعة") },
                        placeholder = { Text("101") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("class_room_input")
                    )
                }

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it.filter { char -> char.isDigit() } },
                    label = { Text("السعة الاستيعابية (عدد الطلاب)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_capacity_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "يرجى إدخال اسم الصف"
                        return@Button
                    }
                    val cap = capacityText.toIntOrNull() ?: 30
                    onSave(name, gradeLevel, sectionName, cap, roomNumber)
                },
                modifier = Modifier.testTag("save_class_button")
            ) {
                Text(if (isEditing) "تحديث" else "إضافة")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_class_button")
            ) {
                Text("إلغاء")
            }
        }
    )
}
