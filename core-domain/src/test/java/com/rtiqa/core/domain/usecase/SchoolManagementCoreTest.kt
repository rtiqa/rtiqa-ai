package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.model.AcademicYearStatus
import com.rtiqa.core.domain.model.EducationStage
import com.rtiqa.core.domain.model.EnrollmentStatus
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.MemberStatus
import com.rtiqa.core.domain.model.Permission
import com.rtiqa.core.domain.model.RbacEvaluator
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.SchoolStatus
import com.rtiqa.core.domain.model.StudentEnrollment
import com.rtiqa.core.domain.model.TeacherAssignment
import com.rtiqa.core.domain.model.TeacherRole
import com.rtiqa.core.domain.repository.SchoolManagementCoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSchoolManagementCoreRepository : SchoolManagementCoreRepository {
    private val gradeLevels = MutableStateFlow<List<GradeLevel>>(emptyList())
    private val teacherAssignments = MutableStateFlow<List<TeacherAssignment>>(emptyList())
    private val studentEnrollments = MutableStateFlow<List<StudentEnrollment>>(emptyList())

    override fun getGradeLevelsForSchool(schoolId: String): Flow<List<GradeLevel>> =
        gradeLevels.map { list -> list.filter { it.schoolId == schoolId } }

    override fun getGradeLevelById(id: String): Flow<GradeLevel?> =
        gradeLevels.map { list -> list.find { it.id == id } }

    override suspend fun saveGradeLevel(gradeLevel: GradeLevel) {
        val current = gradeLevels.value.toMutableList()
        current.removeAll { it.id == gradeLevel.id }
        current.add(gradeLevel)
        gradeLevels.value = current
    }

    override suspend fun deleteGradeLevel(id: String) {
        val current = gradeLevels.value.toMutableList()
        current.removeAll { it.id == id }
        gradeLevels.value = current
    }

    override fun getTeacherAssignmentsForSchool(schoolId: String): Flow<List<TeacherAssignment>> =
        teacherAssignments.map { list -> list.filter { it.schoolId == schoolId } }

    override fun getAssignmentsForTeacher(teacherId: String): Flow<List<TeacherAssignment>> =
        teacherAssignments.map { list -> list.filter { it.teacherId == teacherId } }

    override fun getAssignmentsForSection(sectionId: String): Flow<List<TeacherAssignment>> =
        teacherAssignments.map { list -> list.filter { it.sectionId == sectionId } }

    override suspend fun saveTeacherAssignment(assignment: TeacherAssignment) {
        val current = teacherAssignments.value.toMutableList()
        current.removeAll { it.id == assignment.id }
        current.add(assignment)
        teacherAssignments.value = current
    }

    override suspend fun deleteTeacherAssignment(id: String) {
        val current = teacherAssignments.value.toMutableList()
        current.removeAll { it.id == id }
        teacherAssignments.value = current
    }

    override fun getEnrollmentsForSchool(schoolId: String): Flow<List<StudentEnrollment>> =
        studentEnrollments.map { list -> list.filter { it.schoolId == schoolId } }

    override fun getEnrollmentsForClass(classId: String): Flow<List<StudentEnrollment>> =
        studentEnrollments.map { list -> list.filter { it.classId == classId } }

    override fun getEnrollmentsForSection(sectionId: String): Flow<List<StudentEnrollment>> =
        studentEnrollments.map { list -> list.filter { it.sectionId == sectionId } }

    override fun getEnrollmentsForStudent(studentId: String): Flow<List<StudentEnrollment>> =
        studentEnrollments.map { list -> list.filter { it.studentId == studentId } }

    override suspend fun saveStudentEnrollment(enrollment: StudentEnrollment) {
        val current = studentEnrollments.value.toMutableList()
        current.removeAll { it.id == enrollment.id }
        current.add(enrollment)
        studentEnrollments.value = current
    }

    override suspend fun deleteStudentEnrollment(id: String) {
        val current = studentEnrollments.value.toMutableList()
        current.removeAll { it.id == id }
        studentEnrollments.value = current
    }
}

class SchoolManagementCoreTest {

    private lateinit var repository: FakeSchoolManagementCoreRepository
    private lateinit var getGradeLevelsUseCase: GetGradeLevelsForSchoolUseCase
    private lateinit var saveGradeLevelUseCase: SaveGradeLevelUseCase
    private lateinit var deleteGradeLevelUseCase: DeleteGradeLevelUseCase
    private lateinit var getTeacherAssignmentsUseCase: GetTeacherAssignmentsForSchoolUseCase
    private lateinit var saveTeacherAssignmentUseCase: SaveTeacherAssignmentUseCase
    private lateinit var getEnrollmentsUseCase: GetEnrollmentsForSchoolUseCase
    private lateinit var saveEnrollmentUseCase: SaveStudentEnrollmentUseCase
    private lateinit var evaluatePermissionUseCase: EvaluateUserPermissionUseCase
    private lateinit var checkSchoolAccessUseCase: CheckSchoolAccessUseCase

