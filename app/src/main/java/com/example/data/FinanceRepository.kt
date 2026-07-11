package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val allPersons: Flow<List<Person>> = financeDao.getAllPersons()
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allSavingsGoals: Flow<List<SavingsGoal>> = financeDao.getAllSavingsGoals()

    fun getTransactionsByPerson(personId: Int): Flow<List<Transaction>> {
        return financeDao.getTransactionsByPerson(personId)
    }

    suspend fun getPersonById(id: Int): Person? {
        return financeDao.getPersonById(id)
    }

    suspend fun insertPerson(person: Person): Long {
        return financeDao.insertPerson(person)
    }
    suspend fun updatePerson(person: Person) {
        financeDao.updatePerson(person)
    }


    suspend fun deletePerson(id: Int) {
        // First delete their related transactions to avoid orphaned entries
        financeDao.deleteTransactionsByPersonId(id)
        financeDao.deletePersonById(id)
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        return financeDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        financeDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) {
        financeDao.deleteTransactionById(id)
    }

    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long {
        return financeDao.insertSavingsGoal(savingsGoal)
    }

    suspend fun deleteSavingsGoal(id: Int) {
        financeDao.deleteSavingsTransactionsByGoal(id)
        financeDao.deleteSavingsGoalById(id)
    }

    fun getSavingsTransactionsByGoal(goalId: Int): Flow<List<SavingsTransaction>> {
        return financeDao.getSavingsTransactionsByGoal(goalId)
    }

    suspend fun insertSavingsTransaction(transaction: SavingsTransaction): Long {
        return financeDao.insertSavingsTransaction(transaction)
    }
    
    suspend fun deleteSavingsTransaction(id: Int) {
        financeDao.deleteSavingsTransactionById(id)
    }

    suspend fun getBackupData(): FinanceBackup {
        return FinanceBackup(
            persons = financeDao.getAllPersonsList(),
            transactions = financeDao.getAllTransactionsList(),
            savingsGoals = financeDao.getAllSavingsGoalsList(),
            savingsTransactions = financeDao.getAllSavingsTransactionsList()
        )
    }

    suspend fun restoreBackupData(backup: FinanceBackup) {
        financeDao.deleteAllPersons()
        financeDao.deleteAllTransactions()
        financeDao.deleteAllSavingsGoals()
        financeDao.deleteAllSavingsTransactions()

        financeDao.insertPersons(backup.persons)
        financeDao.insertTransactions(backup.transactions)
        financeDao.insertSavingsGoals(backup.savingsGoals)
        if (backup.savingsTransactions.isNotEmpty()) {
            financeDao.insertSavingsTransactions(backup.savingsTransactions)
        }
    }
}
