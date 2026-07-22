import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamScreens.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the compose Paint import
content = content.replace("import androidx.compose.ui.graphics.Paint\n", "")

# Fix the Paint instantiation in coloredGlow
content = content.replace("val paint = Paint()", "val paint = androidx.compose.ui.graphics.Paint()")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Paint conflict fixed")
