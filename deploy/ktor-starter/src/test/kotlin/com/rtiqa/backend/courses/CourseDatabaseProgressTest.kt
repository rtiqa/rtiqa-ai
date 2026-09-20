package com.rtiqa.backend.courses

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
                    difficulty VARCHAR(50),
                    total_modules INT DEFAULT 0,
                    status VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
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
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
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
}
