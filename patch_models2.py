with open('app/src/main/java/com/example/data/Models.kt', 'r') as f:
    content = f.read()

classes = """
@JsonClass(generateAdapter = true)
data class PersonWithTransactions(
    val person: Person,
    val transactions: List<Transaction>
)

@JsonClass(generateAdapter = true)
data class GoalWithTransactions(
    val goal: SavingsGoal,
    val transactions: List<SavingsTransaction>
)

@JsonClass(generateAdapter = true)
data class DeletedGDriveBackup(
    val fileId: String,
    val fileName: String,
    val backupJson: String
)
"""
content = content + classes

with open('app/src/main/java/com/example/data/Models.kt', 'w') as f:
    f.write(content)
