import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\MainActivity.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

imports = [
    "import androidx.compose.ui.draw.clip",
    "import androidx.compose.ui.graphics.Brush"
]

for imp in imports:
    if imp not in content:
        content = content.replace("import androidx.compose.ui.Modifier", f"import androidx.compose.ui.Modifier\n{imp}")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("MainActivity.kt imports fixed")
