with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

repo_additions = """
    // Trash
    val allTrashItems: Flow<List<TrashItem>> = financeDao.getAllTrashItems()
    suspend fun insertTrashItem(item: TrashItem) = financeDao.insertTrashItem(item)
    suspend fun deleteTrashItemById(id: Int) = financeDao.deleteTrashItemById(id)
    suspend fun deleteOldTrashItems(threshold: Long) = financeDao.deleteOldTrashItems(threshold)
    suspend fun getTrashItemById(id: Int): TrashItem? = financeDao.getTrashItemById(id)
}
"""

content = content.replace("    }\n}", "    }\n" + repo_additions)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
