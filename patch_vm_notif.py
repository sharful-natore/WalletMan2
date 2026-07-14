import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

def add_notif(func_name, msg_en, msg_bn, is_success=True):
    global content
    # Find the function and its closing brace of the launch block
    pattern = rf'fun {func_name}\(.*?\)\s*{{'
    match = re.search(pattern, content, re.DOTALL)
    if match:
        start_pos = match.end()
        # Find the matching closing brace for the function
        brace_count = 1
        pos = start_pos
        while brace_count > 0 and pos < len(content):
            if content[pos] == '{':
                brace_count += 1
            elif content[pos] == '}':
                brace_count -= 1
            pos += 1
        
        # We want to insert BEFORE the last closing brace
        insert_pos = pos - 1
        notif_call = f'\n            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "{msg_bn}" else "{msg_en}", isSuccess = {str(is_success).lower()}, type = "{"SUCCESS" if is_success else "ERROR"}")'
        content = content[:insert_pos] + notif_call + content[insert_pos:]

# List of functions and their messages
actions = [
    ("addPerson", "Person added successfully", "ব্যক্তি সফলভাবে যুক্ত করা হয়েছে"),
    ("updatePerson", "Person updated successfully", "ব্যক্তি সফলভাবে আপডেট করা হয়েছে"),
    ("deletePerson", "Person deleted", "ব্যক্তি মুছে ফেলা হয়েছে"),
    ("addTransaction", "Transaction saved", "লেনদেন সফলভাবে সংরক্ষণ করা হয়েছে"),
    ("updateTransaction", "Transaction updated", "লেনদেন আপডেট করা হয়েছে"),
    ("deleteTransaction", "Transaction deleted", "লেনদেন মুছে ফেলা হয়েছে"),
    ("addSavingsGoal", "Savings goal created", "সঞ্চয় লক্ষ্য তৈরি করা হয়েছে"),
    ("updateSavingsGoal", "Savings goal updated", "সঞ্চয় লক্ষ্য আপডেট করা হয়েছে"),
    ("deleteSavingsGoal", "Savings goal deleted", "সঞ্চয় লক্ষ্য মুছে ফেলা হয়েছে"),
    ("addSavingsContribution", "Contribution added", "সঞ্চয় যুক্ত করা হয়েছে"),
    ("updateSavingsTransaction", "Savings transaction updated", "সঞ্চয় লেনদেন আপডেট করা হয়েছে"),
    ("deleteSavingsTransaction", "Savings transaction deleted", "সঞ্চয় লেনদেন মুছে ফেলা হয়েছে"),
    ("restoreTrashItem", "Item restored successfully", "আইটেম সফলভাবে রিস্টোর করা হয়েছে"),
    ("permanentDeleteTrashItem", "Item permanently deleted", "আইটেম স্থায়ীভাবে মুছে ফেলা হয়েছে")
]

for func, en, bn in actions:
    add_notif(func, en, bn)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
