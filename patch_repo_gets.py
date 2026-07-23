with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'suspend fun deleteTransaction(id: Int) = financeDao.deleteTransactionById(id)',
    'suspend fun getTransactionById(id: Int) = financeDao.getTransactionById(id)\n    suspend fun deleteTransaction(id: Int) = financeDao.deleteTransactionById(id)'
)

content = content.replace(
    'suspend fun getPersonById(id: Int): Person? = financeDao.getPersonById(id)',
    'suspend fun getPersonById(id: Int): Person? = financeDao.getPersonById(id)'
)

content = content.replace(
    'suspend fun deleteSavingsGoal(id: Int) = financeDao.deleteSavingsGoalById(id)',
    'suspend fun getSavingsGoalById(id: Int) = financeDao.getSavingsGoalById(id)\n    suspend fun deleteSavingsGoal(id: Int) = financeDao.deleteSavingsGoalById(id)'
)

content = content.replace(
    'suspend fun deleteSavingsTransaction(id: Int) = financeDao.deleteSavingsTransactionById(id)',
    'suspend fun getSavingsTransactionById(id: Int) = financeDao.getSavingsTransactionById(id)\n    suspend fun deleteSavingsTransaction(id: Int) = financeDao.deleteSavingsTransactionById(id)'
)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
