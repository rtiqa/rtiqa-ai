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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

/**
 * State holder for School add/edit form data and validation.
 */
class SchoolFormState(
    school: School? = null
) {
    var name by mutableStateOf(school?.name ?: "")
    var code by mutableStateOf(school?.code ?: "")
    var address by mutableStateOf(school?.address ?: "")
    var phone by mutableStateOf(school?.phone ?: "")
    var studentsCountStr by mutableStateOf((school?.studentsCount ?: 0).toString())
    var teachersCountStr by mutableStateOf((school?.teachersCount ?: 0).toString())

    var nameError by mutableStateOf(false)
    var codeError by mutableStateOf(false)

    fun validateAndSave(
        schoolId: String?,
        onSave: (id: String?, name: String, code: String, address: String, phone: String, studentsCount: Int, teachersCount: Int) -> Unit
    ) {
        if (name.isBlank() || code.isBlank()) {
            nameError = name.isBlank()
            codeError = code.isBlank()
            return
        }
        val sCount = studentsCountStr.toIntOrNull() ?: 0
        val tCount = teachersCountStr.toIntOrNull() ?: 0
        onSave(schoolId, name, code, address, phone, sCount, tCount)
    }
}

@Composable
fun rememberSchoolFormState(school: School? = null): SchoolFormState {
    return remember(school) { SchoolFormState(school) }
}

@Composable
fun SchoolFormFields(
    state: SchoolFormState,
    modifier: Modifier = Modifier,
    fieldSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    nameTestTag: String = "school_name_input",
    codeTestTag: String = "school_code_input",
    showNameErrorMessage: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.name,
            onValueChange = {
                state.name = it
                state.nameError = it.isBlank()
            },
            label = { Text("اسم المدرسة *") },
            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
            isError = state.nameError,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(nameTestTag)
        )
        if (showNameErrorMessage && state.nameError) {
            Text(
                text = "اسم المدرسة مطلوب",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(fieldSpacing))

        OutlinedTextField(
            value = state.code,
            onValueChange = {
                state.code = it
                state.codeError = it.isBlank()
            },
            label = { Text("رمز المدرسة (Code) *") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            isError = state.codeError,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(codeTestTag)
        )
        Spacer(modifier = Modifier.height(fieldSpacing))

        OutlinedTextField(
            value = state.address,
            onValueChange = { state.address = it },
            label = { Text("العنوان / المدينة") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(fieldSpacing))

        OutlinedTextField(
            value = state.phone,
            onValueChange = { state.phone = it },
            label = { Text("رقم الهاتف / التواصل") },
            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(fieldSpacing))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.studentsCountStr,
                onValueChange = { state.studentsCountStr = it },
                label = { Text("عدد الطلاب") },
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = state.teachersCountStr,
                onValueChange = { state.teachersCountStr = it },
                label = { Text("عدد المعلمين") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AddEditSchoolDialog(
    school: School? = null,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, code: String, address: String, phone: String, studentsCount: Int, teachersCount: Int) -> Unit
) {
    val formState = rememberSchoolFormState(school)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (school == null) "إضافة مدرسة جديدة" else "تعديل بيانات المدرسة",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            SchoolFormFields(
                state = formState,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                fieldSpacing = 8.dp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    formState.validateAndSave(school?.id, onSave)
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
    val formState = rememberSchoolFormState(school)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (school == null) "إضافة مدرسة جديدة" else "تعديل المدرسة") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
            SchoolFormFields(
                state = formState,
                fieldSpacing = 12.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    formState.validateAndSave(school?.id, onSave)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ المدرسة")
            }
        }
    }
}
