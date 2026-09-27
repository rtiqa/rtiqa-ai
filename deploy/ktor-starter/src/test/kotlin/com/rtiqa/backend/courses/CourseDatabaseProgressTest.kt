package com.rtiqa.backend.courses

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
import java.util.UUID

class CourseDatabaseProgressTest {

    private lateinit var connection: Connection
    private val repository = CourseRepository()

    private val tenantA = UUID.randomUUID()
    private val tenantB = UUID.randomUUID()
    private val userA = UUID.randomUUID()
    private val userB = UUID.randomUUID()
    private val courseId = UUID.randomUUID()
    private val lesson1Id = UUID.randomUUID()
    private val lesson2Id = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb_${UUID.randomUUID()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
        
        connection.createStatement().use { stmt ->
            val ddlStatements = listOf(
                """
                CREATE TABLE organizations (
                    id UUID PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    domain VARCHAR(255) NOT NULL,
                    status VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """.trimIndent(),
                """
                CREATE TABLE profiles (
                    id UUID PRIMARY KEY,
                    email VARCHAR(255) NOT NULL,
                    full_name VARCHAR(255),
                    status VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """.trimIndent(),
                """
                CREATE TABLE organization_members (
                    id UUID PRIMARY KEY,
                    organization_id UUID NOT NULL REFERENCES organizations(id),
                    user_id UUID NOT NULL REFERENCES profiles(id),
                    role VARCHAR(50) NOT NULL,
                    status VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """.trimIndent(),
                """
                CREATE TABLE courses (
                    id UUID PRIMARY KEY,
                    organization_id UUID NOT NULL REFERENCES organizations(id),
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    category VARCHAR(100),
                    level VARCHAR(50) DEFAULT 'مبتدئ',
                    difficulty VARCHAR(50),
                    total_modules INT DEFAULT 0,
                    duration_minutes INT DEFAULT 0,
                    icon_url TEXT,
                    status VARCHAR(50) DEFAULT 'published',
                    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW()
                )
                """.trimIndent(),
                """
                CREATE TABLE lessons (
                    id UUID PRIMARY KEY,
                    course_id UUID NOT NULL REFERENCES courses(id),
                    title VARCHAR(255) NOT NULL,
                    content TEXT,
                    module_order INT NOT NULL,
                    estimated_minutes INT DEFAULT 10,
                    audio_url TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW()
                )
                """.trimIndent(),
                """
                CREATE TABLE enrollments (
                    id UUID PRIMARY KEY,
                    user_id UUID NOT NULL REFERENCES profiles(id),
                    course_id UUID NOT NULL REFERENCES courses(id),
                    status VARCHAR(50) NOT NULL,
                    progress_percent REAL DEFAULT 0.0,
                    completed_lessons INT DEFAULT 0,
                    enrolled_at TIMESTAMP NOT NULL,
                    last_accessed_at TIMESTAMP,
                    completed_at TIMESTAMP
                )
                """.trimIndent(),
                """
                CREATE TABLE progress_records (
                    id UUID PRIMARY KEY,
                    user_id UUID NOT NULL REFERENCES profiles(id),
                    lesson_id UUID NOT NULL REFERENCES lessons(id),
                    completed BOOLEAN DEFAULT FALSE,
                    score INT DEFAULT 0,
                    updated_at TIMESTAMP NOT NULL,
                    CONSTRAINT unique_user_lesson_progress UNIQUE (user_id, lesson_id)
                )
                """.trimIndent()
            )

            for (ddl in ddlStatements) {
                stmt.execute(ddl)
            }

            // Insert initial test fixture
            stmt.execute("INSERT INTO organizations (id, name, domain, status, created_at, updated_at) VALUES ('$tenantA', 'Org A', 'orga.com', 'active', NOW(), NOW())")
            stmt.execute("INSERT INTO organizations (id, name, domain, status, created_at, updated_at) VALUES ('$tenantB', 'Org B', 'orgb.com', 'active', NOW(), NOW())")

            stmt.execute("INSERT INTO profiles (id, email, full_name, status, created_at, updated_at) VALUES ('$userA', 'usera@test.com', 'User A', 'active', NOW(), NOW())")
            stmt.execute("INSERT INTO profiles (id, email, full_name, status, created_at, updated_at) VALUES ('$userB', 'userb@test.com', 'User B', 'active', NOW(), NOW())")

            stmt.execute("INSERT INTO organization_members (id, organization_id, user_id, role, status, created_at, updated_at) VALUES ('${UUID.randomUUID()}', '$tenantA', '$userA', 'student', 'active', NOW(), NOW())")
            stmt.execute("INSERT INTO organization_members (id, organization_id, user_id, role, status, created_at, updated_at) VALUES ('${UUID.randomUUID()}', '$tenantA', '$userB', 'student', 'active', NOW(), NOW())")

            stmt.execute("INSERT INTO courses (id, organization_id, title, description, category, difficulty, total_modules, status, created_at, updated_at) VALUES ('$courseId', '$tenantA', 'AI Track', 'Intro course', 'AI', 'Beginner', 2, 'published', NOW(), NOW())")

            stmt.execute("INSERT INTO lessons (id, course_id, title, content, module_order, estimated_minutes, created_at, updated_at) VALUES ('$lesson1Id', '$courseId', 'Lesson 1', 'Content 1', 1, 15, NOW(), NOW())")
            stmt.execute("INSERT INTO lessons (id, course_id, title, content, module_order, estimated_minutes, created_at, updated_at) VALUES ('$lesson2Id', '$courseId', 'Lesson 2', 'Content 2', 2, 20, NOW(), NOW())")

            // Enroll userA and userB in course
            stmt.execute("INSERT INTO enrollments (id, user_id, course_id, status, progress_percent, completed_lessons, enrolled_at) VALUES ('${UUID.randomUUID()}', '$userA', '$courseId', 'active', 0.0, 0, NOW())")
            stmt.execute("INSERT INTO enrollments (id, user_id, course_id, status, progress_percent, completed_lessons, enrolled_at) VALUES ('${UUID.randomUUID()}', '$userB', '$courseId', 'active', 0.0, 0, NOW())")
        }
    }

