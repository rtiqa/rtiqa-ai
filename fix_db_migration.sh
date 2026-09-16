#!/bin/bash
sed -i 's/version = 7,/version = 8,/' ./core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt

# Add MIGRATION_7_8
sed -i '/val MIGRATION_6_7/i \
        val MIGRATION_7_8 = object : Migration(7, 8) {\
            override fun migrate(db: SupportSQLiteDatabase) {\
                db.execSQL("ALTER TABLE `schools` ADD COLUMN `orgId` TEXT NOT NULL DEFAULT '\'''\''")\
            }\
        }\
' ./core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt

# Update addMigrations
sed -i 's/.addMigrations(MIGRATION_1_2, MIGRATION_6_7)/.addMigrations(MIGRATION_1_2, MIGRATION_6_7, MIGRATION_7_8)/' ./core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt
