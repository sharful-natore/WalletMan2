package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val allPersons: Flow<List<Person>> = financeDao.getAllPersons()
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allSavingsGoals: Flow<List<SavingsGoal>> = financeDao.getAllSavingsGoals()
    val allSavingsTransactions: Flow<List<SavingsTransaction>> = financeDao.getAllSavingsTransactions()

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

    suspend fun updateSavingsTransaction(transaction: SavingsTransaction) {
        financeDao.updateSavingsTransaction(transaction)
    }
    
    suspend fun deleteSavingsTransaction(id: Int) {
        financeDao.deleteSavingsTransactionById(id)
    }

    suspend fun getBackupData(): FinanceBackup {
        return FinanceBackup(
            persons = financeDao.getAllPersonsList(),
            transactions = financeDao.getAllTransactionsList(),
            savingsGoals = financeDao.getAllSavingsGoalsList(),
            savingsTransactions = financeDao.getAllSavingsTransactionsList(),
            workspaces = financeDao.getAllWorkspacesList(),
            trashItems = financeDao.getAllTrashItemsList()
        )
    }

    suspend fun restoreBackupData(backup: FinanceBackup) {
        financeDao.deleteAllPersons()
        financeDao.deleteAllTransactions()
        financeDao.deleteAllSavingsGoals()
        financeDao.deleteAllSavingsTransactions()
        financeDao.deleteAllWorkspaces()
        financeDao.deleteAllTrashItems()

        financeDao.insertPersons(backup.persons)
        financeDao.insertTransactions(backup.transactions)
        financeDao.insertSavingsGoals(backup.savingsGoals)
        if (backup.savingsTransactions.isNotEmpty()) {
            financeDao.insertSavingsTransactions(backup.savingsTransactions)
        }
        if (backup.workspaces.isNotEmpty()) {
            financeDao.insertWorkspaces(backup.workspaces)
        } else {
            financeDao.insertWorkspace(Workspace(id = "default", name = "ব্যক্তিগত"))
        }
        if (backup.trashItems.isNotEmpty()) {
            financeDao.insertTrashItems(backup.trashItems)
        }
    }

    // Workspaces
    val allWorkspaces: Flow<List<Workspace>> = financeDao.getAllWorkspaces()
    suspend fun getWorkspaceById(id: String): Workspace? = financeDao.getWorkspaceById(id)
    suspend fun insertWorkspace(workspace: Workspace) = financeDao.insertWorkspace(workspace)
    suspend fun deleteWorkspace(workspaceId: String) {
        financeDao.deleteWorkspaceById(workspaceId)
        financeDao.deletePersonsByWorkspace(workspaceId)
        financeDao.deleteTransactionsByWorkspace(workspaceId)
        financeDao.deleteSavingsGoalsByWorkspace(workspaceId)
        financeDao.deleteSavingsTransactionsByWorkspace(workspaceId)
    }

    // Trash
    val allTrashItems: Flow<List<TrashItem>> = financeDao.getAllTrashItems()
    suspend fun insertTrashItem(item: TrashItem) = financeDao.insertTrashItem(item)
    suspend fun deleteTrashItemById(id: Int) = financeDao.deleteTrashItemById(id)
    suspend fun deleteOldTrashItems(threshold: Long) = financeDao.deleteOldTrashItems(threshold)
    suspend fun getTrashItemById(id: Int): TrashItem? = financeDao.getTrashItemById(id)

    suspend fun getTransactionById(id: Int) = financeDao.getTransactionById(id)
    suspend fun getSavingsGoalById(id: Int) = financeDao.getSavingsGoalById(id)
    suspend fun getSavingsTransactionById(id: Int) = financeDao.getSavingsTransactionById(id)

    suspend fun getTransactionsByPersonList(personId: Int) = financeDao.getTransactionsByPersonList(personId)
    suspend fun getSavingsTransactionsByGoalList(goalId: Int) = financeDao.getSavingsTransactionsByGoalList(goalId)

    suspend fun getDebtNotificationLog(personId: Int): DebtNotificationLog? = financeDao.getDebtNotificationLog(personId)
    suspend fun insertDebtNotificationLog(log: DebtNotificationLog) = financeDao.insertDebtNotificationLog(log)
    suspend fun deleteDebtNotificationLog(personId: Int) = financeDao.deleteDebtNotificationLog(personId)
}

