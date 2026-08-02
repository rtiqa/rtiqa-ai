package com.rtiqa.feature.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.domain.usecase.DeleteEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetUsersForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase

class UserViewModelFactory(
    private val getUsersForSchoolUseCase: GetUsersForSchoolUseCase,
    private val saveEnterpriseMemberUseCase: SaveEnterpriseMemberUseCase,
    private val deleteEnterpriseMemberUseCase: DeleteEnterpriseMemberUseCase,
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val preferencesDataStore: RtiqaPreferencesDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
            return UserManagementViewModel(
                getUsersForSchoolUseCase = getUsersForSchoolUseCase,
                saveEnterpriseMemberUseCase = saveEnterpriseMemberUseCase,
                deleteEnterpriseMemberUseCase = deleteEnterpriseMemberUseCase,
                getSchoolsUseCase = getSchoolsUseCase,
                preferencesDataStore = preferencesDataStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
