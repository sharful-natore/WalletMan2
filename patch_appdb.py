with open('app/src/main/java/com/example/data/AppDatabase.kt', 'r') as f:
    content = f.read()

import re

# Add migration from 6 to 7
migration_code = """import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `trash_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `originalId` INTEGER NOT NULL, `itemType` TEXT NOT NULL, `itemJson` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL)")
    }
}
"""

content = content.replace('import android.content.Context', 'import android.content.Context\nimport androidx.room.migration.Migration\nimport androidx.sqlite.db.SupportSQLiteDatabase')
content = content.replace('version = 6', 'version = 7')
content = content.replace('fallbackToDestructiveMigration().build()', 'fallbackToDestructiveMigration().addMigrations(MIGRATION_6_7).build()')
content = content + "\n" + """val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `trash_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `originalId` INTEGER NOT NULL, `itemType` TEXT NOT NULL, `itemJson` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL)")
    }
}"""

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'w') as f:
    f.write(content)
