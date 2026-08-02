package com.rtiqa.feature.admin.school

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rtiqa.core.domain.model.School

@Composable
fun AddEditSchoolDialog(
    school: School? = null,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, code: String, address: String, phone: String, studentsCount: Int, teachersCount: Int) -> Unit
) {
    var name by remember { mutableStateOf(school?.name ?: "") }
    var code by remember { mutableStateOf(school?.code ?: "") }
    var address by remember { mutableStateOf(school?.address ?: "") }
    var phone by remember { mutableStateOf(school?.phone ?: "") }
    var studentsCountStr by remember { mutableStateOf((school?.studentsCount ?: 0).toString()) }
    var teachersCountStr by remember { mutableStateOf((school?.teachersCount ?: 0).toString()) }

    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (school == null) "إضافة مدرسة جديدة" else "تعديل بيانات المدرسة",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("اسم المدرسة *") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    isError = nameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("school_name_input")
                )
                if (nameError) {
                    Text(
                        text = "اسم المدرسة مطلوب",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        codeError = it.isBlank()
                    },
                    label = { Text("رمز المدرسة (Code) *") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    isError = codeError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("school_code_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان / المدينة") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف / التواصل") },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = studentsCountStr,
                        onValueChange = { studentsCountStr = it },
                        label = { Text("عدد الطلاب") },
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = teachersCountStr,
                        onValueChange = { teachersCountStr = it },
                        label = { Text("عدد المعلمين") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || code.isBlank()) {
                        nameError = name.isBlank()
                        codeError = code.isBlank()
                        return@Button
                    }
                    val sCount = studentsCountStr.toIntOrNull() ?: 0
                    val tCount = teachersCountStr.toIntOrNull() ?: 0
                    onSave(school?.id, name, code, address, phone, sCount, tCount)
                },
                modifier = Modifier.testTag("save_school_button")
            ) {
                Text(if (school == null) "إضافة" else "حفظ التغييرات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSchoolScreen(
    school: School? = null,
    onBack: () -> Unit,
    onSave: (id: String?, name: String, code: String, address: String, phone: String, studentsCount: Int, teachersCount: Int) -> Unit
) {
    var name by remember { mutableStateOf(school?.name ?: "") }
    var code by remember { mutableStateOf(school?.code ?: "") }
    var address by remember { mutableStateOf(school?.address ?: "") }
    var phone by remember { mutableStateOf(school?.phone ?: "") }
    var studentsCountStr by remember { mutableStateOf((school?.studentsCount ?: 0).toString()) }
    var teachersCountStr by remember { mutableStateOf((school?.teachersCount ?: 0).toString()) }

    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (school == null) "إضافة مدرسة جديدة" else "تعديل المدرسة") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                label = { Text("اسم المدرسة *") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                isError = nameError,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    codeError = it.isBlank()
                },
                label = { Text("رمز المدرسة *") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                isError = codeError,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("العنوان") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("الهاتف") },
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = studentsCountStr,
                    onValueChange = { studentsCountStr = it },
                    label = { Text("عدد الطلاب") },
                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = teachersCountStr,
                    onValueChange = { teachersCountStr = it },
                    label = { Text("عدد المعلمين") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || code.isBlank()) {
                        nameError = name.isBlank()
                        codeError = code.isBlank()
                        return@Button
                    }
                    val sCount = studentsCountStr.toIntOrNull() ?: 0
                    val tCount = teachersCountStr.toIntOrNull() ?: 0
                    onSave(school?.id, name, code, address, phone, sCount, tCount)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ المدرسة")
            }
        }
    }
}
