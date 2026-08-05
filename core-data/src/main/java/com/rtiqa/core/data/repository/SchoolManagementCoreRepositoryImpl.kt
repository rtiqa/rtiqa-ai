package com.rtiqa.core.data.repository

import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.data.mapper.toEntity
import com.rtiqa.core.database.dao.SchoolManagementCoreDao
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.StudentEnrollment
import com.rtiqa.core.domain.model.TeacherAssignment
import com.rtiqa.core.domain.repository.SchoolManagementCoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SchoolManagementCoreRepositoryImpl(
    private val dao: SchoolManagementCoreDao
) : SchoolManagementCoreRepository {

    override fun getGradeLevelsForSchool(schoolId: String): Flow<List<GradeLevel>> =
        dao.getGradeLevelsForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getGradeLevelById(id: String): Flow<GradeLevel?> =
        dao.getGradeLevelById(id).map { it?.toDomain() }

    override suspend fun saveGradeLevel(gradeLevel: GradeLevel) {
        dao.insertGradeLevel(gradeLevel.toEntity())
    }

    override suspend fun deleteGradeLevel(id: String) {
        dao.deleteGradeLevel(id)
    }

    override fun getTeacherAssignmentsForSchool(schoolId: String): Flow<List<TeacherAssignment>> =
        dao.getTeacherAssignmentsForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getAssignmentsForTeacher(teacherId: String): Flow<List<TeacherAssignment>> =
        dao.getAssignmentsForTeacher(teacherId).map { list -> list.map { it.toDomain() } }

    override fun getAssignmentsForSection(sectionId: String): Flow<List<TeacherAssignment>> =
        dao.getAssignmentsForSection(sectionId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveTeacherAssignment(assignment: TeacherAssignment) {
        dao.insertTeacherAssignment(assignment.toEntity())
    }

    override suspend fun deleteTeacherAssignment(id: String) {
        dao.deleteTeacherAssignment(id)
    }

    override fun getEnrollmentsForSchool(schoolId: String): Flow<List<StudentEnrollment>> =
        dao.getEnrollmentsForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getEnrollmentsForClass(classId: String): Flow<List<StudentEnrollment>> =
        dao.getEnrollmentsForClass(classId).map { list -> list.map { it.toDomain() } }

    override fun getEnrollmentsForSection(sectionId: String): Flow<List<StudentEnrollment>> =
        dao.getEnrollmentsForSection(sectionId).map { list -> list.map { it.toDomain() } }

    override fun getEnrollmentsForStudent(studentId: String): Flow<List<StudentEnrollment>> =
        dao.getEnrollmentsForStudent(studentId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveStudentEnrollment(enrollment: StudentEnrollment) {
        dao.insertStudentEnrollment(enrollment.toEntity())
    }

    override suspend fun deleteStudentEnrollment(id: String) {
        dao.deleteStudentEnrollment(id)
    }
}
