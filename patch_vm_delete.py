with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# 1. Update deletePerson
new_delete_person = """    fun deletePerson(id: Int) {
        viewModelScope.launch {
            val p = repository.getPersonById(id)
            if (p != null) {
                val txs = repository.getTransactionsByPersonList(id)
                val pWithTx = com.example.data.PersonWithTransactions(p, txs)
                repository.insertTrashItem(com.example.data.TrashItem(
                    originalId = id, 
                    itemType = "PERSON_WITH_TXS", 
                    itemJson = personWithTxAdapter.toJson(pWithTx)
                ))
            }
            repository.deletePerson(id)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
        }
    }"""
content = content.replace(
'''    fun deletePerson(id: Int) {
        viewModelScope.launch {
            val p = repository.getPersonById(id)
            if (p != null) {
                repository.insertTrashItem(com.example.data.TrashItem(originalId = id, itemType = "PERSON", itemJson = personAdapter.toJson(p)))
            }
            repository.deletePerson(id)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
        }
    }''',
new_delete_person
)

# 2. Update deleteSavingsGoal
new_delete_goal = """    fun deleteSavingsGoal(id: Int) {
        viewModelScope.launch {
            val g = repository.getSavingsGoalById(id)
            if (g != null) {
                val txs = repository.getSavingsTransactionsByGoalList(id)
                val gWithTx = com.example.data.GoalWithTransactions(g, txs)
                repository.insertTrashItem(com.example.data.TrashItem(
                    originalId = id,
                    itemType = "SAVINGS_GOAL_WITH_TXS",
                    itemJson = goalWithTxAdapter.toJson(gWithTx)
                ))
            }
            repository.deleteSavingsGoal(id)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
        }
    }"""
content = content.replace(
'''    fun deleteSavingsGoal(id: Int) {
        viewModelScope.launch {
            val g = repository.getSavingsGoalById(id)
            if (g != null) {
                repository.insertTrashItem(com.example.data.TrashItem(originalId = id, itemType = "SAVINGS_GOAL", itemJson = savingsGoalAdapter.toJson(g)))
            }
            repository.deleteSavingsGoal(id)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
        }
    }''',
new_delete_goal
)

# 3. Update restoreTrashItem
new_restore = """    fun restoreTrashItem(item: com.example.data.TrashItem) {
        viewModelScope.launch {
            when (item.itemType) {
                "PERSON" -> {
                    personAdapter.fromJson(item.itemJson)?.let { repository.insertPerson(it) }
                }
                "PERSON_WITH_TXS" -> {
                    personWithTxAdapter.fromJson(item.itemJson)?.let { pWithTx ->
                        repository.insertPerson(pWithTx.person)
                        pWithTx.transactions.forEach { tx ->
                            repository.insertTransaction(tx)
                        }
                    }
                }
                "TRANSACTION" -> {
                    transactionAdapter.fromJson(item.itemJson)?.let { repository.insertTransaction(it) }
                }
                "SAVINGS_GOAL" -> {
                    savingsGoalAdapter.fromJson(item.itemJson)?.let { repository.insertSavingsGoal(it) }
                }
                "SAVINGS_GOAL_WITH_TXS" -> {
                    goalWithTxAdapter.fromJson(item.itemJson)?.let { gWithTx ->
                        repository.insertSavingsGoal(gWithTx.goal)
                        gWithTx.transactions.forEach { tx ->
                            repository.insertSavingsTransaction(tx)
                        }
                    }
                }
                "SAVINGS_TRANSACTION" -> {
                    savingsTransactionAdapter.fromJson(item.itemJson)?.let { repository.insertSavingsTransaction(it) }
                }
                "GDRIVE_BACKUP" -> {
                    // Restoring a GDrive backup: we can't easily upload it back without Context in this function, 
                    // so we will just write it to a local JSON file in Documents, or just notify user.
                    // Wait, we can apply it! Let's just not do anything here for GDrive, UI will handle it, or we apply it.
                }
            }
            if (item.itemType != "GDRIVE_BACKUP") {
                repository.deleteTrashItemById(item.id)
                com.example.widget.updateAllWidgets(getApplication())
                onLocalDatabaseChanged()
            }
        }
    }"""
content = content.replace(
'''    fun restoreTrashItem(item: com.example.data.TrashItem) {
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
    }''',
new_restore
)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
