package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.StudentEnrollment
import com.rtiqa.core.domain.model.TeacherAssignment
import kotlinx.coroutines.flow.Flow

interface SchoolManagementCoreRepository {
    // Grade Levels
    fun getGradeLevelsForSchool(schoolId: String): Flow<List<GradeLevel>>
    fun getGradeLevelById(id: String): Flow<GradeLevel?>
    suspend fun saveGradeLevel(gradeLevel: GradeLevel)
    suspend fun deleteGradeLevel(id: String)

    // Teacher Assignments
    fun getTeacherAssignmentsForSchool(schoolId: String): Flow<List<TeacherAssignment>>
    fun getAssignmentsForTeacher(teacherId: String): Flow<List<TeacherAssignment>>
    fun getAssignmentsForSection(sectionId: String): Flow<List<TeacherAssignment>>
    suspend fun saveTeacherAssignment(assignment: TeacherAssignment)
    suspend fun deleteTeacherAssignment(id: String)

    // Student Enrollments
    fun getEnrollmentsForSchool(schoolId: String): Flow<List<StudentEnrollment>>
    fun getEnrollmentsForClass(classId: String): Flow<List<StudentEnrollment>>
    fun getEnrollmentsForSection(sectionId: String): Flow<List<StudentEnrollment>>
    fun getEnrollmentsForStudent(studentId: String): Flow<List<StudentEnrollment>>
    suspend fun saveStudentEnrollment(enrollment: StudentEnrollment)
    suspend fun deleteStudentEnrollment(id: String)
}
