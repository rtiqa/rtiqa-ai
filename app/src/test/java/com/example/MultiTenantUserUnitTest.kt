package com.example

import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.data.mapper.toEntity
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.MemberStatus
import com.rtiqa.core.domain.model.School
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiTenantUserUnitTest {

    @Test
    fun testAllFiveUserRolesExistAndHaveArabicLabels() {
        val roles = listOf(
            EnterpriseRole.PRINCIPAL,
            EnterpriseRole.VICE_PRINCIPAL,
            EnterpriseRole.TEACHER,
            EnterpriseRole.STUDENT,
            EnterpriseRole.PARENT
        )

        assertEquals("مدير المدرسة", EnterpriseRole.PRINCIPAL.labelAr)
        assertEquals("وكيل المدرسة", EnterpriseRole.VICE_PRINCIPAL.labelAr)
        assertEquals("معلم", EnterpriseRole.TEACHER.labelAr)
        assertEquals("طالب", EnterpriseRole.STUDENT.labelAr)
        assertEquals("ولي أمر", EnterpriseRole.PARENT.labelAr)
        assertEquals(5, roles.size)
    }

    @Test
    fun testUsersLinkedOnlyToActiveSchoolId() {
        val activeSchool1Id = "school_001"
        val activeSchool2Id = "school_002"

        val allUsers = listOf(
            EnterpriseMember(
                id = "u1",
                orgId = "org_1",
                name = "د. محمد",
                email = "principal@school1.edu",
                role = EnterpriseRole.PRINCIPAL,
                department = "الإدارة",
                schoolId = activeSchool1Id
            ),
            EnterpriseMember(
                id = "u2",
                orgId = "org_1",
                name = "أ. سارة",
                email = "teacher@school1.edu",
                role = EnterpriseRole.TEACHER,
                department = "العلوم",
                schoolId = activeSchool1Id
            ),
            EnterpriseMember(
                id = "u3",
                orgId = "org_1",
                name = "د. نورة",
                email = "principal@school2.edu",
                role = EnterpriseRole.PRINCIPAL,
                department = "الإدارة العليا",
                schoolId = activeSchool2Id
            ),
            EnterpriseMember(
                id = "u4",
                orgId = "org_1",
                name = "خالد (ولي أمر)",
                email = "parent@school2.edu",
                role = EnterpriseRole.PARENT,
                department = "أولياء الأمور",
                schoolId = activeSchool2Id
            )
        )

        val school1Users = allUsers.filter { it.schoolId == activeSchool1Id }
        val school2Users = allUsers.filter { it.schoolId == activeSchool2Id }

        assertEquals(2, school1Users.size)
        assertEquals(2, school2Users.size)

        assertTrue(school1Users.all { it.schoolId == activeSchool1Id })
        assertTrue(school2Users.all { it.schoolId == activeSchool2Id })
    }

    @Test
    fun testUserSearchAndRoleFiltering() {
        val users = listOf(
            EnterpriseMember(
                id = "1",
                orgId = "org1",
                name = "عبد الله الشهري",
                email = "abdullah@school.edu",
                role = EnterpriseRole.TEACHER,
                department = "الرياضيات",
                phone = "+966500000001",
                schoolId = "school_001"
            ),
            EnterpriseMember(
                id = "2",
                orgId = "org1",
                name = "علي المظفر",
                email = "ali@school.edu",
                role = EnterpriseRole.STUDENT,
                department = "الحاسب الآلي",
                phone = "+966500000002",
                schoolId = "school_001"
            ),
            EnterpriseMember(
                id = "3",
                orgId = "org1",
                name = "فهد المظفر (ولي أمر)",
                email = "fahad@school.edu",
                role = EnterpriseRole.PARENT,
                department = "أولياء الأمور",
                phone = "+966500000003",
                schoolId = "school_001"
            )
        )

        // Filter by role TEACHER
        val teachers = users.filter { it.role == EnterpriseRole.TEACHER }
        assertEquals(1, teachers.size)
        assertEquals("عبد الله الشهري", teachers.first().name)

        // Filter by role PARENT
        val parents = users.filter { it.role == EnterpriseRole.PARENT }
        assertEquals(1, parents.size)
        assertEquals("فهد المظفر (ولي أمر)", parents.first().name)

        // Search query "المظفر"
        val searchQueryResults = users.filter { it.name.contains("المظفر") }
        assertEquals(2, searchQueryResults.size)
    }

    @Test
    fun testUserCrudOperationsLogic() {
        val userList = mutableListOf<EnterpriseMember>()

        // 1. Add User
        val newUser = EnterpriseMember(
            id = "usr_001",
            orgId = "org_1",
            name = "م. أحمد الغامدي",
            email = "ahmed@school.edu",
            role = EnterpriseRole.VICE_PRINCIPAL,
            department = "الشؤون التعليمية",
            status = MemberStatus.ACTIVE,
            phone = "+966551122334",
            schoolId = "school_001"
        )
        userList.add(newUser)

        assertEquals(1, userList.size)
        assertEquals("usr_001", userList.first().id)
        assertEquals(EnterpriseRole.VICE_PRINCIPAL, userList.first().role)

        // 2. Edit User
        val index = userList.indexOfFirst { it.id == "usr_001" }
        val updatedUser = userList[index].copy(
            name = "د. أحمد الغامدي",
            role = EnterpriseRole.PRINCIPAL,
            department = "الإدارة العامة"
        )
        userList[index] = updatedUser

        assertEquals("د. أحمد الغامدي", userList.first().name)
        assertEquals(EnterpriseRole.PRINCIPAL, userList.first().role)

        // 3. Delete User
        userList.removeIf { it.id == "usr_001" }
        assertTrue(userList.isEmpty())
    }

    @Test
    fun testActiveSchoolSwitchChangesUserListDynamically() {
        var activeSchoolId = "school_001"

        val databaseMembers = listOf(
            EnterpriseMember("u1", "org1", "مدير المستقبل", "p1@sch.edu", EnterpriseRole.PRINCIPAL, "إدارة", schoolId = "school_001"),
            EnterpriseMember("u2", "org1", "طالب المستقبل", "s1@sch.edu", EnterpriseRole.STUDENT, "الصف 1", schoolId = "school_001"),
            EnterpriseMember("u3", "org1", "مدير التفوق", "p2@sch.edu", EnterpriseRole.PRINCIPAL, "إدارة", schoolId = "school_002"),
            EnterpriseMember("u4", "org1", "معلم التفوق", "t2@sch.edu", EnterpriseRole.TEACHER, "فيزياء", schoolId = "school_002")
        )

        // Active school is school_001
        var currentActiveUsers = databaseMembers.filter { it.schoolId == activeSchoolId }
        assertEquals(2, currentActiveUsers.size)
        assertTrue(currentActiveUsers.any { it.name == "مدير المستقبل" })
        assertFalse(currentActiveUsers.any { it.name == "مدير التفوق" })

        // Switch active school to school_002
        activeSchoolId = "school_002"
        currentActiveUsers = databaseMembers.filter { it.schoolId == activeSchoolId }
        assertEquals(2, currentActiveUsers.size)
        assertTrue(currentActiveUsers.any { it.name == "مدير التفوق" })
        assertFalse(currentActiveUsers.any { it.name == "مدير المستقبل" })
    }

    @Test
    fun testEnterpriseMemberEntityMapperToDomainAndEntity() {
        val member = EnterpriseMember(
            id = "usr_mapper_1",
            orgId = "org_1",
            name = "استاذ هشام",
            email = "hisham@school.edu",
            role = EnterpriseRole.TEACHER,
            department = "اللغة العربية",
            status = MemberStatus.ACTIVE,
            phone = "+966512345678",
            schoolId = "school_001"
        )

        val entity = member.toEntity()
        assertEquals("usr_mapper_1", entity.id)
        assertEquals("school_001", entity.schoolId)
        assertEquals("TEACHER", entity.role)

        val domain = entity.toDomain()
        assertEquals(member.id, domain.id)
        assertEquals(member.schoolId, domain.schoolId)
        assertEquals(member.role, domain.role)
    }
}
