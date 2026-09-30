package com.rtiqa.backend.gradebook

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Timestamp
import java.util.UUID

class GradebookRepositoryTest {

    private lateinit var connection: Connection
    private val repository = GradebookRepository()

    private val tenantA = UUID.randomUUID()
    private val tenantB = UUID.randomUUID()
    private val class1Id = UUID.randomUUID()
    private val class2Id = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb_${UUID.randomUUID()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
        
        connection.createStatement().use { stmt ->
            val ddlStatements = listOf(
                """
                CREATE TABLE organizations (
                    id UUID PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    slug VARCHAR(100) NOT NULL,
                    type VARCHAR(50) DEFAULT 'SCHOOL',
                    status VARCHAR(50) DEFAULT 'ACTIVE',
                    created_at TIMESTAMP DEFAULT NOW()
                )
                """.trimIndent(),
                """
                CREATE TABLE profiles (
                    id UUID PRIMARY KEY,
                    email VARCHAR(255) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    avatar_url TEXT,
                    created_at TIMESTAMP DEFAULT NOW()
                )
                """.trimIndent(),
                """
                CREATE TABLE academic_classes (
                    id UUID PRIMARY KEY,
                    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
                    name VARCHAR(255) NOT NULL,
                    academic_year VARCHAR(100),
                    term VARCHAR(100),
                    created_at TIMESTAMP DEFAULT NOW()
                )
                """.trimIndent(),
                """
                CREATE TABLE class_students (
                    id UUID PRIMARY KEY,
                    class_id UUID NOT NULL REFERENCES academic_classes(id) ON DELETE CASCADE,
                    student_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
                    student_number VARCHAR(100),
                    created_at TIMESTAMP DEFAULT NOW(),
                    CONSTRAINT unique_class_student UNIQUE (class_id, student_id)
                )
                """.trimIndent(),
                """
                CREATE TABLE gradebook_assessments (
                    id UUID PRIMARY KEY,
                    class_id UUID NOT NULL REFERENCES academic_classes(id) ON DELETE CASCADE,
                    title VARCHAR(255) NOT NULL,
                    max_score NUMERIC NOT NULL,
                    weight NUMERIC NULL,
                    created_at TIMESTAMP DEFAULT NOW()
                )
                """.trimIndent(),
                """
                CREATE TABLE gradebook_scores (
                    id UUID PRIMARY KEY,
                    assessment_id UUID NOT NULL REFERENCES gradebook_assessments(id) ON DELETE CASCADE,
                    student_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
                    score NUMERIC NULL,
                    updated_at TIMESTAMP DEFAULT NOW(),
                    CONSTRAINT unique_assessment_student_score UNIQUE (assessment_id, student_id)
                )
                """.trimIndent()
            )
            for (ddl in ddlStatements) {
                stmt.execute(ddl)
            }
        }

        insertOrg(tenantA, "مدارس الارتقاء الأهلية")
        insertOrg(tenantB, "مدارس الأفق العالمية")
    }

    @AfterEach
    fun tearDown() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    private fun insertOrg(id: UUID, name: String) {
        connection.prepareStatement("INSERT INTO organizations (id, name, slug) VALUES (?, ?, ?)").use {
            it.setObject(1, id)
            it.setString(2, name)
            it.setString(3, "slug-${id.toString().take(8)}")
            it.executeUpdate()
        }
    }

    private fun insertClass(id: UUID, orgId: UUID, name: String, year: String? = "1447", term: String? = "الفصل الثاني") {
        connection.prepareStatement("INSERT INTO academic_classes (id, organization_id, name, academic_year, term) VALUES (?, ?, ?, ?, ?)").use {
            it.setObject(1, id)
            it.setObject(2, orgId)
            it.setString(3, name)
            it.setString(4, year)
            it.setString(5, term)
            it.executeUpdate()
        }
    }

    private fun insertProfile(id: UUID, name: String, email: String) {
        connection.prepareStatement("INSERT INTO profiles (id, name, email) VALUES (?, ?, ?)").use {
            it.setObject(1, id)
            it.setString(2, name)
            it.setString(3, email)
            it.executeUpdate()
        }
    }

    private fun enrollStudent(classId: UUID, studentId: UUID, studentNumber: String?) {
        connection.prepareStatement("INSERT INTO class_students (id, class_id, student_id, student_number) VALUES (?, ?, ?, ?)").use {
            it.setObject(1, UUID.randomUUID())
            it.setObject(2, classId)
            it.setObject(3, studentId)
            it.setString(4, studentNumber)
            it.executeUpdate()
        }
    }

    private fun insertAssessment(id: UUID, classId: UUID, title: String, maxScore: Double, weight: Double?, createdAt: Timestamp? = null) {
        connection.prepareStatement("INSERT INTO gradebook_assessments (id, class_id, title, max_score, weight, created_at) VALUES (?, ?, ?, ?, ?, ?)").use {
            it.setObject(1, id)
            it.setObject(2, classId)
            it.setString(3, title)
            it.setDouble(4, maxScore)
            if (weight != null) it.setDouble(5, weight) else it.setNull(5, java.sql.Types.NUMERIC)
            it.setTimestamp(6, createdAt ?: Timestamp(System.currentTimeMillis()))
            it.executeUpdate()
        }
    }

