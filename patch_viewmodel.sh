sed -i 's/repository.restoreBackupData(backupData)/restoreFullBackup(backupData)/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i 's/repository.restoreBackupData(remoteData)/restoreFullBackup(remoteData)/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i 's/repository.restoreBackupData(dataToRestore)/restoreFullBackup(dataToRestore)/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i 's/repository.restoreBackupData(FinanceBackup(emptyList(), emptyList(), emptyList(), emptyList()))/restoreFullBackup(FinanceBackup(emptyList(), emptyList(), emptyList(), emptyList()))/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
