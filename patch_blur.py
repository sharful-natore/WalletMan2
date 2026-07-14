with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

import re

target = r"""                val bottomBarGradient = if \(isDarkTheme\) \{
                    Brush\.linearGradient\(
                        colors = listOf\(
                            Color\(0xCE141724\), // Translucent surface dark
                            Color\(0xB26F7BF7\), // Translucent sleek indigo
                            Color\(0xCE0B0D14\)  // Translucent obsidian
                        \)
                    \)
                \} else \{
                    Brush\.linearGradient\(
                        colors = listOf\(
                            Color\(0xD9FFFFFF\), // Translucent pure white
                            Color\(0xB238BDF8\), // Translucent sky blue
                            Color\(0xD9F1F5F9\)  // Translucent light gray
                        \)
                    \)
                \}"""

repl = """                val bottomBarGradient = Brush.linearGradient(
                    colors = GradientsList[0].map { it.copy(alpha = 0.85f) }
                )"""

content = re.sub(target, repl, content)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(content)
