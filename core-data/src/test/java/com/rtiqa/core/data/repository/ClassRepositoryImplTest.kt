package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.SchoolClassDao
import com.rtiqa.core.database.entity.SchoolClassEntity
import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.domain.usecase.SaveClassUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClassRepositoryImplTest {

    private class FakeSchoolClassDao : SchoolClassDao {
        val list = mutableListOf(
            SchoolClassEntity("c1", "school_001", "الصف الأول الابتدائي", "الابتدائي", "أ", 30, 20, "101", 1),
            SchoolClassEntity("c2", "school_001", "الصف الثاني الابتدائي", "الابتدائي", "ب", 30, 22, "102", 2),
            SchoolClassEntity("c3", "school_002", "الصف الأول الابتدائي", "الابتدائي", "أ", 25, 18, "201", 1)
        )

        override fun getClassesForSchool(schoolId: String): Flow<List<SchoolClassEntity>> =
            flowOf(list.filter { it.schoolId == schoolId })

        override suspend fun getClassById(id: String): SchoolClassEntity? =
            list.find { it.id == id }

        override suspend fun findClassByNameInSchool(schoolId: String, name: String, excludeId: String): SchoolClassEntity? =
            list.find {
                it.schoolId == schoolId &&
                        it.name.trim().equals(name.trim(), ignoreCase = true) &&
                        it.id != excludeId
            }

        override suspend fun insertClass(schoolClass: SchoolClassEntity) {
            val idx = list.indexOfFirst { it.id == schoolClass.id }
            if (idx != -1) list[idx] = schoolClass else list.add(schoolClass)
        }

        override suspend fun insertClasses(classes: List<SchoolClassEntity>) {
            classes.forEach { insertClass(it) }
        }

        override suspend fun deleteClassById(id: String) {
            list.removeAll { it.id == id }
        }
    }

    private lateinit var fakeDao: FakeSchoolClassDao
    private lateinit var repository: ClassRepositoryImpl
    private lateinit var saveClassUseCase: SaveClassUseCase

    @Before
    fun setUp() {
        fakeDao = FakeSchoolClassDao()
        repository = ClassRepositoryImpl(fakeDao)
        saveClassUseCase = SaveClassUseCase(repository)
    }

    @Test
    fun getClassesForSchool_isolatesDataBySchoolId() = runTest {
        val school1Classes = repository.getClassesForSchool("school_001").first()
        val school2Classes = repository.getClassesForSchool("school_002").first()

        assertEquals(2, school1Classes.size)
        assertTrue(school1Classes.all { it.schoolId == "school_001" })

        assertEquals(1, school2Classes.size)
        assertTrue(school2Classes.all { it.schoolId == "school_002" })
    }

    @Test
    fun crudOperations_workCorrectly() = runTest {
        // Create
        val newClass = SchoolClass(
            id = "c4",
            schoolId = "school_001",
            name = "الصف الثالث الابتدائي",
            gradeLevel = "الابتدائي",
            sectionName = "ج",
            capacity = 30,
            displayOrder = 3
        )
        repository.saveClass(newClass)

        // Read
        val fetched = repository.getClassById("c4")
        assertNotNull(fetched)
        assertEquals("الصف الثالث الابتدائي", fetched?.name)

        // Update
        val updated = fetched!!.copy(capacity = 35)
        repository.saveClass(updated)
        val fetchedUpdated = repository.getClassById("c4")
        assertEquals(35, fetchedUpdated?.capacity)

        // Delete
        repository.deleteClass("c4")
        val fetchedDeleted = repository.getClassById("c4")
        assertNull(fetchedDeleted)
    }

    @Test
    fun validateDuplicateName_preventsDuplicateInSameSchool_allowsInDifferentSchool() = runTest {
        // Same school_001: "الصف الأول الابتدائي" exists
        val isUniqueInSchool1 = repository.isClassNameUniqueInSchool("school_001", "الصف الأول الابتدائي")
        assertFalse("Should not allow duplicate class name in same school", isUniqueInSchool1)

        // Different school_003: "الصف الأول الابتدائي" does not exist yet
        val isUniqueInSchool3 = repository.isClassNameUniqueInSchool("school_003", "الصف الأول الابتدائي")
        assertTrue("Should allow same class name in different school", isUniqueInSchool3)
    }

    @Test
    fun saveClassUseCase_failsOnDuplicateNameInSameSchool() = runTest {
        val duplicateClass = SchoolClass(
            id = "c5",
            schoolId = "school_001",
            name = "الصف الأول الابتدائي", // Already exists in school_001
            gradeLevel = "الابتدائي"
        )

        val result = saveClassUseCase(duplicateClass)
        assertTrue(result.isFailure)
        assertEquals("اسم الصف مستخدم بالفعل في هذه المدرسة", result.exceptionOrNull()?.message)
    }

    @Test
    fun activeSchoolDataSwitch_retrievesNewSchoolClassesImmediately() = runTest {
        var activeSchoolId = "school_001"
        var classes = repository.getClassesForSchool(activeSchoolId).first()
        assertEquals(2, classes.size)

        // Switch to school_002
        activeSchoolId = "school_002"
        classes = repository.getClassesForSchool(activeSchoolId).first()
        assertEquals(1, classes.size)
        assertEquals("school_002", classes[0].schoolId)
    }
}
