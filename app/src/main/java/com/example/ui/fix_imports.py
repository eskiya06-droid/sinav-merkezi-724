import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamScreens.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

imports_to_add = [
    "import androidx.compose.foundation.layout.ExperimentalLayoutApi",
    "import androidx.compose.ui.unit.Dp"
]

for imp in imports_to_add:
    if imp not in content:
        content = content.replace("import androidx.compose.ui.unit.sp", f"import androidx.compose.ui.unit.sp\n{imp}")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("ExamScreens.kt imports fixed")
