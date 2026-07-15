package com.example.data

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Person::class, Transaction::class, SavingsGoal::class, SavingsTransaction::class, TrashItem::class, Workspace::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "financenote_finance_db"
                ).fallbackToDestructiveMigration().addMigrations(MIGRATION_6_7, MIGRATION_7_8).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `trash_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `originalId` INTEGER NOT NULL, `itemType` TEXT NOT NULL, `itemJson` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL)")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `persons` ADD COLUMN `workspaceId` TEXT NOT NULL DEFAULT 'default'")
        database.execSQL("ALTER TABLE `transactions` ADD COLUMN `workspaceId` TEXT NOT NULL DEFAULT 'default'")
        database.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `workspaceId` TEXT NOT NULL DEFAULT 'default'")
        database.execSQL("ALTER TABLE `savings_transactions` ADD COLUMN `workspaceId` TEXT NOT NULL DEFAULT 'default'")
        
        database.execSQL("CREATE TABLE IF NOT EXISTS `workspaces` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("INSERT OR IGNORE INTO `workspaces` (id, name, createdAt) VALUES ('default', 'ব্যক্তিগত', ${System.currentTimeMillis()})")
    }
}