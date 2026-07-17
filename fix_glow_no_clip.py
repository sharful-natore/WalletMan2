import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

def remove_clip_glow(content):
    pattern1 = r"""                                        val innerClipRadius = .*?
                                        val innerClipPath = androidx\.compose\.ui\.graphics\.Path\(\)\.apply \{
                                            addOval\(androidx\.compose\.ui\.geometry\.Rect\(
                                                center\.x - innerClipRadius, center\.y - innerClipRadius,
                                                center\.x \+ innerClipRadius, center\.y \+ innerClipRadius
                                            \)\)
                                        \}
                                        drawContext\.canvas\.clipPath\(innerClipPath, androidx\.compose\.ui\.graphics\.ClipOp\.Difference\)"""

    content = re.sub(pattern1, "", content, flags=re.DOTALL)
    
    pattern2 = r"""                            val innerClipRadius = .*?
                            val innerClipPath = androidx\.compose\.ui\.graphics\.Path\(\)\.apply \{
                                addOval\(androidx\.compose\.ui\.geometry\.Rect\(
                                    center\.x - innerClipRadius, center\.y - innerClipRadius,
                                    center\.x \+ innerClipRadius, center\.y \+ innerClipRadius
                                \)\)
                            \}
                            drawContext\.canvas\.clipPath\(innerClipPath, androidx\.compose\.ui\.graphics\.ClipOp\.Difference\)"""
                            
    content = re.sub(pattern2, "", content, flags=re.DOTALL)

    pattern3 = r"""                        val innerClipRadius = .*?
                        val innerClipPath = androidx\.compose\.ui\.graphics\.Path\(\)\.apply \{
                            addOval\(androidx\.compose\.ui\.geometry\.Rect\(
                                center\.x - innerClipRadius, center\.y - innerClipRadius,
                                center\.x \+ innerClipRadius, center\.y \+ innerClipRadius
                            \)\)
                        \}
                        drawContext\.canvas\.clipPath\(innerClipPath, androidx\.compose\.ui\.graphics\.ClipOp\.Difference\)"""
    content = re.sub(pattern3, "", content, flags=re.DOTALL)

    pattern4 = r"""                    val innerClipRadius = .*?
                    val innerClipPath = androidx\.compose\.ui\.graphics\.Path\(\)\.apply \{
                        addOval\(androidx\.compose\.ui\.geometry\.Rect\(
                            center\.x - innerClipRadius, center\.y - innerClipRadius,
                            center\.x \+ innerClipRadius, center\.y \+ innerClipRadius
                        \)\)
                    \}
                    drawContext\.canvas\.clipPath\(innerClipPath, androidx\.compose\.ui\.graphics\.ClipOp\.Difference\)"""
    content = re.sub(pattern4, "", content, flags=re.DOTALL)

    return content

content = remove_clip_glow(content)

# Increase the shadow alpha to make it standard visible and adjust blur to a standard 12.dp
old_alpha = "alpha = 0.5f"
new_alpha = "alpha = 0.6f"
content = content.replace(old_alpha, new_alpha)

old_blur = "BlurMaskFilter(16.dp.toPx()"
new_blur = "BlurMaskFilter(12.dp.toPx()"
content = content.replace(old_blur, new_blur)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
