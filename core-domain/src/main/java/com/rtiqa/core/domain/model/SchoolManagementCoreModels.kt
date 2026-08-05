package com.rtiqa.core.domain.model

enum class SchoolStatus(val labelAr: String) {
    ACTIVE("نشطة"),
    INACTIVE("غير نشطة"),
    SUSPENDED("معلقة")
}

enum class AcademicYearStatus(val labelAr: String) {
    UPCOMING("قادم"),
    ACTIVE("نشط حالياً"),
    COMPLETED("مكتمل"),
    ARCHIVED("مؤرشف")
}

enum class EducationStage(val labelAr: String) {
    PRIMARY("المرحلة الابتدائية"),
    MIDDLE("المرحلة المتوسطة / الإعدادية"),
    SECONDARY("المرحلة الثانوية"),
    HIGHER_EDUCATION("التعليم العالي")
}

enum class TeacherRole(val labelAr: String) {
    PRIMARY_TEACHER("معلم أساسي"),
    SUBJECT_TEACHER("معلم مادة"),
    ASSISTANT_TEACHER("معلم مساعد"),
    SUBSTITUTE("معلم بديـل")
}

enum class EnrollmentStatus(val labelAr: String) {
    ENROLLED("مسجل"),
    PROMOTED("منقول للصف الأعلى"),
    TRANSFERRED("محول لمدرسة أخرى"),
    GRADUATED("متخرج"),
    DROPPED("منسحب / منقطع"),
    SUSPENDED("موقوف مؤقتاً")
}

data class GradeLevel(
    val id: String,
    val schoolId: String = "school_001",
    val name: String,
    val code: String,
    val levelSequence: Int,
    val stage: EducationStage = EducationStage.PRIMARY
)

data class TeacherAssignment(
    val id: String,
    val schoolId: String = "school_001",
    val teacherId: String,
    val teacherName: String,
    val subjectId: String,
    val subjectName: String,
    val sectionId: String,
    val sectionName: String,
    val classId: String = "",
    val academicYearId: String = "ay_2026",
    val semesterId: String = "sem_001",
    val assignmentRole: TeacherRole = TeacherRole.PRIMARY_TEACHER,
    val isPrimaryTeacher: Boolean = true,
    val assignedAt: Long = System.currentTimeMillis()
)

data class StudentEnrollment(
    val id: String,
    val studentId: String,
    val studentName: String,
    val schoolId: String = "school_001",
    val academicYearId: String = "ay_2026",
    val classId: String,
    val className: String,
    val sectionId: String,
    val sectionName: String,
    val enrollmentStatus: EnrollmentStatus = EnrollmentStatus.ENROLLED,
    val enrolledAt: Long = System.currentTimeMillis()
)

enum class Permission(val key: String, val descriptionAr: String) {
    MANAGE_ORGANIZATION("org:manage", "إدارة المؤسسة التعليمية"),
    MANAGE_SCHOOL("school:manage", "إدارة المدرسة"),
    MANAGE_ACADEMIC_YEARS("academic_year:manage", "إدارة الأعوام الدراسية"),
    MANAGE_TERMS("term:manage", "إدارة الفصول الدراسية"),
    MANAGE_GRADES("grade:manage", "إدارة المراحل والدرجات"),
    MANAGE_CLASSES("class:manage", "إدارة الصفوف والتعليم"),
    MANAGE_SECTIONS("section:manage", "إدارة الشعب الدراسية"),
    MANAGE_SUBJECTS("subject:manage", "إدارة المواد والمناهج"),
    ASSIGN_TEACHERS("teacher:assign", "تعيين وسندات المعلمين"),
    ENROLL_STUDENTS("student:enroll", "تسجيل وقبول الطلاب"),
    MANAGE_STUDENTS("student:manage", "إدارة ملفات الطلاب"),
    MANAGE_TEACHERS("teacher:manage", "إدارة الكادر التعليمي"),
    VIEW_REPORTS("report:view", "عرض التقارير والأداء"),
    VIEW_ANALYTICS("analytics:view", "عرض تحليلات المنظومة")
}

object RbacEvaluator {
    private val rolePermissions: Map<EnterpriseRole, Set<Permission>> = mapOf(
        EnterpriseRole.SUPER_ADMIN to Permission.entries.toSet(),
        EnterpriseRole.ORG_ADMIN to setOf(
            Permission.MANAGE_ORGANIZATION, Permission.MANAGE_SCHOOL,
            Permission.MANAGE_ACADEMIC_YEARS, Permission.MANAGE_TERMS, Permission.MANAGE_GRADES,
            Permission.MANAGE_CLASSES, Permission.MANAGE_SECTIONS, Permission.MANAGE_SUBJECTS,
            Permission.ASSIGN_TEACHERS, Permission.ENROLL_STUDENTS, Permission.MANAGE_STUDENTS,
            Permission.MANAGE_TEACHERS, Permission.VIEW_REPORTS, Permission.VIEW_ANALYTICS
        ),
        EnterpriseRole.PRINCIPAL to setOf(
            Permission.MANAGE_SCHOOL, Permission.MANAGE_ACADEMIC_YEARS, Permission.MANAGE_TERMS,
            Permission.MANAGE_GRADES, Permission.MANAGE_CLASSES, Permission.MANAGE_SECTIONS,
            Permission.MANAGE_SUBJECTS, Permission.ASSIGN_TEACHERS, Permission.ENROLL_STUDENTS,
            Permission.MANAGE_STUDENTS, Permission.MANAGE_TEACHERS, Permission.VIEW_REPORTS, Permission.VIEW_ANALYTICS
        ),
        EnterpriseRole.VICE_PRINCIPAL to setOf(
            Permission.MANAGE_CLASSES, Permission.MANAGE_SECTIONS, Permission.MANAGE_SUBJECTS,
            Permission.ASSIGN_TEACHERS, Permission.ENROLL_STUDENTS, Permission.MANAGE_STUDENTS,
            Permission.VIEW_REPORTS, Permission.VIEW_ANALYTICS
        ),
        EnterpriseRole.TEACHER to setOf(
            Permission.VIEW_REPORTS
        ),
        EnterpriseRole.STAFF to setOf(
            Permission.ENROLL_STUDENTS, Permission.MANAGE_STUDENTS, Permission.VIEW_REPORTS
        ),
        EnterpriseRole.STUDENT to emptySet(),
        EnterpriseRole.PARENT to emptySet()
    )

    fun getPermissionsForRole(role: EnterpriseRole): Set<Permission> {
        return rolePermissions[role] ?: emptySet()
    }

    fun hasPermission(role: EnterpriseRole, permission: Permission): Boolean {
        return getPermissionsForRole(role).contains(permission)
    }

    fun canAccessSchool(member: EnterpriseMember, targetSchoolId: String): Boolean {
        if (member.role == EnterpriseRole.SUPER_ADMIN || member.role == EnterpriseRole.ORG_ADMIN) return true
        return member.schoolId == targetSchoolId
    }
}
