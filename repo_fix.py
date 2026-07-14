with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

repo_additions = """
    suspend fun getTransactionById(id: Int) = financeDao.getTransactionById(id)
    suspend fun getSavingsGoalById(id: Int) = financeDao.getSavingsGoalById(id)
    suspend fun getSavingsTransactionById(id: Int) = financeDao.getSavingsTransactionById(id)
"""

content = content.replace("    suspend fun getTrashItemById(id: Int): TrashItem? = financeDao.getTrashItemById(id)", "    suspend fun getTrashItemById(id: Int): TrashItem? = financeDao.getTrashItemById(id)\n" + repo_additions)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
