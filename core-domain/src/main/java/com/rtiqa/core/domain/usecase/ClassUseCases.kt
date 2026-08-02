package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.domain.repository.ClassRepository
import kotlinx.coroutines.flow.Flow

class GetClassesForSchoolUseCase(
    private val classRepository: ClassRepository
) {
    operator fun invoke(schoolId: String): Flow<List<SchoolClass>> =
        classRepository.getClassesForSchool(schoolId)
}

class GetClassByIdUseCase(
    private val classRepository: ClassRepository
) {
    suspend operator fun invoke(id: String): SchoolClass? =
        classRepository.getClassById(id)
}

class ValidateClassNameUniquenessUseCase(
    private val classRepository: ClassRepository
) {
    suspend operator fun invoke(schoolId: String, name: String, excludeId: String = ""): Boolean =
        classRepository.isClassNameUniqueInSchool(schoolId, name, excludeId)
}

class SaveClassUseCase(
    private val classRepository: ClassRepository
) {
    suspend operator fun invoke(schoolClass: SchoolClass): Result<Unit> {
        val trimmedName = schoolClass.name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("اسم الصف مطلوب"))
        }
        val isUnique = classRepository.isClassNameUniqueInSchool(
            schoolId = schoolClass.schoolId,
            name = trimmedName,
            excludeId = schoolClass.id
        )
        if (!isUnique) {
            return Result.failure(IllegalStateException("اسم الصف مستخدم بالفعل في هذه المدرسة"))
        }
        classRepository.saveClass(schoolClass.copy(name = trimmedName))
        return Result.success(Unit)
    }
}

class DeleteClassUseCase(
    private val classRepository: ClassRepository
) {
    suspend operator fun invoke(id: String) {
        classRepository.deleteClass(id)
    }
}

class ReorderClassesUseCase(
    private val classRepository: ClassRepository
) {
    suspend operator fun invoke(classes: List<SchoolClass>) {
        classRepository.updateClassOrder(classes)
    }
}
