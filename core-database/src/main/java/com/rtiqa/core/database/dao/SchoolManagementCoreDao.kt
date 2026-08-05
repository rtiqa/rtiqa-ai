package com.rtiqa.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rtiqa.core.database.entity.GradeLevelEntity
import com.rtiqa.core.database.entity.StudentEnrollmentEntity
import com.rtiqa.core.database.entity.TeacherAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolManagementCoreDao {

    // Grade Levels
    @Query("SELECT * FROM grade_levels WHERE schoolId = :schoolId ORDER BY levelSequence ASC")
    fun getGradeLevelsForSchool(schoolId: String): Flow<List<GradeLevelEntity>>

    @Query("SELECT * FROM grade_levels WHERE id = :id LIMIT 1")
    fun getGradeLevelById(id: String): Flow<GradeLevelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGradeLevel(gradeLevel: GradeLevelEntity)

    @Query("DELETE FROM grade_levels WHERE id = :id")
    suspend fun deleteGradeLevel(id: String)

    // Teacher Assignments
    @Query("SELECT * FROM teacher_assignments WHERE schoolId = :schoolId ORDER BY assignedAt DESC")
    fun getTeacherAssignmentsForSchool(schoolId: String): Flow<List<TeacherAssignmentEntity>>

    @Query("SELECT * FROM teacher_assignments WHERE teacherId = :teacherId")
    fun getAssignmentsForTeacher(teacherId: String): Flow<List<TeacherAssignmentEntity>>

    @Query("SELECT * FROM teacher_assignments WHERE sectionId = :sectionId")
    fun getAssignmentsForSection(sectionId: String): Flow<List<TeacherAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherAssignment(assignment: TeacherAssignmentEntity)

    @Query("DELETE FROM teacher_assignments WHERE id = :id")
    suspend fun deleteTeacherAssignment(id: String)

    // Student Enrollments
    @Query("SELECT * FROM student_enrollments WHERE schoolId = :schoolId ORDER BY enrolledAt DESC")
    fun getEnrollmentsForSchool(schoolId: String): Flow<List<StudentEnrollmentEntity>>

    @Query("SELECT * FROM student_enrollments WHERE classId = :classId")
    fun getEnrollmentsForClass(classId: String): Flow<List<StudentEnrollmentEntity>>

    @Query("SELECT * FROM student_enrollments WHERE sectionId = :sectionId")
    fun getEnrollmentsForSection(sectionId: String): Flow<List<StudentEnrollmentEntity>>

    @Query("SELECT * FROM student_enrollments WHERE studentId = :studentId")
    fun getEnrollmentsForStudent(studentId: String): Flow<List<StudentEnrollmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentEnrollment(enrollment: StudentEnrollmentEntity)

    @Query("DELETE FROM student_enrollments WHERE id = :id")
    suspend fun deleteStudentEnrollment(id: String)
}
