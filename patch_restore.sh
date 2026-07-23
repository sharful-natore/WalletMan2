sed -i '/private fun saveImageToInternalStorage/i \
    private suspend fun restoreFullBackup(backup: FinanceBackup) {\
        repository.restoreBackupData(backup)\
        if (backup.profileName.isNotBlank() || backup.profileEmail.isNotBlank()) {\
            updateProfile(\
                name = backup.profileName.ifBlank { _profileName.value },\
                email = backup.profileEmail.ifBlank { _profileEmail.value },\
                photoUri = backup.profilePhotoUri ?: _profilePhotoUri.value,\
                phone = backup.profilePhone.ifBlank { _profilePhone.value },\
                social = backup.profileSocial.ifBlank { _profileSocial.value },\
                address = backup.profileAddress.ifBlank { _profileAddress.value }\
            )\
        }\
        com.example.widget.updateAllWidgets(getApplication())\
    }\
' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
