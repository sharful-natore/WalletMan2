import re

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

# Replace Toast.makeText(context, "message", ...).show() with viewModel.triggerCustomNotification("message")
# We also have Toast.makeText(context, if (...) "..." else "...", ...).show()
# And Toast.makeText(context, Translation.get(...), ...).show()

pattern = re.compile(r'Toast\.makeText\(\s*context\s*,\s*(.*?),\s*Toast\.LENGTH_(?:SHORT|LONG)\s*\)\.show\(\)')
def replacer(match):
    msg = match.group(1)
    is_success = 'true'
    type_str = '"INFO"'
    
    # Simple heuristics to determine if error
    if 'failed' in msg.lower() or 'error' in msg.lower() or 'ব্যর্থ' in msg or 'নেই' in msg or 'invalid' in msg.lower():
        is_success = 'false'
        type_str = '"ERROR"'
    elif 'successful' in msg.lower() or 'সফল' in msg:
        type_str = '"SUCCESS"'
        
    return f'viewModel.triggerCustomNotification({msg}, isSuccess = {is_success}, type = {type_str})'

new_content = pattern.sub(replacer, content)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(new_content)
