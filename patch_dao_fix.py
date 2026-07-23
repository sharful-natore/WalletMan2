with open('app/src/main/java/com/example/data/FinanceDao.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '@Query("DELETE FROM transactions WHERE id = :id")\n    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")',
    '@Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")'
)

content = content.replace(
    '@Query("DELETE FROM savings_goals WHERE id = :id")\n    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")',
    '@Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")'
)

content = content.replace(
    '@Query("DELETE FROM savings_transactions WHERE id = :id")\n    @Query("SELECT * FROM savings_transactions WHERE id = :id LIMIT 1")',
    '@Query("SELECT * FROM savings_transactions WHERE id = :id LIMIT 1")'
)

with open('app/src/main/java/com/example/data/FinanceDao.kt', 'w') as f:
    f.write(content)
