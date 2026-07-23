with open('app/src/main/java/com/example/data/FinanceDao.kt', 'r') as f:
    content = f.read()

dao_additions = """
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
}
"""

content = content.replace("}", dao_additions)

with open('app/src/main/java/com/example/data/FinanceDao.kt', 'w') as f:
    f.write(content)