    private fun insertScore(assessmentId: UUID, studentId: UUID, score: Double?) {
        connection.prepareStatement("INSERT INTO gradebook_scores (id, assessment_id, student_id, score) VALUES (?, ?, ?, ?)").use {
            it.setObject(1, UUID.randomUUID())
            it.setObject(2, assessmentId)
            it.setObject(3, studentId)
            if (score != null) it.setDouble(4, score) else it.setNull(4, java.sql.Types.NUMERIC)
            it.executeUpdate()
        }
    }

    @Test
    fun existingClass_returnsRealClassId() {
        insertClass(class1Id, tenantA, "الصف الثالث الثانوي - أ")
        val result = repository.getClassGradebook(connection, class1Id, tenantA)
        assertTrue(result is GetClassGradebookResult.Success)
        val success = result as GetClassGradebookResult.Success
        assertEquals(class1Id.toString(), success.gradebook.classId)
    }

    @Test
    fun existingEmptyClass_returnsSuccessWithEmptyLists() {
        insertClass(class1Id, tenantA, "فصل فارغ")
        val result = repository.getClassGradebook(connection, class1Id, tenantA)
        assertTrue(result is GetClassGradebookResult.Success)
        val success = result as GetClassGradebookResult.Success
        assertEquals(class1Id.toString(), success.gradebook.classId)
        assertTrue(success.gradebook.students.isEmpty())
        assertTrue(success.gradebook.assessments.isEmpty())
        assertTrue(success.gradebook.scores.isEmpty())
    }

    @Test
    fun nonexistentClass_returnsClassNotFound() {
        val nonExistentId = UUID.randomUUID()
        val result = repository.getClassGradebook(connection, nonExistentId, tenantA)
        assertEquals(GetClassGradebookResult.ClassNotFound, result)
    }

    @Test
    fun classBelongingToAnotherTenant_returnsClassNotFound() {
        insertClass(class1Id, tenantB, "فصل في مستأجر آخر")
        val result = repository.getClassGradebook(connection, class1Id, tenantA)
        assertEquals(GetClassGradebookResult.ClassNotFound, result)
    }

    @Test
    fun students_comeFromProfilesAndClassStudents_orderedByName() {
        insertClass(class1Id, tenantA, "فصل العلوم")
        val student1 = UUID.randomUUID()
        val student2 = UUID.randomUUID()
        insertProfile(student1, "سعد العتيبي", "saad@test.com")
        insertProfile(student2, "أحمد الدوسري", "ahmed@test.com")
        enrollStudent(class1Id, student1, "STD-200")
        enrollStudent(class1Id, student2, "STD-100")

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success
        val students = result.gradebook.students

        assertEquals(2, students.size)
        // Ordered by name ASC: "أحمد الدوسري" then "سعد العتيبي"
        assertEquals(student2.toString(), students[0].studentId)
        assertEquals("أحمد الدوسري", students[0].displayName)
        assertEquals("STD-100", students[0].studentNumber)

        assertEquals(student1.toString(), students[1].studentId)
        assertEquals("سعد العتيبي", students[1].displayName)
        assertEquals("STD-200", students[1].studentNumber)
    }

    @Test
    fun nullableStudentNumber_remainsNull() {
        insertClass(class1Id, tenantA, "فصل الرياضيات")
        val studentId = UUID.randomUUID()
        insertProfile(studentId, "خالد الشمري", "khalid@test.com")
        enrollStudent(class1Id, studentId, null)

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success
        assertEquals(1, result.gradebook.students.size)
        val s = result.gradebook.students[0]
        assertEquals(studentId.toString(), s.studentId)
        assertNull(s.studentNumber)
    }

    @Test
    fun assessments_preserveMaxScore() {
        insertClass(class1Id, tenantA, "فصل الفيزياء")
        val asmId = UUID.randomUUID()
        insertAssessment(asmId, class1Id, "الاختبار النصفي", 40.0, 0.3)

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success
        assertEquals(1, result.gradebook.assessments.size)
        val asm = result.gradebook.assessments[0]
        assertEquals(asmId.toString(), asm.assessmentId)
        assertEquals("الاختبار النصفي", asm.title)
        assertEquals(40.0, asm.maxScore, 0.001)
        assertEquals(0.3, asm.weight!!, 0.001)
    }

    @Test
    fun nullableAssessmentWeight_remainsNull() {
        insertClass(class1Id, tenantA, "فصل الكيمياء")
        val asmId = UUID.randomUUID()
        insertAssessment(asmId, class1Id, "نشاط صفي", 10.0, null)

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success
        assertEquals(1, result.gradebook.assessments.size)
        val asm = result.gradebook.assessments[0]
        assertNull(asm.weight)
    }

