package com.rtiqa.backend.gradebook

import java.sql.Connection
import java.util.UUID

class GradebookRepository {

    fun getClassGradebook(
        conn: Connection,
        classId: UUID,
        tenantId: UUID
    ): GetClassGradebookResult {
        // Step 1: Mandatory tenant check. Verify the class belongs to the requested tenant.
        val classCheckSql = """
            SELECT 1
            FROM academic_classes
            WHERE id = ?
              AND organization_id = ?
        """.trimIndent()

        val classExists = conn.prepareStatement(classCheckSql).use { stmt ->
            stmt.setObject(1, classId)
            stmt.setObject(2, tenantId)
            stmt.executeQuery().use { rs ->
                rs.next()
            }
        }

        if (!classExists) {
            return GetClassGradebookResult.ClassNotFound
        }

        // Step 2: Read students enrolled in the verified class
        val studentsSql = """
            SELECT 
                p.id AS student_id,
                p.name AS display_name,
                cs.student_number AS student_number
            FROM class_students cs
            JOIN profiles p ON p.id = cs.student_id
            WHERE cs.class_id = ?
            ORDER BY p.name ASC, p.id ASC
        """.trimIndent()

        val students = conn.prepareStatement(studentsSql).use { stmt ->
            stmt.setObject(1, classId)
            stmt.executeQuery().use { rs ->
                val list = mutableListOf<GradebookStudentDto>()
                while (rs.next()) {
                    val sId = rs.getObject("student_id")?.toString() ?: rs.getString("student_id")
                    val name = rs.getString("display_name")
                    val studentNum = rs.getString("student_number")
                    list.add(
                        GradebookStudentDto(
                            studentId = sId,
                            displayName = name,
                            studentNumber = studentNum
                        )
                    )
                }
                list
            }
        }

        // Step 3: Read assessments belonging to the verified class
        val assessmentsSql = """
            SELECT 
                id AS assessment_id,
                title,
                max_score,
                weight
            FROM gradebook_assessments
            WHERE class_id = ?
            ORDER BY created_at ASC, id ASC
        """.trimIndent()

        val assessments = conn.prepareStatement(assessmentsSql).use { stmt ->
            stmt.setObject(1, classId)
            stmt.executeQuery().use { rs ->
                val list = mutableListOf<GradebookAssessmentDto>()
                while (rs.next()) {
                    val aId = rs.getObject("assessment_id")?.toString() ?: rs.getString("assessment_id")
                    val title = rs.getString("title")
                    val maxScore = rs.getDouble("max_score")
                    val rawWeight = rs.getObject("weight")
                    val weight = if (rawWeight != null && !rs.wasNull()) {
                        (rawWeight as? Number)?.toDouble() ?: rs.getDouble("weight")
                    } else {
                        null
                    }
                    list.add(
                        GradebookAssessmentDto(
                            assessmentId = aId,
                            title = title,
                            maxScore = maxScore,
                            weight = weight
                        )
                    )
                }
                list
            }
        }

        // Step 4: Read scores ONLY when:
        // A. assessment belongs to requested class
        // B. student is enrolled in requested class
        val scoresSql = """
            SELECT 
                gs.student_id,
                gs.assessment_id,
                gs.score
            FROM gradebook_scores gs
            INNER JOIN gradebook_assessments ga ON ga.id = gs.assessment_id
            INNER JOIN class_students cs ON cs.student_id = gs.student_id AND cs.class_id = ga.class_id
            WHERE ga.class_id = ?
            ORDER BY gs.assessment_id ASC, gs.student_id ASC
        """.trimIndent()

        val scores = conn.prepareStatement(scoresSql).use { stmt ->
            stmt.setObject(1, classId)
            stmt.executeQuery().use { rs ->
                val list = mutableListOf<GradebookScoreDto>()
                while (rs.next()) {
                    val sId = rs.getObject("student_id")?.toString() ?: rs.getString("student_id")
                    val aId = rs.getObject("assessment_id")?.toString() ?: rs.getString("assessment_id")
                    val rawScore = rs.getObject("score")
                    val score = if (rawScore != null && !rs.wasNull()) {
                        (rawScore as? Number)?.toDouble() ?: rs.getDouble("score")
                    } else {
                        null
                    }
                    list.add(
                        GradebookScoreDto(
                            studentId = sId,
                            assessmentId = aId,
                            score = score
                        )
                    )
                }
                list
            }
        }

        return GetClassGradebookResult.Success(
            ClassGradebookDto(
                classId = classId.toString(),
                students = students,
                assessments = assessments,
                scores = scores
            )
        )
    }
}
