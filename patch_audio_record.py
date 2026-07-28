import re

with open('app/src/main/java/com/example/ui/screens/OfflineVoiceHelper.kt', 'r') as f:
    content = f.read()

replacement = """                    val numBytes = android.media.AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
                    if (numBytes <= 0) throw Exception("AudioRecord unsupported")
                    
                    val record = android.media.AudioRecord(
                        android.media.MediaRecorder.AudioSource.MIC,
                        sampleRateInHz,
                        channelConfig,
                        audioFormat,
                        numBytes * 2
                    )
                    if (record.state != android.media.AudioRecord.STATE_INITIALIZED) {
                        throw Exception("AudioRecord failed to initialize")
                    }"""

content = content.replace("val numBytes = android.media.AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)\n                    \n                    val record = android.media.AudioRecord(\n                        android.media.MediaRecorder.AudioSource.MIC,\n                        sampleRateInHz,\n                        channelConfig,\n                        audioFormat,\n                        numBytes * 2\n                    )", replacement)

with open('app/src/main/java/com/example/ui/screens/OfflineVoiceHelper.kt', 'w') as f:
    f.write(content)
