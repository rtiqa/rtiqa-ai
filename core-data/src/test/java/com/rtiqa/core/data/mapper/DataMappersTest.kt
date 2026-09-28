package com.rtiqa.core.data.mapper

import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.network.api.NetworkCourseDto
import com.rtiqa.core.network.api.NetworkLessonDto
import com.rtiqa.core.network.model.CourseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DataMappersTest {

    @Test
    fun courseEntity_toDomain_preservesNullAndNonNullSchoolId() {
        val entityNullSchool = CourseEntity(
            id = "c1",
            title = "Kotlin",
            description = "Desc",
            category = "Mobile",
            totalLessons = 5,
            durationMinutes = 100,
            iconUrl = null,
            isDownloaded = false,
            progressPercent = 0.5f,
            schoolId = null
        )
        val domainNull = entityNullSchool.toDomain()
        assertNull(domainNull.schoolId)
        assertNotEquals("school_001", domainNull.schoolId)

        val entityWithSchool = entityNullSchool.copy(schoolId = "school_custom_123")
        val domainWithSchool = entityWithSchool.toDomain()
        assertEquals("school_custom_123", domainWithSchool.schoolId)
    }

    @Test
    fun course_toEntity_preservesNullAndNonNullSchoolId() {
        val courseNull = Course(
            id = "c1",
            title = "Kotlin",
            description = "Desc",
            category = "Mobile",
            totalLessons = 5,
            durationMinutes = 100,
            schoolId = null
        )
        val entityNull = courseNull.toEntity()
        assertNull(entityNull.schoolId)
        assertNotEquals("school_001", entityNull.schoolId)

        val courseWithSchool = courseNull.copy(schoolId = "school_custom_123")
        val entityWithSchool = courseWithSchool.toEntity()
        assertEquals("school_custom_123", entityWithSchool.schoolId)
    }

    @Test
    fun courseDto_toEntity_defaultsToNullSchoolId_andPreservesExistingSchoolId() {
        val dto = CourseDto(
            id = "c_dto",
            title = "Course Dto",
            description = "Desc",
            category = "General",
            totalLessons = 3,
            durationMinutes = 60
        )
        val entityDefault = dto.toEntity()
        assertNull(entityDefault.schoolId)
        assertNotEquals("school_001", entityDefault.schoolId)

        val entityWithExisting = dto.toEntity(existingSchoolId = "school_existing_88")
        assertEquals("school_existing_88", entityWithExisting.schoolId)
    }

    @Test
    fun networkCourseDto_toEntity_defaultsToNullSchoolId_andPreservesExistingSchoolId() {
        val dto = NetworkCourseDto(
            id = "nc_dto",
            title = "Net Course",
            description = "Desc",
            category = "AI",
            difficulty = "BEGINNER",
            totalModules = 4,
            completedModules = 1,
            progressPercent = 0.25f
        )
        val entityDefault = dto.toEntity()
        assertNull(entityDefault.schoolId)
        assertNotEquals("school_001", entityDefault.schoolId)

        val entityWithExisting = dto.toEntity(existingSchoolId = "school_preserved_55")
        assertEquals("school_preserved_55", entityWithExisting.schoolId)
    }

    @Test
    fun networkLessonDto_toEntity_and_toDomain_defaultToNullSchoolId_andPropagateAudioUrl() {
        val dto = NetworkLessonDto(
            id = "nl_1",
            courseId = "nc_1",
            title = "Lesson Net",
            content = "Content",
            moduleOrder = 1,
            audioUrl = "https://example.com/audio/net.mp3"
        )

        val entityDefault = dto.toEntity()
        assertNull(entityDefault.schoolId)
        assertNotEquals("school_001", entityDefault.schoolId)
        assertEquals("https://example.com/audio/net.mp3", entityDefault.audioUrl)

        val entityPreserved = dto.toEntity(existingSchoolId = "school_branch_09")
        assertEquals("school_branch_09", entityPreserved.schoolId)
        assertEquals("https://example.com/audio/net.mp3", entityPreserved.audioUrl)

        val domainDefault = dto.toDomain()
        assertNull(domainDefault.schoolId)
        assertNotEquals("school_001", domainDefault.schoolId)
        assertEquals("https://example.com/audio/net.mp3", domainDefault.audioUrl)

        val domainWithSchool = dto.toDomain(schoolId = "school_branch_09")
        assertEquals("school_branch_09", domainWithSchool.schoolId)
    }

    @Test
    fun lessonEntity_and_Lesson_preserveNullAndNonNullSchoolId() {
        val entity = LessonEntity(
            id = "l1",
            courseId = "c1",
            title = "Title",
            content = "Content",
            order = 1,
            isCompleted = false,
            audioUrl = "https://example.com/audio/lesson.mp3",
            schoolId = null
        )
        val domain = entity.toDomain()
        assertNull(domain.schoolId)
        assertNotEquals("school_001", domain.schoolId)
        assertEquals("https://example.com/audio/lesson.mp3", domain.audioUrl)

        val entityWithSchool = entity.copy(schoolId = "school_specific_77")
        val domainWithSchool = entityWithSchool.toDomain()
        assertEquals("school_specific_77", domainWithSchool.schoolId)

        val backToEntity = domainWithSchool.toEntity()
        assertEquals("school_specific_77", backToEntity.schoolId)
        assertEquals("https://example.com/audio/lesson.mp3", backToEntity.audioUrl)
    }
}
