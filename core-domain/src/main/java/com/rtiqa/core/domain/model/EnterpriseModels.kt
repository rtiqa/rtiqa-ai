package com.rtiqa.core.domain.model

data class School(
    val id: String,
    val name: String,
    val code: String,
    val address: String = "",
    val phone: String = "",
    val logoUrl: String? = null,
    val studentsCount: Int = 0,
    val teachersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class OrgType(val labelAr: String) {
    SCHOOL("مدرسة"),
    UNIVERSITY("جامعة"),
    INSTITUTE("معهد"),
    ACADEMY("أكاديمية")
}

enum class OrgStatus(val labelAr: String) {
    ACTIVE("نشطة"),
    SUSPENDED("معلقة"),
    PENDING("قيد المراجعة")
}

data class Organization(
    val id: String,
    val name: String,
    val type: OrgType,
    val code: String,
    val logoUrl: String? = null,
    val status: OrgStatus = OrgStatus.ACTIVE,
    val branchesCount: Int = 1,
    val studentsCount: Int = 0,
    val teachersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Branch(
    val id: String,
    val orgId: String,
    val name: String,
    val code: String,
    val address: String,
    val phone: String
)

data class AcademicYear(
    val id: String,
    val orgId: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean = false
)

data class Semester(
    val id: String,
    val academicYearId: String,
    val name: String,
    val order: Int,
    val isActive: Boolean = false
)

data class Department(
    val id: String,
    val orgId: String,
    val name: String,
    val code: String,
    val headName: String
)

data class Major(
    val id: String,
    val departmentId: String,
    val name: String,
    val code: String,
    val degreeType: String
)

data class Section(
    val id: String,
    val majorId: String,
    val semesterId: String,
    val branchId: String,
    val name: String,
    val capacity: Int,
    val studentsCount: Int = 0,
    val schoolId: String = "school_001"
)

data class Subject(
    val id: String,
    val majorId: String,
    val code: String,
    val name: String,
    val creditHours: Int,
    val schoolId: String = "school_001"
)

data class StudyPlan(
    val id: String,
    val majorId: String,
    val name: String,
    val totalCredits: Int,
    val version: String
)

enum class EnterpriseRole(val labelAr: String) {
    SUPER_ADMIN("مسؤول النظام الشامل"),
    ORG_ADMIN("مدير المؤسسة"),
    PRINCIPAL("مدير المدرسة"),
    VICE_PRINCIPAL("وكيل المدرسة"),
    TEACHER("معلم"),
    STUDENT("طالب"),
    PARENT("ولي أمر"),
    STAFF("موظف إداري")
}

enum class MemberStatus(val labelAr: String) {
    ACTIVE("نشط"),
    INACTIVE("غير نشط"),
    INVITATION_PENDING("في انتظار القبول")
}

data class EnterpriseMember(
    val id: String,
    val orgId: String,
    val name: String,
    val email: String,
    val role: EnterpriseRole,
    val department: String,
    val status: MemberStatus = MemberStatus.ACTIVE,
    val phone: String = "",
    val schoolId: String = "school_001"
)
