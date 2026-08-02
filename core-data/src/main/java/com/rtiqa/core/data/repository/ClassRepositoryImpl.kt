package com.rtiqa.core.data.repository

import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.data.mapper.toEntity
import com.rtiqa.core.database.dao.SchoolClassDao
import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.domain.repository.ClassRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClassRepositoryImpl(
    private val classDao: SchoolClassDao
) : ClassRepository {

    override fun getClassesForSchool(schoolId: String): Flow<List<SchoolClass>> =
        classDao.getClassesForSchool(schoolId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getClassById(id: String): SchoolClass? =
        classDao.getClassById(id)?.toDomain()

    override suspend fun isClassNameUniqueInSchool(schoolId: String, name: String, excludeId: String): Boolean =
        classDao.findClassByNameInSchool(schoolId, name, excludeId) == null

    override suspend fun saveClass(schoolClass: SchoolClass) {
        classDao.insertClass(schoolClass.toEntity())
    }

    override suspend fun deleteClass(id: String) {
        classDao.deleteClassById(id)
    }

    override suspend fun updateClassOrder(classes: List<SchoolClass>) {
        classDao.insertClasses(classes.mapIndexed { index, schoolClass ->
            schoolClass.copy(displayOrder = index + 1).toEntity()
        })
    }
}
