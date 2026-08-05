package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.Permission
import com.rtiqa.core.domain.model.RbacEvaluator
import com.rtiqa.core.domain.model.StudentEnrollment
import com.rtiqa.core.domain.model.TeacherAssignment
import com.rtiqa.core.domain.repository.SchoolManagementCoreRepository
import kotlinx.coroutines.flow.Flow

class GetGradeLevelsForSchoolUseCase(private val repository: SchoolManagementCoreRepository) {
    operator fun invoke(schoolId: String): Flow<List<GradeLevel>> = repository.getGradeLevelsForSchool(schoolId)
}

class SaveGradeLevelUseCase(private val repository: SchoolManagementCoreRepository) {
    suspend operator fun invoke(gradeLevel: GradeLevel) = repository.saveGradeLevel(gradeLevel)
}

class DeleteGradeLevelUseCase(private val repository: SchoolManagementCoreRepository) {
    suspend operator fun invoke(id: String) = repository.deleteGradeLevel(id)
}

class GetTeacherAssignmentsForSchoolUseCase(private val repository: SchoolManagementCoreRepository) {
    operator fun invoke(schoolId: String): Flow<List<TeacherAssignment>> = repository.getTeacherAssignmentsForSchool(schoolId)
}

class GetTeacherAssignmentsForTeacherUseCase(private val repository: SchoolManagementCoreRepository) {
    operator fun invoke(teacherId: String): Flow<List<TeacherAssignment>> = repository.getAssignmentsForTeacher(teacherId)
}

class SaveTeacherAssignmentUseCase(private val repository: SchoolManagementCoreRepository) {
    suspend operator fun invoke(assignment: TeacherAssignment) = repository.saveTeacherAssignment(assignment)
}

class DeleteTeacherAssignmentUseCase(private val repository: SchoolManagementCoreRepository) {
    suspend operator fun invoke(id: String) = repository.deleteTeacherAssignment(id)
}

class GetEnrollmentsForSchoolUseCase(private val repository: SchoolManagementCoreRepository) {
    operator fun invoke(schoolId: String): Flow<List<StudentEnrollment>> = repository.getEnrollmentsForSchool(schoolId)
}

class GetEnrollmentsForClassUseCase(private val repository: SchoolManagementCoreRepository) {
    operator fun invoke(classId: String): Flow<List<StudentEnrollment>> = repository.getEnrollmentsForClass(classId)
}

class SaveStudentEnrollmentUseCase(private val repository: SchoolManagementCoreRepository) {
    suspend operator fun invoke(enrollment: StudentEnrollment) = repository.saveStudentEnrollment(enrollment)
}

class DeleteStudentEnrollmentUseCase(private val repository: SchoolManagementCoreRepository) {
    suspend operator fun invoke(id: String) = repository.deleteStudentEnrollment(id)
}

class EvaluateUserPermissionUseCase {
    operator fun invoke(role: EnterpriseRole, permission: Permission): Boolean {
        return RbacEvaluator.hasPermission(role, permission)
    }

    fun getPermissions(role: EnterpriseRole): Set<Permission> {
        return RbacEvaluator.getPermissionsForRole(role)
    }
}

class CheckSchoolAccessUseCase {
    operator fun invoke(member: EnterpriseMember, schoolId: String): Boolean {
        return RbacEvaluator.canAccessSchool(member, schoolId)
    }
}
