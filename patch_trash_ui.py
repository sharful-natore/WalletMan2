with open('app/src/main/java/com/example/ui/screens/BackupRestoreDialogs.kt', 'r') as f:
    content = f.read()

title_logic = """                            val daysLeft = maxOf(0L, 30L - ((System.currentTimeMillis() - item.deletedAt) / (24L*60L*60L*1000L)))
                            
                            val itemTitle = when(item.itemType) {
                                "PERSON_WITH_TXS" -> if (language == AppLanguage.BN) "ব্যক্তি ও লেনদেন" else "Person & Transactions"
                                "PERSON" -> if (language == AppLanguage.BN) "ব্যক্তি" else "Person"
                                "TRANSACTION" -> if (language == AppLanguage.BN) "লেনদেন" else "Transaction"
                                "SAVINGS_GOAL" -> if (language == AppLanguage.BN) "কার্ড" else "Card"
                                "SAVINGS_GOAL_WITH_TXS" -> if (language == AppLanguage.BN) "কার্ড ও লেনদেন" else "Card & Transactions"
                                "SAVINGS_TRANSACTION" -> if (language == AppLanguage.BN) "কার্ড লেনদেন" else "Card Transaction"
                                "GDRIVE_BACKUP" -> if (language == AppLanguage.BN) "গুগল ড্রাইভ ব্যাকআপ" else "Google Drive Backup"
                                else -> item.itemType
                            }"""

content = content.replace(
'''                            val daysLeft = maxOf(0L, 30L - ((System.currentTimeMillis() - item.deletedAt) / (24L*60L*60L*1000L)))''',
title_logic
)

content = content.replace(
'''                                        Text(
                                            text = "${item.itemType} #${item.originalId}",''',
'''                                        Text(
                                            text = if (item.itemType == "GDRIVE_BACKUP") itemTitle else "$itemTitle #${item.originalId}",'''
)

with open('app/src/main/java/com/example/ui/screens/BackupRestoreDialogs.kt', 'w') as f:
    f.write(content)
