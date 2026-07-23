with open('app/src/main/java/com/example/ui/viewmodel/UpdateManager.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val latestVersion: String = ""',
    'val latestVersion: String = "",\n    val updateDetails: String = ""'
)

content = content.replace(
    'val updateUrl = remoteConfig.getString("update_url")',
    'val updateUrl = remoteConfig.getString("update_url")\n                val updateDetails = remoteConfig.getString("Update_Details")'
)

content = content.replace(
    'updateUrl = updateUrl,',
    'updateUrl = updateUrl,\n                    updateDetails = updateDetails,'
)

with open('app/src/main/java/com/example/ui/viewmodel/UpdateManager.kt', 'w') as f:
    f.write(content)
