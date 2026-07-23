with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

old_gdrive = """    fun deleteGoogleDriveFile(context: Context, fileId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                val request = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId")
                    .delete()
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val response = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(request).execute()
                    }
                }

                if (response.isSuccessful) {
                    listGoogleDriveFiles(context)
                    onSuccess()
                } else {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("Failed to delete file: $errBody")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }"""

new_gdrive = """    fun deleteGoogleDriveFile(context: Context, fileId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                // First, fetch the file metadata to get the name
                val metaRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId?fields=name")
                    .header("Authorization", "Bearer $accessToken")
                    .get()
                    .build()
                    
                var fileName = "Deleted Backup"
                kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        try {
                            val metaResponse = client.newCall(metaRequest).execute()
                            if (metaResponse.isSuccessful) {
                                val metaJson = org.json.JSONObject(metaResponse.body?.string() ?: "{}")
                                fileName = metaJson.optString("name", "Deleted Backup")
                            }
                        } catch (e: Exception) { /* ignore */ }
                    }
                }

                // Fetch file content
                val getRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                    .header("Authorization", "Bearer $accessToken")
                    .get()
                    .build()

                var backupJson = ""
                kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        try {
                            val getResponse = client.newCall(getRequest).execute()
                            if (getResponse.isSuccessful) {
                                backupJson = getResponse.body?.string() ?: ""
                            }
                        } catch(e: Exception) { /* ignore */ }
                    }
                }

                if (backupJson.isNotEmpty()) {
                    val deletedBackup = com.example.data.DeletedGDriveBackup(fileId, fileName, backupJson)
                    repository.insertTrashItem(com.example.data.TrashItem(
                        originalId = 0,
                        itemType = "GDRIVE_BACKUP",
                        itemJson = deletedBackupAdapter.toJson(deletedBackup)
                    ))
                }

                // Now actually delete from Google Drive
                val request = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId")
                    .delete()
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val response = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(request).execute()
                    }
                }

                if (response.isSuccessful) {
                    listGoogleDriveFiles(context)
                    onSuccess()
                } else {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("Failed to delete file: $errBody")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }"""

content = content.replace(old_gdrive, new_gdrive)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
