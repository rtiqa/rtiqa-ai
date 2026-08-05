package com.rtiqa.core.data.mapper

import com.rtiqa.core.database.entity.GradeLevelEntity
import com.rtiqa.core.database.entity.StudentEnrollmentEntity
import com.rtiqa.core.database.entity.TeacherAssignmentEntity
import com.rtiqa.core.domain.model.EducationStage
import com.rtiqa.core.domain.model.EnrollmentStatus
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.StudentEnrollment
import com.rtiqa.core.domain.model.TeacherAssignment
import com.rtiqa.core.domain.model.TeacherRole

fun GradeLevelEntity.toDomain(): GradeLevel = GradeLevel(
    id = id,
    schoolId = schoolId,
    name = name,
    code = code,
    levelSequence = levelSequence,
    stage = try { EducationStage.valueOf(stage) } catch (e: Exception) { EducationStage.PRIMARY }
)

fun GradeLevel.toEntity(): GradeLevelEntity = GradeLevelEntity(
    id = id,
    schoolId = schoolId,
    name = name,
    code = code,
    levelSequence = levelSequence,
    stage = stage.name
)

fun TeacherAssignmentEntity.toDomain(): TeacherAssignment = TeacherAssignment(
    id = id,
    schoolId = schoolId,
    teacherId = teacherId,
    teacherName = teacherName,
    subjectId = subjectId,
    subjectName = subjectName,
    sectionId = sectionId,
    sectionName = sectionName,
    classId = classId,
    academicYearId = academicYearId,
    semesterId = semesterId,
    assignmentRole = try { TeacherRole.valueOf(assignmentRole) } catch (e: Exception) { TeacherRole.PRIMARY_TEACHER },
    isPrimaryTeacher = isPrimaryTeacher,
    assignedAt = assignedAt
)

fun TeacherAssignment.toEntity(): TeacherAssignmentEntity = TeacherAssignmentEntity(
    id = id,
    schoolId = schoolId,
    teacherId = teacherId,
    teacherName = teacherName,
    subjectId = subjectId,
    subjectName = subjectName,
    sectionId = sectionId,
    sectionName = sectionName,
    classId = classId,
    academicYearId = academicYearId,
    semesterId = semesterId,
    assignmentRole = assignmentRole.name,
    isPrimaryTeacher = isPrimaryTeacher,
    assignedAt = assignedAt
)

fun StudentEnrollmentEntity.toDomain(): StudentEnrollment = StudentEnrollment(
    id = id,
    studentId = studentId,
    studentName = studentName,
    schoolId = schoolId,
    academicYearId = academicYearId,
    classId = classId,
    className = className,
    sectionId = sectionId,
    sectionName = sectionName,
    enrollmentStatus = try { EnrollmentStatus.valueOf(enrollmentStatus) } catch (e: Exception) { EnrollmentStatus.ENROLLED },
    enrolledAt = enrolledAt
)

fun StudentEnrollment.toEntity(): StudentEnrollmentEntity = StudentEnrollmentEntity(
    id = id,
    studentId = studentId,
    studentName = studentName,
    schoolId = schoolId,
    academicYearId = academicYearId,
    classId = classId,
    className = className,
    sectionId = sectionId,
    sectionName = sectionName,
    enrollmentStatus = enrollmentStatus.name,
    enrolledAt = enrolledAt
)
