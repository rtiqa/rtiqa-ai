import re
content = open('core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt').read()
content = content.replace('version = 6,', 'version = 7,')

migration_code = """        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `sync_queue` ADD COLUMN `ownerUserId` TEXT NOT NULL DEFAULT 'legacy_user'")
                db.execSQL("ALTER TABLE `sync_queue` ADD COLUMN `ownerSessionId` TEXT NOT NULL DEFAULT 'legacy_session'")
            }
        }"""

content = content.replace('        val MIGRATION_1_2 =', migration_code + '\n        val MIGRATION_1_2 =')
content = content.replace('.addMigrations(MIGRATION_1_2)', '.addMigrations(MIGRATION_1_2, MIGRATION_6_7)')

open('core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt', 'w').write(content)
