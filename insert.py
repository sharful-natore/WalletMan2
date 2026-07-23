import sys

with open("app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "fun downloadGoogleDriveFile" in line:
        new_lines.append("""
    fun deleteGoogleDriveFile(context: Context, fileId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                // Fetch metadata for file name
                val metaRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId?fields=name")
                    .header("Authorization", "Bearer $accessToken")
                    .get()
                    .build()
                
                var fileName = "Deleted Backup"
                var backupJson = ""

                kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        try {
                            val metaResponse = client.newCall(metaRequest).execute()
                            if (metaResponse.isSuccessful) {
                                val metaStr = metaResponse.body?.string() ?: "{}"
                                val metaJsonObj = org.json.JSONObject(metaStr)
                                fileName = metaJsonObj.optString("name", "Deleted Backup")
                            }
                            
                            val downloadRequest = Request.Builder()
                                .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                                .header("Authorization", "Bearer $accessToken")
                                .get()
                                .build()
                            val dlResponse = client.newCall(downloadRequest).execute()
                            if (dlResponse.isSuccessful) {
                                backupJson = dlResponse.body?.string() ?: ""
                            }
                        } catch (e: Exception) { e.printStackTrace() }
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
                    triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ফাইলটি ট্র্যাশে সরানো হয়েছে" else "File moved to trash", isSuccess = true, type = "SUCCESS")
                    onSuccess()
                } else {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("Failed to delete file: $errBody")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }
""")
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt", "w") as f:
    f.writelines(new_lines)
