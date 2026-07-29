package com.rtiqa.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val code: String,
    val logoUrl: String?,
    val status: String,
    val branchesCount: Int,
    val studentsCount: Int,
    val teachersCount: Int,
    val createdAt: Long
)

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val name: String,
    val code: String,
    val address: String,
    val phone: String
)

@Entity(tableName = "academic_years")
data class AcademicYearEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean
)

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey val id: String,
    val academicYearId: String,
    val name: String,
    val order: Int,
    val isActive: Boolean
)

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val name: String,
    val code: String,
    val headName: String
)

@Entity(tableName = "majors")
data class MajorEntity(
    @PrimaryKey val id: String,
    val departmentId: String,
    val name: String,
    val code: String,
    val degreeType: String
)

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey val id: String,
    val majorId: String,
    val semesterId: String,
    val branchId: String,
    val name: String,
    val capacity: Int,
    val studentsCount: Int
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val majorId: String,
    val code: String,
    val name: String,
    val creditHours: Int
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey val id: String,
    val majorId: String,
    val name: String,
    val totalCredits: Int,
    val version: String
)

@Entity(tableName = "enterprise_members")
data class EnterpriseMemberEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val name: String,
    val email: String,
    val role: String,
    val department: String,
    val status: String,
    val phone: String
)
