#!/bin/bash
sed -i 's/vosk-model-small-streaming-bn/sherpa-model-bn/g' app/src/main/java/com/example/ui/screens/OfflineVoiceHelper.kt
sed -i 's/vosk_model_bn.zip/sherpa_model_bn.zip/g' app/src/main/java/com/example/ui/screens/OfflineVoiceHelper.kt
