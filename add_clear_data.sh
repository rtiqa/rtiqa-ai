#!/bin/bash
sed -i '/abstract fun schoolManagementCoreDao(): SchoolManagementCoreDao/a\
\
    fun clearSensitiveData() {\
        runInTransaction {\
            val tables = listOf(\
                "courses", "lessons", "schools", "school_classes", "sync_queue",\
                "organizations", "branches", "academic_years", "semesters", "departments",\
                "majors", "sections", "subjects", "study_plans", "enterprise_members",\
                "curriculum_modules", "academic_lessons", "assignments", "assignment_submissions",\
                "question_bank", "assessments", "assessment_attempts", "gradebook_records",\
                "student_progress", "achievement_badges", "learning_paths", "prerequisites",\
                "smart_recommendations", "grade_levels", "teacher_assignments", "student_enrollments"\
            )\
            tables.forEach {\
                compileStatement("DELETE FROM $it").execute()\
            }\
        }\
    }\
' /app/applet/core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt
