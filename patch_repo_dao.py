with open('app/src/main/java/com/example/data/FinanceDao.kt', 'r') as f:
    content = f.read()

dao_additions = """
    @Query("SELECT * FROM transactions WHERE personId = :personId")
    suspend fun getTransactionsByPersonList(personId: Int): List<Transaction>

    @Query("SELECT * FROM savings_transactions WHERE goalId = :goalId")
    suspend fun getSavingsTransactionsByGoalList(goalId: Int): List<SavingsTransaction>
"""

content = content.replace(
    'suspend fun getSavingsTransactionById(id: Int): SavingsTransaction?',
    'suspend fun getSavingsTransactionById(id: Int): SavingsTransaction?\n' + dao_additions
)

with open('app/src/main/java/com/example/data/FinanceDao.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

repo_additions = """
    suspend fun getTransactionsByPersonList(personId: Int) = financeDao.getTransactionsByPersonList(personId)
    suspend fun getSavingsTransactionsByGoalList(goalId: Int) = financeDao.getSavingsTransactionsByGoalList(goalId)
"""

content = content.replace(
    'suspend fun getSavingsTransactionById(id: Int) = financeDao.getSavingsTransactionById(id)',
    'suspend fun getSavingsTransactionById(id: Int) = financeDao.getSavingsTransactionById(id)\n' + repo_additions
)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
