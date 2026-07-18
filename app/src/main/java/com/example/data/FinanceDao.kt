package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // Persons
    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    suspend fun getPersonById(id: Int): Person?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long
    @Update
    suspend fun updatePerson(person: Person)


    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun deletePersonById(id: Int)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY timestamp DESC")
    fun getTransactionsByPerson(personId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("DELETE FROM transactions WHERE personId = :personId")
    suspend fun deleteTransactionsByPersonId(personId: Int)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // Savings Goals
    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getSavingsGoalById(id: Int): SavingsGoal?

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteSavingsGoalById(id: Int)

    // Backup & Restore Bulk Helpers
    @Query("SELECT * FROM persons")
    suspend fun getAllPersonsList(): List<Person>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<Transaction>

    @Query("SELECT * FROM savings_goals")
    suspend fun getAllSavingsGoalsList(): List<SavingsGoal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersons(persons: List<Person>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoals(goals: List<SavingsGoal>)

    @Query("DELETE FROM persons")
    suspend fun deleteAllPersons()

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAllSavingsGoals()

    // Savings Transactions
    @Query("SELECT * FROM savings_transactions ORDER BY timestamp DESC")
    fun getAllSavingsTransactions(): Flow<List<SavingsTransaction>>

    @Query("SELECT * FROM savings_transactions WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getSavingsTransactionsByGoal(goalId: Int): Flow<List<SavingsTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsTransaction(transaction: SavingsTransaction): Long

    @Update
    suspend fun updateSavingsTransaction(transaction: SavingsTransaction)

    @Query("DELETE FROM savings_transactions WHERE goalId = :goalId")
    suspend fun deleteSavingsTransactionsByGoal(goalId: Int)
    
    @Query("SELECT * FROM savings_transactions WHERE id = :id LIMIT 1")
    suspend fun getSavingsTransactionById(id: Int): SavingsTransaction?

    @Query("SELECT * FROM transactions WHERE personId = :personId")
    suspend fun getTransactionsByPersonList(personId: Int): List<Transaction>

    @Query("SELECT * FROM savings_transactions WHERE goalId = :goalId")
    suspend fun getSavingsTransactionsByGoalList(goalId: Int): List<SavingsTransaction>


    @Query("DELETE FROM savings_transactions WHERE id = :id")
    suspend fun deleteSavingsTransactionById(id: Int)

    @Query("SELECT * FROM savings_transactions")
    suspend fun getAllSavingsTransactionsList(): List<SavingsTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsTransactions(transactions: List<SavingsTransaction>)

    @Query("DELETE FROM savings_transactions")
    suspend fun deleteAllSavingsTransactions()

    // Trash
    @Query("SELECT * FROM trash_items ORDER BY deletedAt DESC")
    fun getAllTrashItems(): Flow<List<TrashItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashItem(item: TrashItem)

    @Query("DELETE FROM trash_items WHERE id = :id")
    suspend fun deleteTrashItemById(id: Int)

    @Query("DELETE FROM trash_items WHERE deletedAt < :threshold")
    suspend fun deleteOldTrashItems(threshold: Long)
    
    @Query("SELECT * FROM trash_items WHERE id = :id LIMIT 1")
    suspend fun getTrashItemById(id: Int): TrashItem?

    @Query("SELECT * FROM trash_items")
    suspend fun getAllTrashItemsList(): List<TrashItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashItems(items: List<TrashItem>)

    @Query("DELETE FROM trash_items")
    suspend fun deleteAllTrashItems()

    // Workspaces
    @Query("SELECT * FROM workspaces WHERE id = :id LIMIT 1")
    suspend fun getWorkspaceById(id: String): Workspace?

    @Query("SELECT * FROM workspaces ORDER BY createdAt ASC")
    fun getAllWorkspaces(): Flow<List<Workspace>>

    @Query("SELECT * FROM workspaces")
    suspend fun getAllWorkspacesList(): List<Workspace>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: Workspace)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspaces(workspaces: List<Workspace>)

    @Query("DELETE FROM workspaces WHERE id = :id")
    suspend fun deleteWorkspaceById(id: String)

    @Query("DELETE FROM workspaces")
    suspend fun deleteAllWorkspaces()

    @Query("DELETE FROM persons WHERE workspaceId = :workspaceId")
    suspend fun deletePersonsByWorkspace(workspaceId: String)

    @Query("DELETE FROM transactions WHERE workspaceId = :workspaceId")
    suspend fun deleteTransactionsByWorkspace(workspaceId: String)

    @Query("DELETE FROM savings_goals WHERE workspaceId = :workspaceId")
    suspend fun deleteSavingsGoalsByWorkspace(workspaceId: String)

    @Query("DELETE FROM savings_transactions WHERE workspaceId = :workspaceId")
    suspend fun deleteSavingsTransactionsByWorkspace(workspaceId: String)
}

