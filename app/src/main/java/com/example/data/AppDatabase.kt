package com.example.data

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Person::class, Transaction::class, SavingsGoal::class, SavingsTransaction::class, TrashItem::class, Workspace::class, DebtNotificationLog::class, MonthlyBudget::class],
    version = 12,
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
                ).fallbackToDestructiveMigration().addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12).build()
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

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `budgetIncome` REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `budgetExpense` REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `budgetSavings` REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `profileName` TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `profileEmail` TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `profilePhone` TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `profileSocial` TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `profileAddress` TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE `workspaces` ADD COLUMN `profilePhotoUri` TEXT")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `debt_notification_logs` (`personId` INTEGER NOT NULL, `lastNotifiedAt` INTEGER NOT NULL, PRIMARY KEY(`personId`))")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `monthly_budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `year` INTEGER NOT NULL, `month` INTEGER NOT NULL, `income` REAL NOT NULL, `expense` REAL NOT NULL, `savings` REAL NOT NULL, `workspaceId` TEXT NOT NULL)")
    }
}