    @AfterEach
    fun tearDown() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    @Test
    fun `completeLesson saves progress in progress_records and updates enrollment`() {
        val result = repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 95
        )

        assertTrue(result is CompleteLessonResult.Success)
        val completion = (result as CompleteLessonResult.Success).completion
        assertEquals(true, completion.success)
        assertEquals(true, completion.completed)
        assertEquals(1, completion.completedLessons)
        assertEquals(2, completion.totalLessons)
        assertEquals(50.0f, completion.courseProgressPercent)

        // Verify direct database state in progress_records
        connection.prepareStatement("SELECT completed, score FROM progress_records WHERE user_id = ? AND lesson_id = ?").use { stmt ->
            stmt.setObject(1, userA)
            stmt.setObject(2, lesson1Id)
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next(), "Progress record must exist in database")
                assertTrue(rs.getBoolean("completed"))
                assertEquals(95, rs.getInt("score"))
            }
        }

        // Verify direct database state in enrollments
        connection.prepareStatement("SELECT completed_lessons, progress_percent, status FROM enrollments WHERE user_id = ? AND course_id = ?").use { stmt ->
            stmt.setObject(1, userA)
            stmt.setObject(2, courseId)
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next(), "Enrollment record must exist")
                assertEquals(1, rs.getInt("completed_lessons"))
                assertEquals(50.0f, rs.getFloat("progress_percent"))
                assertEquals("active", rs.getString("status"))
            }
        }
    }

    @Test
    fun `completeLesson is idempotent and does not create duplicate records on repeated submissions`() {
        // First submission
        repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 80
        )

        // Second submission of the same lesson
        val secondResult = repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 90
        )

        assertTrue(secondResult is CompleteLessonResult.Success)

        // Verify exactly ONE record exists in progress_records
        connection.prepareStatement("SELECT COUNT(*) FROM progress_records WHERE user_id = ? AND lesson_id = ?").use { stmt ->
            stmt.setObject(1, userA)
            stmt.setObject(2, lesson1Id)
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next())
                assertEquals(1, rs.getInt(1), "Count of progress records must be exactly 1, no duplicates")
            }
        }

        // Verify score was updated to greatest score (90)
        connection.prepareStatement("SELECT score FROM progress_records WHERE user_id = ? AND lesson_id = ?").use { stmt ->
            stmt.setObject(1, userA)
            stmt.setObject(2, lesson1Id)
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next())
                assertEquals(90, rs.getInt("score"))
            }
        }
    }

    @Test
    fun `user from another tenant cannot complete lesson or access course`() {
        val result = repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantB, // Wrong tenant!
            userId = userA,
            score = 100
        )

        assertTrue(result is CompleteLessonResult.CourseNotFound)

        // Ensure nothing was saved in progress_records
        connection.prepareStatement("SELECT COUNT(*) FROM progress_records WHERE user_id = ? AND lesson_id = ?").use { stmt ->
            stmt.setObject(1, userA)
            stmt.setObject(2, lesson1Id)
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next())
                assertEquals(0, rs.getInt(1))
            }
        }
    }

    @Test
    fun `user cannot modify progress of another user`() {
        // User A completes lesson 1
        repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 100
        )

        // Check user B's lessons: user B's progress must still be 0
        val userBLessons = repository.getLessonsForCourse(
            conn = connection,
            courseId = courseId,
            tenantId = tenantA,
            userId = userB
        )

        assertEquals(2, userBLessons.size)
        assertFalse(userBLessons[0].isCompleted, "User B must not have lesson 1 completed")
        assertFalse(userBLessons[1].isCompleted, "User B must not have lesson 2 completed")

        // User A's lessons: user A has lesson 1 completed
        val userALessons = repository.getLessonsForCourse(
            conn = connection,
            courseId = courseId,
            tenantId = tenantA,
            userId = userA
        )
        assertTrue(userALessons[0].isCompleted, "User A must have lesson 1 completed")
        assertFalse(userALessons[1].isCompleted, "User A must not have lesson 2 completed")
    }

    @Test
    fun `non-existent lesson returns LessonNotFound`() {
        val nonExistentLessonId = UUID.randomUUID()
        val result = repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = nonExistentLessonId,
            tenantId = tenantA,
            userId = userA
        )

        assertTrue(result is CompleteLessonResult.LessonNotFound)
    }

    @Test
    fun `non-enrolled user returns NotEnrolled`() {
        val unenrolledUser = UUID.randomUUID()
        connection.createStatement().use { stmt ->
            stmt.execute("INSERT INTO profiles (id, email, full_name, status, created_at, updated_at) VALUES ('$unenrolledUser', 'other@test.com', 'Other User', 'active', NOW(), NOW())")
        }

        val result = repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = unenrolledUser
        )

        assertTrue(result is CompleteLessonResult.NotEnrolled)
    }

    @Test
    fun `completing all lessons marks enrollment as completed`() {
        repository.completeLesson(connection, courseId, lesson1Id, tenantA, userA, 100)
        val finalResult = repository.completeLesson(connection, courseId, lesson2Id, tenantA, userA, 100)

        assertTrue(finalResult is CompleteLessonResult.Success)
        val completion = (finalResult as CompleteLessonResult.Success).completion
        assertEquals(2, completion.completedLessons)
        assertEquals(2, completion.totalLessons)
        assertEquals(100.0f, completion.courseProgressPercent)

        // Check status in enrollments
        connection.prepareStatement("SELECT status, completed_at FROM enrollments WHERE user_id = ? AND course_id = ?").use { stmt ->
            stmt.setObject(1, userA)
            stmt.setObject(2, courseId)
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next())
                assertEquals("completed", rs.getString("status"))
            }
        }
    }

    @Test
    fun `createCourse persists course in database for organization`() {
        val request = CreateCourseRequestDto(
            title = "دورة تطوير تطبيقات أندرويد",
            description = "تعلم لغة كوتلن مع جيت باك كومبوز",
            category = "تطوير البرمجيات",
            difficulty = "متوسط",
            durationMinutes = 120
        )
        val created = repository.createCourse(connection, tenantA, request)

        assertEquals("دورة تطوير تطبيقات أندرويد", created.title)
        assertEquals("تعلم لغة كوتلن مع جيت باك كومبوز", created.description)
        assertEquals("تطوير البرمجيات", created.category)
        assertEquals("متوسط", created.difficulty)
        assertEquals(0, created.totalModules)
        assertEquals(0, created.completedModules)
        assertEquals(0.0f, created.progressPercent)

        // Verify direct database row
        connection.prepareStatement("SELECT title, description, category, level, duration_minutes FROM courses WHERE id = ?").use { stmt ->
            stmt.setObject(1, UUID.fromString(created.id))
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next(), "Created course must exist in database")
                assertEquals("دورة تطوير تطبيقات أندرويد", rs.getString("title"))
                assertEquals("تعلم لغة كوتلن مع جيت باك كومبوز", rs.getString("description"))
                assertEquals("تطوير البرمجيات", rs.getString("category"))
                assertEquals("متوسط", rs.getString("level"))
                assertEquals(120, rs.getInt("duration_minutes"))
            }
        }
    }

    @Test
    fun `createLesson persists lesson in database and links to correct course`() {
        val request = CreateLessonRequestDto(
            title = "المتغيرات وأنواع البيانات",
            content = "شرح تفصيلي للمتغيرات في كوتلن",
            moduleOrder = 3,
            estimatedMinutes = 25
        )
        val result = repository.createLesson(connection, courseId, tenantA, request)

        assertTrue(result is CreateLessonResult.Success)
        val createdLesson = (result as CreateLessonResult.Success).lesson
        assertEquals("المتغيرات وأنواع البيانات", createdLesson.title)
        assertEquals("شرح تفصيلي للمتغيرات في كوتلن", createdLesson.content)
        assertEquals(3, createdLesson.moduleOrder)
        assertEquals(25, createdLesson.estimatedMinutes)
        assertEquals(courseId.toString(), createdLesson.courseId)
        assertFalse(createdLesson.isCompleted)

        // Verify direct database row
        connection.prepareStatement("SELECT id, course_id, title, content, module_order, estimated_minutes FROM lessons WHERE id = ?").use { stmt ->
            stmt.setObject(1, UUID.fromString(createdLesson.id))
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next(), "Lesson must be found in database")
                assertEquals(courseId.toString(), rs.getString("course_id"))
                assertEquals("المتغيرات وأنواع البيانات", rs.getString("title"))
                assertEquals("شرح تفصيلي للمتغيرات في كوتلن", rs.getString("content"))
                assertEquals(3, rs.getInt("module_order"))
                assertEquals(25, rs.getInt("estimated_minutes"))
            }
        }
    }

    @Test
    fun `createLesson returns CourseNotFound when course does not exist`() {
        val nonExistentCourse = UUID.randomUUID()
        val request = CreateLessonRequestDto(title = "درس تجريبي")
        val result = repository.createLesson(connection, nonExistentCourse, tenantA, request)

        assertTrue(result is CreateLessonResult.CourseNotFound)
    }

    @Test
    fun `createLesson calculates next module order when not provided`() {
        val request = CreateLessonRequestDto(title = "الدرس التالي التلقائي")
        val result = repository.createLesson(connection, courseId, tenantA, request)

        assertTrue(result is CreateLessonResult.Success)
        val createdLesson = (result as CreateLessonResult.Success).lesson
        // courseId already has lesson 1 (order 1) and lesson 2 (order 2)
        assertEquals(3, createdLesson.moduleOrder)
    }

    @Test
    fun `student progress update persists score changes and updates enrollment progress`() {
        // Initial completion with score 70
        val initialResult = repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 70
        )
        assertTrue(initialResult is CompleteLessonResult.Success)

        // Repeat with higher score 95
        val updatedResult = repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 95
        )
        assertTrue(updatedResult is CompleteLessonResult.Success)

        // Verify score updated in database
        connection.prepareStatement("SELECT score, completed FROM progress_records WHERE user_id = ? AND lesson_id = ?").use { stmt ->
            stmt.setObject(1, userA)
            stmt.setObject(2, lesson1Id)
            stmt.executeQuery().use { rs ->
                assertTrue(rs.next())
                assertEquals(95, rs.getInt("score"))
                assertTrue(rs.getBoolean("completed"))
            }
        }

        // Verify reflected in getLessonsForCourse
        val lessons = repository.getLessonsForCourse(
            conn = connection,
            courseId = courseId,
            tenantId = tenantA,
            userId = userA
        )
        val completedLesson = lessons.first { it.id == lesson1Id.toString() }
        assertTrue(completedLesson.isCompleted)
    }

    @Test
    fun `cannot complete lesson from another course`() {
        val otherCourseId = UUID.randomUUID()
        val result = repository.completeLesson(
            conn = connection,
            courseId = otherCourseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 100
        )
        assertTrue(result is CompleteLessonResult.CourseNotFound)
    }

    @Test
    fun `getCourseById returns existing course with lessons count and student progress`() {
        // Complete lesson 1 for userA
        repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 100
        )

        val course = repository.getCourseById(
            conn = connection,
            courseId = courseId,
            tenantId = tenantA,
            userId = userA
        )

        assertNotNull(course)
        assertEquals(courseId.toString(), course!!.id)
        assertEquals("AI Track", course.title)
        assertEquals("Intro course", course.description)
        assertEquals("AI", course.category)
        assertEquals(2, course.totalModules)
        assertEquals(1, course.completedModules)
        assertEquals(50.0f, course.progressPercent)
    }

    @Test
    fun `getCourseById progress isolates querying student from other students`() {
        // Complete lesson 1 for userA only
        repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 100
        )

        val courseForUserB = repository.getCourseById(
            conn = connection,
            courseId = courseId,
            tenantId = tenantA,
            userId = userB
        )

        assertNotNull(courseForUserB)
        assertEquals(courseId.toString(), courseForUserB!!.id)
        assertEquals(2, courseForUserB.totalModules)
        assertEquals(0, courseForUserB.completedModules)
        assertEquals(0.0f, courseForUserB.progressPercent)
    }

    @Test
    fun `getCourseById returns null when course belongs to another tenant`() {
        val course = repository.getCourseById(
            conn = connection,
            courseId = courseId,
            tenantId = tenantB,
            userId = userA
        )
        assertNull(course)
    }

    @Test
    fun `getCourseById returns null when course does not exist`() {
        val nonExistentCourseId = UUID.randomUUID()
        val course = repository.getCourseById(
            conn = connection,
            courseId = nonExistentCourseId,
            tenantId = tenantA,
            userId = userA
        )
        assertNull(course)
    }

    @Test
    fun `getLesson returns existing lesson with completion true for completed student`() {
        // Complete lesson 1 for userA
        repository.completeLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA,
            score = 100
        )

        val result = repository.getLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA
        )

        assertTrue(result is GetLessonResult.Success)
        val lesson = (result as GetLessonResult.Success).lesson
        assertEquals(lesson1Id.toString(), lesson.id)
        assertEquals(courseId.toString(), lesson.courseId)
        assertEquals("Lesson 1", lesson.title)
        assertEquals("Content 1", lesson.content)
        assertEquals(1, lesson.moduleOrder)
        assertEquals(15, lesson.estimatedMinutes)
        assertTrue(lesson.isCompleted)
    }

    @Test
    fun `getLesson completion status is false for student who has not completed it`() {
        // userB has not completed lesson 1
        val result = repository.getLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userB
        )

        assertTrue(result is GetLessonResult.Success)
        val lesson = (result as GetLessonResult.Success).lesson
        assertEquals(lesson1Id.toString(), lesson.id)
        assertFalse(lesson.isCompleted)
    }

    @Test
    fun `getLesson returns CourseNotFound when course does not exist`() {
        val nonExistentCourseId = UUID.randomUUID()
        val result = repository.getLesson(
            conn = connection,
            courseId = nonExistentCourseId,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA
        )
        assertTrue(result is GetLessonResult.CourseNotFound)
    }

    @Test
    fun `getLesson returns CourseNotFound when course belongs to another tenant`() {
        val result = repository.getLesson(
            conn = connection,
            courseId = courseId,
            lessonId = lesson1Id,
            tenantId = tenantB,
            userId = userA
        )
        assertTrue(result is GetLessonResult.CourseNotFound)
    }

    @Test
    fun `getLesson returns LessonNotFound when lesson does not exist`() {
        val nonExistentLessonId = UUID.randomUUID()
        val result = repository.getLesson(
            conn = connection,
            courseId = courseId,
            lessonId = nonExistentLessonId,
            tenantId = tenantA,
            userId = userA
        )
        assertTrue(result is GetLessonResult.LessonNotFound)
    }

    @Test
    fun `getLesson returns LessonNotFound when lesson belongs to another course`() {
        val course2Id = UUID.randomUUID()
        connection.createStatement().use { stmt ->
            stmt.execute("INSERT INTO courses (id, organization_id, title, description, category, difficulty, total_modules, status, created_at, updated_at) VALUES ('$course2Id', '$tenantA', 'Course 2', 'Desc 2', 'General', 'Beginner', 0, 'published', NOW(), NOW())")
        }

        // lesson1Id belongs to courseId, not course2Id
        val result = repository.getLesson(
            conn = connection,
            courseId = course2Id,
            lessonId = lesson1Id,
            tenantId = tenantA,
            userId = userA
        )
        assertTrue(result is GetLessonResult.LessonNotFound)
    }
}