    @Before
    fun setUp() {
        repository = FakeSchoolManagementCoreRepository()
        getGradeLevelsUseCase = GetGradeLevelsForSchoolUseCase(repository)
        saveGradeLevelUseCase = SaveGradeLevelUseCase(repository)
        deleteGradeLevelUseCase = DeleteGradeLevelUseCase(repository)
        getTeacherAssignmentsUseCase = GetTeacherAssignmentsForSchoolUseCase(repository)
        saveTeacherAssignmentUseCase = SaveTeacherAssignmentUseCase(repository)
        getEnrollmentsUseCase = GetEnrollmentsForSchoolUseCase(repository)
        saveEnrollmentUseCase = SaveStudentEnrollmentUseCase(repository)
        evaluatePermissionUseCase = EvaluateUserPermissionUseCase()
        checkSchoolAccessUseCase = CheckSchoolAccessUseCase()
    }

    @Test
    fun `test GradeLevel CRUD flow`() = runBlocking {
        val gradeLevel = GradeLevel(
            id = "gl_10",
            schoolId = "school_001",
            name = "الصف العاشر",
            code = "G10",
            levelSequence = 10,
            stage = EducationStage.SECONDARY
        )

        saveGradeLevelUseCase(gradeLevel)

        val retrieved = getGradeLevelsUseCase("school_001").first()
        assertEquals(1, retrieved.size)
        assertEquals("الصف العاشر", retrieved.first().name)
        assertEquals(EducationStage.SECONDARY, retrieved.first().stage)

        deleteGradeLevelUseCase("gl_10")
        val emptyList = getGradeLevelsUseCase("school_001").first()
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun `test Teacher Assignment flow`() = runBlocking {
        val assignment = TeacherAssignment(
            id = "ta_01",
            schoolId = "school_001",
            teacherId = "tch_101",
            teacherName = "أحمد القحطاني",
            subjectId = "sub_math",
            subjectName = "الرياضيات",
            sectionId = "sec_10a",
            sectionName = "شعبة أ",
            classId = "cls_10",
            assignmentRole = TeacherRole.PRIMARY_TEACHER
        )

        saveTeacherAssignmentUseCase(assignment)

        val assignments = getTeacherAssignmentsUseCase("school_001").first()
        assertEquals(1, assignments.size)
        assertEquals("أحمد القحطاني", assignments.first().teacherName)
        assertEquals(TeacherRole.PRIMARY_TEACHER, assignments.first().assignmentRole)
    }

    @Test
    fun `test Student Enrollment flow`() = runBlocking {
        val enrollment = StudentEnrollment(
            id = "enr_01",
            studentId = "std_201",
            studentName = "سارة العتيبي",
            schoolId = "school_001",
            classId = "cls_10",
            className = "الصف العاشر",
            sectionId = "sec_10a",
            sectionName = "شعبة أ",
            enrollmentStatus = EnrollmentStatus.ENROLLED
        )

        saveEnrollmentUseCase(enrollment)

        val enrollments = getEnrollmentsUseCase("school_001").first()
        assertEquals(1, enrollments.size)
        assertEquals("سارة العتيبي", enrollments.first().studentName)
        assertEquals(EnrollmentStatus.ENROLLED, enrollments.first().enrollmentStatus)
    }

    @Test
    fun `test Role-Based Access Control permissions`() {
        // Super Admin has all permissions
        assertTrue(evaluatePermissionUseCase(EnterpriseRole.SUPER_ADMIN, Permission.MANAGE_ORGANIZATION))
        assertTrue(evaluatePermissionUseCase(EnterpriseRole.SUPER_ADMIN, Permission.ASSIGN_TEACHERS))

        // Principal permissions
        assertTrue(evaluatePermissionUseCase(EnterpriseRole.PRINCIPAL, Permission.MANAGE_CLASSES))
        assertTrue(evaluatePermissionUseCase(EnterpriseRole.PRINCIPAL, Permission.ENROLL_STUDENTS))

        // Student has no manage permissions
        assertFalse(evaluatePermissionUseCase(EnterpriseRole.STUDENT, Permission.MANAGE_SCHOOL))
        assertFalse(evaluatePermissionUseCase(EnterpriseRole.STUDENT, Permission.ASSIGN_TEACHERS))
    }

    @Test
    fun `test School multi-tenant access control`() {
        val principalMember = EnterpriseMember(
            id = "user_01",
            orgId = "org_01",
            name = "المدير علي",
            email = "principal@school.edu",
            role = EnterpriseRole.PRINCIPAL,
            department = "Administration",
            status = MemberStatus.ACTIVE,
            schoolId = "school_001"
        )

        assertTrue(checkSchoolAccessUseCase(principalMember, "school_001"))
        assertFalse(checkSchoolAccessUseCase(principalMember, "school_002"))

        val superAdmin = principalMember.copy(role = EnterpriseRole.SUPER_ADMIN)
        assertTrue(checkSchoolAccessUseCase(superAdmin, "school_002"))
    }
}
