package com.rtiqa.backend.courses

import java.sql.Connection
import java.util.UUID

class CourseRepository {

    fun getCoursesForTenant(conn: Connection, tenantId: UUID, userId: UUID, category: String? = null): List<CourseResponseDto> {
        val hasCategory = !category.isNullOrBlank()
        val sql = if (hasCategory) {
            """
                SELECT 
                    c.id,
                    c.title,
                    c.description,
                    c.category,
                    c.level,
                    COUNT(DISTINCT l.id) AS total_lessons,
                    COALESCE(e.completed_lessons, 0) AS completed_lessons,
                    COALESCE(e.progress_percent, 0.0) AS progress_percent
                FROM courses c
                LEFT JOIN lessons l ON l.course_id = c.id
                LEFT JOIN enrollments e ON e.course_id = c.id AND e.user_id = ?
                WHERE c.organization_id = ? AND c.category = ?
                GROUP BY c.id, c.title, c.description, c.category, c.level, e.completed_lessons, e.progress_percent
                ORDER BY c.created_at ASC
            """.trimIndent()
        } else {
            """
                SELECT 
                    c.id,
                    c.title,
                    c.description,
                    c.category,
                    c.level,
                    COUNT(DISTINCT l.id) AS total_lessons,
                    COALESCE(e.completed_lessons, 0) AS completed_lessons,
                    COALESCE(e.progress_percent, 0.0) AS progress_percent
                FROM courses c
                LEFT JOIN lessons l ON l.course_id = c.id
                LEFT JOIN enrollments e ON e.course_id = c.id AND e.user_id = ?
                WHERE c.organization_id = ?
                GROUP BY c.id, c.title, c.description, c.category, c.level, e.completed_lessons, e.progress_percent
                ORDER BY c.created_at ASC
            """.trimIndent()
        }

        conn.prepareStatement(sql).use { stmt ->
            var idx = 1
            stmt.setObject(idx++, userId)
            stmt.setObject(idx++, tenantId)
            if (hasCategory) {
                stmt.setString(idx++, category)
            }

            stmt.executeQuery().use { rs ->
                val courses = mutableListOf<CourseResponseDto>()
                while (rs.next()) {
                    courses.add(
                        CourseResponseDto(
                            id = rs.getString("id"),
                            title = rs.getString("title") ?: "",
                            description = rs.getString("description") ?: "",
                            category = rs.getString("category") ?: "عام",
                            difficulty = rs.getString("level") ?: "مبتدئ",
                            totalModules = rs.getInt("total_lessons"),
                            completedModules = rs.getInt("completed_lessons"),
                            progressPercent = rs.getFloat("progress_percent")
                        )
                    )
                }
                return courses
            }
        }
    }

