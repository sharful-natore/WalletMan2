with open('app/src/main/java/com/example/data/FinanceDao.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'suspend fun deleteTransactionById(id: Int)',
    '@Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")\n    suspend fun getTransactionById(id: Int): Transaction?\n\n    @Query("DELETE FROM transactions WHERE id = :id")\n    suspend fun deleteTransactionById(id: Int)'
)

content = content.replace(
    'suspend fun deleteSavingsGoalById(id: Int)',
    '@Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")\n    suspend fun getSavingsGoalById(id: Int): SavingsGoal?\n\n    @Query("DELETE FROM savings_goals WHERE id = :id")\n    suspend fun deleteSavingsGoalById(id: Int)'
)

content = content.replace(
    'suspend fun deleteSavingsTransactionById(id: Int)',
    '@Query("SELECT * FROM savings_transactions WHERE id = :id LIMIT 1")\n    suspend fun getSavingsTransactionById(id: Int): SavingsTransaction?\n\n    @Query("DELETE FROM savings_transactions WHERE id = :id")\n    suspend fun deleteSavingsTransactionById(id: Int)'
)

with open('app/src/main/java/com/example/data/FinanceDao.kt', 'w') as f:
    f.write(content)
