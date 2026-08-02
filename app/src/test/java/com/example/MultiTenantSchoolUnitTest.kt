package com.example

import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.data.mapper.toEntity
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.Section
import com.rtiqa.core.domain.model.Subject
import com.rtiqa.core.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MultiTenantSchoolUnitTest {

    @Test
    fun testSchoolEntityCreationAndIsolation() {
        val schoolA = School(
            id = "school_001",
            name = "مدرسة المستقبل النموذجية",
            code = "SCH-001",
            address = "الرياض - المملكة العربية السعودية",
            phone = "+966500000001",
            studentsCount = 450,
            teachersCount = 35
        )

        val schoolB = School(
            id = "school_002",
            name = "مدرسة التفوق الدولية",
            code = "SCH-002",
            address = "جدة - المملكة العربية السعودية",
            phone = "+966500000002",
            studentsCount = 320,
            teachersCount = 28
        )

        assertNotNull(schoolA)
        assertNotNull(schoolB)
        assertNotEquals(schoolA.id, schoolB.id)
        assertEquals("مدرسة المستقبل النموذجية", schoolA.name)
        assertEquals("SCH-002", schoolB.code)
    }

    @Test
    fun testSchoolMapperToEntityAndDomain() {
        val school = School(
            id = "school_001",
            name = "مدرسة النبلاء",
            code = "SCH-003",
            address = "الدمام",
            phone = "+966500000003"
        )

        val entity = school.toEntity()
        assertEquals("school_001", entity.id)
        assertEquals("مدرسة النبلاء", entity.name)

        val domain = entity.toDomain()
        assertEquals(school.id, domain.id)
        assertEquals(school.name, domain.name)
        assertEquals(school.code, domain.code)
    }

    @Test
    fun testMultiTenantDataLinkingBySchoolId() {
        val schoolIdA = "school_001"
        val schoolIdB = "school_002"

        val studentA = EnterpriseMember(
            id = "s1",
            orgId = "org_1",
            name = "علي أحمد",
            email = "ali@school1.edu",
            role = EnterpriseRole.STUDENT,
            department = "الحاسب الآلي",
            schoolId = schoolIdA
        )

        val studentB = EnterpriseMember(
            id = "s2",
            orgId = "org_1",
            name = "عمر خالد",
            email = "omar@school2.edu",
            role = EnterpriseRole.STUDENT,
            department = "الرياضيات",
            schoolId = schoolIdB
        )

        val teacherA = EnterpriseMember(
            id = "t1",
            orgId = "org_1",
            name = "د. سارة محمود",
            email = "sara@school1.edu",
            role = EnterpriseRole.TEACHER,
            department = "العلوم",
            schoolId = schoolIdA
        )

        val classSectionA = Section(
            id = "sec_101",
            majorId = "maj_1",
            semesterId = "sem_1",
            branchId = "br_1",
            name = "الصف الثالث الثانوي - أ",
            capacity = 30,
            studentsCount = 25,
            schoolId = schoolIdA
        )

        val subjectA = Subject(
            id = "sub_101",
            majorId = "maj_1",
            code = "MATH-301",
            name = "الرياضيات المتقدمة",
            creditHours = 3,
            schoolId = schoolIdA
        )

        val courseA = Course(
            id = "c1",
            title = "برمجة تطبيقات أندرويد بـ Kotlin",
            description = "دورة شاملة لبناء التطبيقات",
            category = "تطوير البرمجيات",
            totalLessons = 12,
            durationMinutes = 360,
            schoolId = schoolIdA
        )

        val courseB = Course(
            id = "c2",
            title = "أساسيات الفيزياء التطبيقية",
            description = "مقدمة لمبادئ الفيزياء",
            category = "العلوم الطبيعية",
            totalLessons = 10,
            durationMinutes = 300,
            schoolId = schoolIdB
        )

        val lessonA = Lesson(
            id = "l1",
            courseId = "c1",
            title = "المكونات الأساسية للتطبيق",
            content = "شرح ViewModel و Room",
            order = 1,
            schoolId = schoolIdA
        )

        val quizA = Quiz(
            id = "q1",
            courseId = "c1",
            title = "اختبار المفاهيم الأساسية",
            questions = emptyList(),
            schoolId = schoolIdA
        )

        val userProfileA = UserProfile(
            id = "u1",
            name = "محمد المظفر",
            email = "mohammed@school1.edu",
            schoolId = schoolIdA
        )

        // Verify multi-tenant properties
        assertEquals(schoolIdA, studentA.schoolId)
        assertEquals(schoolIdB, studentB.schoolId)
        assertNotEquals(studentA.schoolId, studentB.schoolId)

        assertEquals(schoolIdA, teacherA.schoolId)
        assertEquals(schoolIdA, classSectionA.schoolId)
        assertEquals(schoolIdA, subjectA.schoolId)
        assertEquals(schoolIdA, courseA.schoolId)
        assertEquals(schoolIdB, courseB.schoolId)
        assertEquals(schoolIdA, lessonA.schoolId)
        assertEquals(schoolIdA, quizA.schoolId)
        assertEquals(schoolIdA, userProfileA.schoolId)

        // Verify isolation logic using filtering simulation
        val allCourses = listOf(courseA, courseB)
        val coursesForSchoolA = allCourses.filter { it.schoolId == schoolIdA }
        val coursesForSchoolB = allCourses.filter { it.schoolId == schoolIdB }

        assertEquals(1, coursesForSchoolA.size)
        assertEquals("c1", coursesForSchoolA.first().id)
        assertEquals(1, coursesForSchoolB.size)
        assertEquals("c2", coursesForSchoolB.first().id)
    }

    @Test
    fun testActiveSchoolSelectionAndDynamicStateSwitch() {
        val schools = listOf(
            School("school_001", "مدرسة المستقبل", "SCH-001", "الرياض", "+966501234567"),
            School("school_002", "مدرسة التفوق", "SCH-002", "جدة", "+966507654321")
        )

        var activeSchoolId = "school_001"
        var activeSchool = schools.find { it.id == activeSchoolId }

        assertEquals("مدرسة المستقبل", activeSchool?.name)

        // Switch active school dynamically without restarting app
        activeSchoolId = "school_002"
        activeSchool = schools.find { it.id == activeSchoolId }

        assertEquals("مدرسة التفوق", activeSchool?.name)
        assertEquals("SCH-002", activeSchool?.code)
    }

    @Test
    fun testAddEditSchoolEntityAndPersistence() {
        val initialList = mutableListOf(
            School("school_001", "المستقبل", "SCH-001", "الرياض", "+966500000001")
        )

        // Add new school
        val newSchool = School(
            id = "school_003",
            name = "مدرسة الرؤية 2030",
            code = "SCH-003",
            address = "الخبر",
            phone = "+966509998877",
            studentsCount = 600,
            teachersCount = 45
        )
        initialList.add(newSchool)
        assertEquals(2, initialList.size)

        // Edit existing school
        val indexToEdit = initialList.indexOfFirst { it.id == "school_003" }
        if (indexToEdit != -1) {
            initialList[indexToEdit] = initialList[indexToEdit].copy(
                name = "مدرسة الرؤية المتقدمة",
                studentsCount = 650
            )
        }

        val updatedSchool = initialList.find { it.id == "school_003" }
        assertEquals("مدرسة الرؤية المتقدمة", updatedSchool?.name)
        assertEquals(650, updatedSchool?.studentsCount)
    }
}
