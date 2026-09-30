package com.rtiqa.core.data.repository

import com.rtiqa.core.data.datasource.GradebookDataSource
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.ClassGradebook
import com.rtiqa.core.domain.repository.GradebookRepository
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.CancellationException

class GradebookRepositoryImpl(
    private val gradebookDataSource: GradebookDataSource? = null
) : GradebookRepository {

    override suspend fun getClassGradebook(classId: String): RtiqaResult<ClassGradebook> {
        return try {
            val gradebook = gradebookDataSource?.getClassGradebook(classId)
            if (gradebook != null) {
                RtiqaResult.Success(gradebook)
            } else {
                RtiqaResult.Success(
                    ClassGradebook(
                        classId = classId,
                        students = emptyList(),
                        assessments = emptyList(),
                        scores = emptyList()
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RtiqaResult.Error(
                RtiqaError.DatabaseError(
                    message = e.message ?: "Failed to load gradebook for class $classId",
                    cause = e
                )
            )
        }
    }
}
