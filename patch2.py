import re

with open('app/src/main/java/com/example/ui/screens/OfflineVoiceHelper.kt', 'r') as f:
    content = f.read()

with open('replacement.txt', 'r') as f:
    rep = f.read()

# Replace between "@Composable\nfun VoskSpeechInputDialog(" and "    Dialog("
pattern = re.compile(r'@Composable\s*fun VoskSpeechInputDialog.*?Dialog\(', re.DOTALL)
content = pattern.sub(rep + '\n    Dialog(', content)

with open('app/src/main/java/com/example/ui/screens/OfflineVoiceHelper.kt', 'w') as f:
    f.write(content)