    fun getCourseById(conn: Connection, courseId: UUID, tenantId: UUID, userId: UUID): CourseResponseDto? {
        val sql = """
            SELECT 
                c.id,
                c.title,
                c.description,
                c.category,
                c.level,
                COUNT(DISTINCT l.id) AS total_lessons,
                COALESCE(e.completed_lessons, 0) AS completed_lessons,
                COALESCE(e.progress_percent, 0.0) AS progress_percent
            FROM courses c
            LEFT JOIN lessons l ON l.course_id = c.id
            LEFT JOIN enrollments e ON e.course_id = c.id AND e.user_id = ?
            WHERE c.id = ? AND c.organization_id = ?
            GROUP BY c.id, c.title, c.description, c.category, c.level, e.completed_lessons, e.progress_percent
        """.trimIndent()

        conn.prepareStatement(sql).use { stmt ->
            stmt.setObject(1, userId)
            stmt.setObject(2, courseId)
            stmt.setObject(3, tenantId)

            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return CourseResponseDto(
                        id = rs.getString("id"),
                        title = rs.getString("title") ?: "",
                        description = rs.getString("description") ?: "",
                        category = rs.getString("category") ?: "عام",
                        difficulty = rs.getString("level") ?: "مبتدئ",
                        totalModules = rs.getInt("total_lessons"),
                        completedModules = rs.getInt("completed_lessons"),
                        progressPercent = rs.getFloat("progress_percent")
                    )
                }
                return null
            }
        }
    }

    fun courseExistsInTenant(conn: Connection, courseId: UUID, tenantId: UUID): Boolean {
        val sql = "SELECT 1 FROM courses WHERE id = ? AND organization_id = ?"
        conn.prepareStatement(sql).use { stmt ->
            stmt.setObject(1, courseId)
            stmt.setObject(2, tenantId)
            stmt.executeQuery().use { rs ->
                return rs.next()
            }
        }
    }

    fun getLessonsForCourse(conn: Connection, courseId: UUID, tenantId: UUID, userId: UUID? = null): List<LessonResponseDto> {
        val sql = if (userId != null) {
            """
                SELECT 
                    l.id,
                    l.course_id,
                    l.title,
                    l.content,
                    l.module_order,
                    l.estimated_minutes,
                    COALESCE(pr.completed, false) AS is_completed
                FROM lessons l
                INNER JOIN courses c ON c.id = l.course_id
                LEFT JOIN progress_records pr ON pr.lesson_id = l.id AND pr.user_id = ?
                WHERE l.course_id = ? AND c.organization_id = ?
                ORDER BY l.module_order ASC
            """.trimIndent()
        } else {
            """
                SELECT 
                    l.id,
                    l.course_id,
                    l.title,
                    l.content,
                    l.module_order,
                    l.estimated_minutes,
                    false AS is_completed
                FROM lessons l
                INNER JOIN courses c ON c.id = l.course_id
                WHERE l.course_id = ? AND c.organization_id = ?
                ORDER BY l.module_order ASC
            """.trimIndent()
        }

        conn.prepareStatement(sql).use { stmt ->
            var idx = 1
            if (userId != null) {
                stmt.setObject(idx++, userId)
            }
            stmt.setObject(idx++, courseId)
            stmt.setObject(idx++, tenantId)
            stmt.executeQuery().use { rs ->
                val lessons = mutableListOf<LessonResponseDto>()
                while (rs.next()) {
                    lessons.add(
                        LessonResponseDto(
                            id = rs.getString("id"),
                            courseId = rs.getString("course_id"),
                            title = rs.getString("title") ?: "",
                            content = rs.getString("content") ?: "",
                            moduleOrder = rs.getInt("module_order"),
                            estimatedMinutes = rs.getInt("estimated_minutes"),
                            isCompleted = rs.getBoolean("is_completed")
                        )
                    )
                }
                return lessons
            }
        }
    }

    fun getLesson(
        conn: Connection,
        courseId: UUID,
        lessonId: UUID,
        tenantId: UUID,
        userId: UUID? = null
    ): GetLessonResult {
        // 1. Verify course belongs to tenant
        if (!courseExistsInTenant(conn, courseId, tenantId)) {
            return GetLessonResult.CourseNotFound
        }

        // 2. Fetch lesson if it belongs to course and tenant
        val sql = if (userId != null) {
            """
                SELECT 
                    l.id,
                    l.course_id,
                    l.title,
                    l.content,
                    l.module_order,
                    l.estimated_minutes,
                    COALESCE(pr.completed, false) AS is_completed
                FROM lessons l
                INNER JOIN courses c ON c.id = l.course_id
                LEFT JOIN progress_records pr ON pr.lesson_id = l.id AND pr.user_id = ?
                WHERE l.id = ? AND l.course_id = ? AND c.organization_id = ?
            """.trimIndent()
        } else {
            """
                SELECT 
                    l.id,
                    l.course_id,
                    l.title,
                    l.content,
                    l.module_order,
                    l.estimated_minutes,
                    false AS is_completed
                FROM lessons l
                INNER JOIN courses c ON c.id = l.course_id
                WHERE l.id = ? AND l.course_id = ? AND c.organization_id = ?
            """.trimIndent()
        }

        conn.prepareStatement(sql).use { stmt ->
            var idx = 1
            if (userId != null) {
                stmt.setObject(idx++, userId)
            }
            stmt.setObject(idx++, lessonId)
            stmt.setObject(idx++, courseId)
            stmt.setObject(idx++, tenantId)

            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val lesson = LessonResponseDto(
                        id = rs.getString("id"),
                        courseId = rs.getString("course_id"),
                        title = rs.getString("title") ?: "",
                        content = rs.getString("content") ?: "",
                        moduleOrder = rs.getInt("module_order"),
                        estimatedMinutes = rs.getInt("estimated_minutes"),
                        isCompleted = rs.getBoolean("is_completed")
                    )
                    return GetLessonResult.Success(lesson)
                }
                return GetLessonResult.LessonNotFound
            }
        }
    }

    fun completeLesson(
        conn: Connection,
        courseId: UUID,
        lessonId: UUID,
        tenantId: UUID,
        userId: UUID,
        score: Int = 100
    ): CompleteLessonResult {
        // 1. Verify course belongs to tenant
        val checkCourseSql = "SELECT id FROM courses WHERE id = ? AND organization_id = ?"
        val courseExists = conn.prepareStatement(checkCourseSql).use { stmt ->
            stmt.setObject(1, courseId)
            stmt.setObject(2, tenantId)
            stmt.executeQuery().use { it.next() }
        }
        if (!courseExists) return CompleteLessonResult.CourseNotFound

        // 2. Verify lesson belongs to course
        val checkLessonSql = "SELECT id FROM lessons WHERE id = ? AND course_id = ?"
        val lessonExists = conn.prepareStatement(checkLessonSql).use { stmt ->
            stmt.setObject(1, lessonId)
            stmt.setObject(2, courseId)
            stmt.executeQuery().use { it.next() }
        }
        if (!lessonExists) return CompleteLessonResult.LessonNotFound

        // 3. Verify user is enrolled in course
        val checkEnrollmentSql = "SELECT status FROM enrollments WHERE user_id = ? AND course_id = ?"
        val isEnrolled = conn.prepareStatement(checkEnrollmentSql).use { stmt ->
            stmt.setObject(1, userId)
            stmt.setObject(2, courseId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val status = rs.getString("status")
                    status != "dropped" && status != "suspended"
                } else {
                    false
                }
            }
        }
        if (!isEnrolled) return CompleteLessonResult.NotEnrolled

        // 4. Idempotent UPSERT into progress_records
        val checkProgressSql = "SELECT id, score FROM progress_records WHERE user_id = ? AND lesson_id = ?"
        val existingProgress = conn.prepareStatement(checkProgressSql).use { stmt ->
            stmt.setObject(1, userId)
            stmt.setObject(2, lessonId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    Pair(rs.getObject("id"), rs.getInt("score"))
                } else {
                    null
                }
            }
        }

        if (existingProgress != null) {
            val newScore = maxOf(existingProgress.second, score)
            val updateProgressSql = "UPDATE progress_records SET completed = TRUE, score = ?, updated_at = NOW() WHERE id = ?"
            conn.prepareStatement(updateProgressSql).use { stmt ->
                stmt.setInt(1, newScore)
                stmt.setObject(2, existingProgress.first)
                stmt.executeUpdate()
            }
        } else {
            val insertProgressSql = "INSERT INTO progress_records (id, user_id, lesson_id, completed, score, updated_at) VALUES (gen_random_uuid(), ?, ?, TRUE, ?, NOW())"
            conn.prepareStatement(insertProgressSql).use { stmt ->
                stmt.setObject(1, userId)
                stmt.setObject(2, lessonId)
                stmt.setInt(3, score)
                stmt.executeUpdate()
            }
        }

        // 5. Total lessons in course
        val totalLessonsSql = "SELECT COUNT(*) FROM lessons WHERE course_id = ?"
        val totalLessons = conn.prepareStatement(totalLessonsSql).use { stmt ->
            stmt.setObject(1, courseId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }

        // 6. Completed lessons for this user in course
        val completedLessonsSql = """
            SELECT COUNT(DISTINCT pr.lesson_id)
            FROM progress_records pr
            JOIN lessons l ON l.id = pr.lesson_id
            WHERE pr.user_id = ? AND l.course_id = ? AND pr.completed = TRUE
        """.trimIndent()
        val completedLessons = conn.prepareStatement(completedLessonsSql).use { stmt ->
            stmt.setObject(1, userId)
            stmt.setObject(2, courseId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }

        val progressPercent = if (totalLessons > 0) {
            (completedLessons.toFloat() / totalLessons.toFloat()) * 100.0f
        } else {
            0.0f
        }

        val isComplete = completedLessons >= totalLessons && totalLessons > 0
        val updateEnrollmentSql = """
            UPDATE enrollments
            SET completed_lessons = ?,
                progress_percent = ?,
                last_accessed_at = NOW(),
                completed_at = CASE WHEN ? = TRUE THEN COALESCE(completed_at, NOW()) ELSE completed_at END,
                status = CASE WHEN ? = TRUE THEN 'completed' ELSE status END
            WHERE user_id = ? AND course_id = ?
        """.trimIndent()
        conn.prepareStatement(updateEnrollmentSql).use { stmt ->
            stmt.setInt(1, completedLessons)
            stmt.setFloat(2, progressPercent)
            stmt.setBoolean(3, isComplete)
            stmt.setBoolean(4, isComplete)
            stmt.setObject(5, userId)
            stmt.setObject(6, courseId)
            stmt.executeUpdate()
        }

        return CompleteLessonResult.Success(
            LessonCompletionResponseDto(
                success = true,
                lessonId = lessonId.toString(),
                courseId = courseId.toString(),
                completed = true,
                courseProgressPercent = progressPercent,
                completedLessons = completedLessons,
                totalLessons = totalLessons
            )
        )
    }

    fun createCourse(
        conn: Connection,
        tenantId: UUID,
        request: CreateCourseRequestDto,
        courseId: UUID = UUID.randomUUID()
    ): CourseResponseDto {
        val trimmedTitle = request.title.trim()
        val resolvedDescription = request.description?.trim() ?: ""
        val resolvedCategory = request.category?.trim()?.ifBlank { "عام" } ?: "عام"
        val resolvedLevel = (request.difficulty ?: request.level)?.trim()?.ifBlank { "مبتدئ" } ?: "مبتدئ"
        val durationMinutes = request.durationMinutes ?: 0
        val iconUrl = request.iconUrl ?: request.imageUrl

        val sql = """
            INSERT INTO courses (id, organization_id, title, description, category, level, duration_minutes, icon_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
        """.trimIndent()

        conn.prepareStatement(sql).use { stmt ->
            var idx = 1
            stmt.setObject(idx++, courseId)
            stmt.setObject(idx++, tenantId)
            stmt.setString(idx++, trimmedTitle)
            stmt.setString(idx++, resolvedDescription)
            stmt.setString(idx++, resolvedCategory)
            stmt.setString(idx++, resolvedLevel)
            stmt.setInt(idx++, durationMinutes)
            stmt.setString(idx++, iconUrl)
            stmt.executeUpdate()
        }

        return CourseResponseDto(
            id = courseId.toString(),
            title = trimmedTitle,
            description = resolvedDescription,
            category = resolvedCategory,
            difficulty = resolvedLevel,
            totalModules = 0,
            completedModules = 0,
            progressPercent = 0.0f
        )
    }

    fun createLesson(
        conn: Connection,
        courseId: UUID,
        tenantId: UUID,
        request: CreateLessonRequestDto,
        lessonId: UUID = UUID.randomUUID()
    ): CreateLessonResult {
        // 1. Verify course belongs to tenant
        val checkCourseSql = "SELECT id FROM courses WHERE id = ? AND organization_id = ?"
        val courseExists = conn.prepareStatement(checkCourseSql).use { stmt ->
            stmt.setObject(1, courseId)
            stmt.setObject(2, tenantId)
            stmt.executeQuery().use { it.next() }
        }
        if (!courseExists) return CreateLessonResult.CourseNotFound

        val trimmedTitle = request.title.trim()
        val resolvedContent = request.content?.trim() ?: ""
        val estimatedMinutes = if (request.estimatedMinutes != null && request.estimatedMinutes > 0) {
            request.estimatedMinutes
        } else {
            15
        }

        val resolvedOrder = if (request.moduleOrder != null && request.moduleOrder > 0) {
            request.moduleOrder
        } else {
            val nextOrderSql = "SELECT COALESCE(MAX(module_order), 0) + 1 FROM lessons WHERE course_id = ?"
            conn.prepareStatement(nextOrderSql).use { stmt ->
                stmt.setObject(1, courseId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getInt(1) else 1
                }
            }
        }

        val insertSql = """
            INSERT INTO lessons (id, course_id, title, content, module_order, estimated_minutes, audio_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
        """.trimIndent()

        conn.prepareStatement(insertSql).use { stmt ->
            var idx = 1
            stmt.setObject(idx++, lessonId)
            stmt.setObject(idx++, courseId)
            stmt.setString(idx++, trimmedTitle)
            stmt.setString(idx++, resolvedContent)
            stmt.setInt(idx++, resolvedOrder)
            stmt.setInt(idx++, estimatedMinutes)
            stmt.setString(idx++, request.audioUrl)
            stmt.executeUpdate()
        }

        return CreateLessonResult.Success(
            LessonResponseDto(
                id = lessonId.toString(),
                courseId = courseId.toString(),
                title = trimmedTitle,
                content = resolvedContent,
                moduleOrder = resolvedOrder,
                estimatedMinutes = estimatedMinutes,
                isCompleted = false
            )
        )
    }
}
