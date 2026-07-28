package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.repository.OfflineSyncContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case to trigger remote synchronization of course materials.
 */
class SyncOfflineDataUseCase(
    private val offlineSyncContract: OfflineSyncContract
) {
    suspend operator fun invoke(): RtiqaResult<Unit> {
        return offlineSyncContract.syncRemoteCourses()
    }
}

/**
 * Use case to observe pending offline sync queue count.
 */
class ObserveSyncStatusUseCase(
    private val offlineSyncContract: OfflineSyncContract
) {
    operator fun invoke(): Flow<Int> {
        return offlineSyncContract.observePendingSyncCount()
    }
}
