package com.rtiqa.core.data.datasource

import com.rtiqa.core.domain.model.ClassGradebook

interface GradebookDataSource {
    suspend fun getClassGradebook(classId: String): ClassGradebook?
}
