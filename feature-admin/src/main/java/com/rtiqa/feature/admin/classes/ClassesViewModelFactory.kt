package com.rtiqa.feature.admin.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.domain.usecase.DeleteClassUseCase
import com.rtiqa.core.domain.usecase.GetClassesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.ReorderClassesUseCase
import com.rtiqa.core.domain.usecase.SaveClassUseCase

class ClassesViewModelFactory(
    private val getClassesForSchoolUseCase: GetClassesForSchoolUseCase,
    private val saveClassUseCase: SaveClassUseCase,
    private val deleteClassUseCase: DeleteClassUseCase,
    private val reorderClassesUseCase: ReorderClassesUseCase,
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val preferencesDataStore: RtiqaPreferencesDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClassesViewModel(
            getClassesForSchoolUseCase = getClassesForSchoolUseCase,
            saveClassUseCase = saveClassUseCase,
            deleteClassUseCase = deleteClassUseCase,
            reorderClassesUseCase = reorderClassesUseCase,
            getSchoolsUseCase = getSchoolsUseCase,
            preferencesDataStore = preferencesDataStore
        ) as T
    }
}
