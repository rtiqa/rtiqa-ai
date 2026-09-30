package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.ClassGradebook
import com.rtiqa.core.domain.result.RtiqaResult

interface GradebookRepository {
    suspend fun getClassGradebook(classId: String): RtiqaResult<ClassGradebook>
}
