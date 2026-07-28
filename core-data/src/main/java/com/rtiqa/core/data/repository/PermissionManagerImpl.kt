package com.rtiqa.core.data.repository

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.rtiqa.core.domain.repository.PermissionContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android system runtime permission manager implementation.
 */
class PermissionManagerImpl(
    private val context: Context
) : PermissionContract {

    private val permissionStates = mutableMapOf<String, MutableStateFlow<Boolean>>()

    override fun isPermissionGranted(permissionName: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionName
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun observePermissionStatus(permissionName: String): Flow<Boolean> {
        val granted = isPermissionGranted(permissionName)
        val state = permissionStates.getOrPut(permissionName) { MutableStateFlow(granted) }
        state.value = granted
        return state.asStateFlow()
    }
}
