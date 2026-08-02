package com.rtiqa.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rtiqa.core.database.dao.AcademicDao
import com.rtiqa.core.database.dao.AiInsightDao
import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.EnterpriseDao
import com.rtiqa.core.database.dao.LessonDao
import com.rtiqa.core.database.dao.SyncDao
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.AcademicLessonEntity
import com.rtiqa.core.database.entity.AcademicYearEntity
import com.rtiqa.core.database.entity.AchievementBadgeEntity
import com.rtiqa.core.database.entity.AiInsightEntity
import com.rtiqa.core.database.entity.AssessmentAttemptEntity
import com.rtiqa.core.database.entity.AssessmentEntity
import com.rtiqa.core.database.entity.AssignmentEntity
import com.rtiqa.core.database.entity.AssignmentSubmissionEntity
import com.rtiqa.core.database.entity.BranchEntity
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.CurriculumModuleEntity
import com.rtiqa.core.database.entity.DepartmentEntity
import com.rtiqa.core.database.entity.EnterpriseMemberEntity
import com.rtiqa.core.database.entity.GradebookRecordEntity
import com.rtiqa.core.database.entity.LearningPathEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.database.entity.MajorEntity
import com.rtiqa.core.database.entity.OfflineContentDownloadEntity
import com.rtiqa.core.database.entity.OrganizationEntity
import com.rtiqa.core.database.entity.PrerequisiteEntity
import com.rtiqa.core.database.entity.QuestionBankEntity
import com.rtiqa.core.database.entity.SchoolEntity
import com.rtiqa.core.database.entity.SectionEntity
import com.rtiqa.core.database.entity.SemesterEntity
import com.rtiqa.core.database.entity.SmartRecommendationEntity
import com.rtiqa.core.database.entity.StudentProgressEntity
import com.rtiqa.core.database.entity.StudyPlanEntity
import com.rtiqa.core.database.entity.SubjectEntity
import com.rtiqa.core.database.entity.SyncQueueEntity
import com.rtiqa.core.database.entity.UserProfileEntity

@Database(
    entities = [
        SchoolEntity::class,
        CourseEntity::class,
        LessonEntity::class,
        UserProfileEntity::class,
        AiInsightEntity::class,
        SyncQueueEntity::class,
        OrganizationEntity::class,
        BranchEntity::class,
        AcademicYearEntity::class,
        SemesterEntity::class,
        DepartmentEntity::class,
        MajorEntity::class,
        SectionEntity::class,
        SubjectEntity::class,
        StudyPlanEntity::class,
        EnterpriseMemberEntity::class,
        CurriculumModuleEntity::class,
        AcademicLessonEntity::class,
        AssignmentEntity::class,
        AssignmentSubmissionEntity::class,
        QuestionBankEntity::class,
        AssessmentEntity::class,
        AssessmentAttemptEntity::class,
        GradebookRecordEntity::class,
        StudentProgressEntity::class,
        AchievementBadgeEntity::class,
        LearningPathEntity::class,
        PrerequisiteEntity::class,
        SmartRecommendationEntity::class,
        OfflineContentDownloadEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class RtiqaDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun aiInsightDao(): AiInsightDao
    abstract fun syncDao(): SyncDao
    abstract fun enterpriseDao(): EnterpriseDao
    abstract fun academicDao(): AcademicDao

    companion object {
        @Volatile
        private var INSTANCE: RtiqaDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_queue` (
                        `id` TEXT NOT NULL,
                        `actionType` TEXT NOT NULL,
                        `payloadJson` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `retryCount` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): RtiqaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RtiqaDatabase::class.java,
                    "rtiqa_database.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
