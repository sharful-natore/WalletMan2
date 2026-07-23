with open('app/src/main/java/com/example/data/Models.kt', 'r') as f:
    content = f.read()

trash_model = """
@Entity(tableName = "trash_items")
@JsonClass(generateAdapter = true)
data class TrashItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalId: Int,
    val itemType: String, // "TRANSACTION", "PERSON", "SAVINGS_GOAL", "SAVINGS_TRANSACTION"
    val itemJson: String,
    val deletedAt: Long = System.currentTimeMillis()
)
"""
content = content + trash_model

with open('app/src/main/java/com/example/data/Models.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'r') as f:
    db = f.read()
db = db.replace('entities = [Person::class, Transaction::class, SavingsGoal::class, SavingsTransaction::class]', 'entities = [Person::class, Transaction::class, SavingsGoal::class, SavingsTransaction::class, TrashItem::class]')
with open('app/src/main/java/com/example/data/AppDatabase.kt', 'w') as f:
    f.write(db)
