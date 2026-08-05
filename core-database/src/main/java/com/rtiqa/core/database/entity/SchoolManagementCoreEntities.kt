package com.rtiqa.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade_levels")
data class GradeLevelEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val name: String,
    val code: String,
    val levelSequence: Int,
    val stage: String
)

@Entity(tableName = "teacher_assignments")
data class TeacherAssignmentEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val teacherId: String,
    val teacherName: String,
    val subjectId: String,
    val subjectName: String,
    val sectionId: String,
    val sectionName: String,
    val classId: String,
    val academicYearId: String,
    val semesterId: String,
    val assignmentRole: String,
    val isPrimaryTeacher: Boolean,
    val assignedAt: Long
)

@Entity(tableName = "student_enrollments")
data class StudentEnrollmentEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val schoolId: String,
    val academicYearId: String,
    val classId: String,
    val className: String,
    val sectionId: String,
    val sectionName: String,
    val enrollmentStatus: String,
    val enrolledAt: Long
)
