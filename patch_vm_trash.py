with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

import re

# Add moshi adapters
adapters_code = """
    private val personAdapter = moshi.adapter(Person::class.java)
    private val transactionAdapter = moshi.adapter(Transaction::class.java)
    private val savingsGoalAdapter = moshi.adapter(SavingsGoal::class.java)
    private val savingsTransactionAdapter = moshi.adapter(SavingsTransaction::class.java)
    
    val allTrashItems = repository.allTrashItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun restoreTrashItem(item: TrashItem) {
        viewModelScope.launch {
            when (item.itemType) {
                "PERSON" -> {
                    personAdapter.fromJson(item.itemJson)?.let { repository.insertPerson(it) }
                }
                "TRANSACTION" -> {
                    transactionAdapter.fromJson(item.itemJson)?.let { repository.insertTransaction(it) }
                }
                "SAVINGS_GOAL" -> {
                    savingsGoalAdapter.fromJson(item.itemJson)?.let { repository.insertSavingsGoal(it) }
                }
                "SAVINGS_TRANSACTION" -> {
                    savingsTransactionAdapter.fromJson(item.itemJson)?.let { repository.insertSavingsTransaction(it) }
                }
            }
            repository.deleteTrashItemById(item.id)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
        }
    }
    
    fun permanentDeleteTrashItem(id: Int) {
        viewModelScope.launch {
            repository.deleteTrashItemById(id)
        }
    }
    
    fun cleanUpOldTrash() {
        viewModelScope.launch {
            // 30 days ago
            val threshold = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
            repository.deleteOldTrashItems(threshold)
        }
    }
"""

content = content.replace("private val backupAdapter = moshi.adapter(FinanceBackup::class.java)", "private val backupAdapter = moshi.adapter(FinanceBackup::class.java)\n" + adapters_code)

# Modify delete functions
content = content.replace(
"""    fun deletePerson(id: Int) {
        viewModelScope.launch {
            repository.deletePerson(id)""",
"""    fun deletePerson(id: Int) {
        viewModelScope.launch {
            val p = repository.getPersonById(id)
            if (p != null) {
                repository.insertTrashItem(com.example.data.TrashItem(originalId = id, itemType = "PERSON", itemJson = personAdapter.toJson(p)))
            }
            repository.deletePerson(id)"""
)

content = content.replace(
"""    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)""",
"""    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            val t = repository.getTransactionById(id)
            if (t != null) {
                repository.insertTrashItem(com.example.data.TrashItem(originalId = id, itemType = "TRANSACTION", itemJson = transactionAdapter.toJson(t)))
            }
            repository.deleteTransaction(id)"""
)

content = content.replace(
"""    fun deleteSavingsGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)""",
"""    fun deleteSavingsGoal(id: Int) {
        viewModelScope.launch {
            val g = repository.getSavingsGoalById(id)
            if (g != null) {
                repository.insertTrashItem(com.example.data.TrashItem(originalId = id, itemType = "SAVINGS_GOAL", itemJson = savingsGoalAdapter.toJson(g)))
            }
            repository.deleteSavingsGoal(id)"""
)

content = content.replace(
"""    fun deleteSavingsTransaction(tx: SavingsTransaction) {
        viewModelScope.launch {
            repository.deleteSavingsTransaction(tx.id)""",
"""    fun deleteSavingsTransaction(tx: SavingsTransaction) {
        viewModelScope.launch {
            repository.insertTrashItem(com.example.data.TrashItem(originalId = tx.id, itemType = "SAVINGS_TRANSACTION", itemJson = savingsTransactionAdapter.toJson(tx)))
            repository.deleteSavingsTransaction(tx.id)"""
)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
