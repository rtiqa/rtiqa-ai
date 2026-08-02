package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.SchoolClass
import kotlinx.coroutines.flow.Flow

interface ClassRepository {
    fun getClassesForSchool(schoolId: String): Flow<List<SchoolClass>>
    suspend fun getClassById(id: String): SchoolClass?
    suspend fun isClassNameUniqueInSchool(schoolId: String, name: String, excludeId: String = ""): Boolean
    suspend fun saveClass(schoolClass: SchoolClass)
    suspend fun deleteClass(id: String)
    suspend fun updateClassOrder(classes: List<SchoolClass>)
}
