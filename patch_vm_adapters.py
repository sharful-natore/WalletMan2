with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

adapters = """
    private val personWithTxAdapter = moshi.adapter(com.example.data.PersonWithTransactions::class.java)
    private val goalWithTxAdapter = moshi.adapter(com.example.data.GoalWithTransactions::class.java)
    private val deletedBackupAdapter = moshi.adapter(com.example.data.DeletedGDriveBackup::class.java)
"""

content = content.replace(
    'private val savingsTransactionAdapter = moshi.adapter(SavingsTransaction::class.java)',
    'private val savingsTransactionAdapter = moshi.adapter(SavingsTransaction::class.java)\n' + adapters
)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
