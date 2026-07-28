package com.rtiqa.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rtiqa.core.database.dao.AiInsightDao
import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.LessonDao
import com.rtiqa.core.database.dao.SyncDao
import com.rtiqa.core.database.dao.UserProfileDao
import com.rtiqa.core.database.entity.AiInsightEntity
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.database.entity.SyncQueueEntity
import com.rtiqa.core.database.entity.UserProfileEntity

@Database(
    entities = [
        CourseEntity::class,
        LessonEntity::class,
        UserProfileEntity::class,
        AiInsightEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RtiqaDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun aiInsightDao(): AiInsightDao
    abstract fun syncDao(): SyncDao

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
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
