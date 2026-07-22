import os
import base64

drawable_dir = r"c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\res\drawable"
os.makedirs(drawable_dir, exist_ok=True)

# 1x1 transparent PNG base64
png_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="

with open(os.path.join(drawable_dir, "kitap724_logo.png"), "wb") as f:
    f.write(base64.b64decode(png_b64))

print("Dummy logo created")