    @Test
    fun sqlNullScore_remainsKotlinNull_notZero() {
        insertClass(class1Id, tenantA, "فصل الأحياء")
        val student1 = UUID.randomUUID()
        val student2 = UUID.randomUUID()
        insertProfile(student1, "طالب لم يختبر", "s1@test.com")
        insertProfile(student2, "طالب أخذ صفر", "s2@test.com")
        enrollStudent(class1Id, student1, "101")
        enrollStudent(class1Id, student2, "102")

        val asmId = UUID.randomUUID()
        insertAssessment(asmId, class1Id, "واجب 1", 10.0, 0.1)

        insertScore(asmId, student1, null) // SQL NULL
        insertScore(asmId, student2, 0.0)  // Explicit 0.0

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success
        val scoreNull = result.gradebook.scores.find { it.studentId == student1.toString() }
        val scoreZero = result.gradebook.scores.find { it.studentId == student2.toString() }

        assertNotNull(scoreNull)
        assertNull(scoreNull!!.score, "SQL NULL score must be Kotlin null, not 0.0")

        assertNotNull(scoreZero)
        assertEquals(0.0, scoreZero!!.score!!, 0.001, "Real 0.0 score must remain 0.0")
    }

    @Test
    fun scoreFromAssessmentOutsideRequestedClass_isExcluded() {
        insertClass(class1Id, tenantA, "فصل 1")
        insertClass(class2Id, tenantA, "فصل 2")

        val studentId = UUID.randomUUID()
        insertProfile(studentId, "طالب مشترك", "shared@test.com")
        enrollStudent(class1Id, studentId, "1")
        enrollStudent(class2Id, studentId, "1")

        val asm1 = UUID.randomUUID()
        val asm2 = UUID.randomUUID()
        insertAssessment(asm1, class1Id, "تقييم فصل 1", 20.0, 0.2)
        insertAssessment(asm2, class2Id, "تقييم فصل 2", 30.0, 0.3)

        insertScore(asm1, studentId, 18.0)
        insertScore(asm2, studentId, 28.0)

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success
        assertEquals(1, result.gradebook.scores.size)
        assertEquals(asm1.toString(), result.gradebook.scores[0].assessmentId)
        assertEquals(18.0, result.gradebook.scores[0].score!!, 0.001)
    }

    @Test
    fun scoreForStudentNotEnrolledInRequestedClass_isExcluded() {
        insertClass(class1Id, tenantA, "فصل أ")
        insertClass(class2Id, tenantA, "فصل ب")

        val studentInClass = UUID.randomUUID()
        val studentNotInClass = UUID.randomUUID()
        insertProfile(studentInClass, "طالب مقيد", "in@test.com")
        insertProfile(studentNotInClass, "طالب غير مقيد", "out@test.com")

        enrollStudent(class1Id, studentInClass, "1")
        enrollStudent(class2Id, studentNotInClass, "2") // enrolled in class 2, not class 1

        val asmId = UUID.randomUUID()
        insertAssessment(asmId, class1Id, "اختبار فصلي", 50.0, 0.5)

        insertScore(asmId, studentInClass, 45.0)
        insertScore(asmId, studentNotInClass, 35.0) // rogue score row for unenrolled student

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success
        assertEquals(1, result.gradebook.scores.size)
        assertEquals(studentInClass.toString(), result.gradebook.scores[0].studentId)
        assertFalse(result.gradebook.scores.any { it.studentId == studentNotInClass.toString() })
    }

    @Test
    fun noFabricatedIdentitiesOrData_areReturned() {
        insertClass(class1Id, tenantA, "فصل موثق")
        val studentId = UUID.randomUUID()
        insertProfile(studentId, "طالب حقيقي", "real@test.com")
        enrollStudent(class1Id, studentId, "NUM-42")

        val asmId = UUID.randomUUID()
        insertAssessment(asmId, class1Id, "اختبار حقيقي", 25.0, 0.2)
        insertScore(asmId, studentId, 24.5)

        val result = repository.getClassGradebook(connection, class1Id, tenantA) as GetClassGradebookResult.Success

        val gradebook = result.gradebook
        assertEquals(class1Id.toString(), gradebook.classId)
        assertEquals(1, gradebook.students.size)
        assertEquals(studentId.toString(), gradebook.students[0].studentId)
        assertEquals("طالب حقيقي", gradebook.students[0].displayName)
        assertEquals("NUM-42", gradebook.students[0].studentNumber)

        assertEquals(1, gradebook.assessments.size)
        assertEquals(asmId.toString(), gradebook.assessments[0].assessmentId)
        assertEquals("اختبار حقيقي", gradebook.assessments[0].title)

        assertEquals(1, gradebook.scores.size)
        assertEquals(studentId.toString(), gradebook.scores[0].studentId)
        assertEquals(asmId.toString(), gradebook.scores[0].assessmentId)
        assertEquals(24.5, gradebook.scores[0].score!!, 0.001)

        val allIds = listOf(gradebook.classId) + gradebook.students.map { it.studentId } + gradebook.assessments.map { it.assessmentId }
        for (id in allIds) {
            assertFalse(id.contains("cls_001") || id.contains("cls-101") || id.contains("org_") || id.contains("school_") || id.contains("std_001"))
        }
    }
}
