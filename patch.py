import re

with open('app/src/main/java/com/example/ui/screens/OfflineVoiceHelper.kt', 'r') as f:
    content = f.read()

# Replace the entire VoskSpeechInputDialog function
# It starts with "@Composable\nfun VoskSpeechInputDialog("
# And ends before "    Dialog(" or similar? Wait, the dialog is part of the function!
# My replacement only includes the logic part, I should include the Dialog UI!
