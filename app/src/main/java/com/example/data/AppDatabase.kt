package com.example.data

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Person::class, Transaction::class, SavingsGoal::class, SavingsTransaction::class, TrashItem::class],
    version = 7,
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
                ).fallbackToDestructiveMigration().addMigrations(MIGRATION_6_7).build()
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