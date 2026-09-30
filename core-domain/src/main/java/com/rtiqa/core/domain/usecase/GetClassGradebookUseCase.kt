package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.ClassGradebook
import com.rtiqa.core.domain.repository.GradebookRepository
import com.rtiqa.core.domain.result.RtiqaResult

class GetClassGradebookUseCase(
    private val gradebookRepository: GradebookRepository
) {
    suspend operator fun invoke(classId: String): RtiqaResult<ClassGradebook> {
        if (classId.isBlank()) {
            return RtiqaResult.Error(
                RtiqaError.ValidationError(listOf("Class ID must not be blank"))
            )
        }
        return gradebookRepository.getClassGradebook(classId)
    }
}
