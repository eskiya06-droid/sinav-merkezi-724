import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamScreens.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Fix AuthScreen Layout and Logo
# Remove duplicate imePadding()
content = content.replace(".windowInsetsPadding(WindowInsets.safeDrawing)\n            .imePadding()", ".windowInsetsPadding(WindowInsets.safeDrawing)")

# Replace Image logo with a text logo for now, as the image is not uploaded
auth_hero_pattern = re.compile(r'                Image\(\s*painter = painterResource\(id = R\.drawable\.kitap724_logo\),[\s\S]*?modifier = Modifier\.height\(70\.dp\)\.fillMaxWidth\(\)\.padding\(horizontal = 32\.dp\)\n                \)')
text_logo = """                Text(
                    text = "KİTAP 7/24",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )"""
content = auth_hero_pattern.sub(text_logo, content)

# 2. Fix ProfileScreen Bottom Cutoff
# Add a spacer at the end of ProfileScreen Column
profile_end_pattern = re.compile(r'            Text\("Çıkış Yap", fontSize = 16\.sp, fontWeight = FontWeight\.Bold\)\n        \}\n    \}\n\}')
profile_end_replacement = """            Text("Çıkış Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}"""
content = profile_end_pattern.sub(profile_end_replacement, content)


# 3. Fix TestConfigScreen Colors
# The Unselected background is BrandBackground, Text is TextLight
# Selected background is BrandPrimary, Text is Color.White
# We need to ensure the selected color in FlowRow for Lessons and Topics is readable.
test_config_topics_pattern = re.compile(r'Text\(topic, color = if \(isSel\) Color\.White else TextLight, fontSize = 12\.sp, fontWeight = FontWeight\.Bold\)')
# Wait, let's check what the current color code is for TestConfigScreen.
# In TestConfigScreen, Topics are:
# .background(if (isSel) BrandPrimaryLight else Color(0xFF131A30))
# Text(topic, color = if (isSel) BrandSurface else TextMuted ... )
# That's why it's unreadable! BrandPrimaryLight is light blue, BrandSurface is white. White on Light Blue = invisible.
# Color(0xFF131A30) is dark blue, TextMuted is gray. Dark on Dark = unreadable.
# Let's fix TestConfigScreen entirely by replacing the Box modifiers and Text colors for both Lesson and Topic loops.

def fix_test_config_lesson(match):
    return match.group(0).replace("BrandPrimaryLight.copy(alpha = 0.3f)", "BrandPrimary.copy(alpha = 0.1f)").replace("BrandSurface", "BrandBackground")

def fix_test_config_topic(match):
    return match.group(0).replace("if (isSel) BrandPrimaryLight else Color(0xFF131A30)", "if (isSel) BrandPrimary else BrandBackground").replace("if (isSel) BrandSurface else TextMuted", "if (isSel) Color.White else TextLight")

content = re.sub(r'\.background\(if \(isSel\) BrandPrimaryLight else Color\(0xFF131A30\)\)', ".background(if (isSel) BrandPrimary else BrandBackground)", content)
content = re.sub(r'Text\(topic, color = if \(isSel\) BrandSurface else TextMuted', "Text(topic, color = if (isSel) Color.White else TextLight", content)


# 4. Fix ActiveExamScreen Question Text Color and Bottom Padding
# Replace color = Color.White with color = TextLight for question text
question_text_pattern = re.compile(r'text = currentQuestion\.questionText,\s*color = Color\.White,\s*fontSize = 16\.sp,')
content = question_text_pattern.sub(r'text = currentQuestion.questionText,\n                    color = TextLight,\n                    fontSize = 16.sp,', content)

# Also fix the Lesson/Topic indicator background to not clash
indicator_pattern = re.compile(r'\.background\(BrandPrimary\.copy\(alpha = 0\.12f\)\)')
content = indicator_pattern.sub(r'.background(BrandPrimary.copy(alpha = 0.15f))', content)

# Add spacer at the end of the ActiveExamScreen Scrollable Column to prevent optical form from obscuring options
options_end_pattern = re.compile(r'                            \)\n                        \}\n                    \}\n                \}\n            \}\n\n            // Virtual Optical Form Sheet')
options_end_replacement = """                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Virtual Optical Form Sheet"""
content = options_end_pattern.sub(options_end_replacement, content)

# Write back
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("UI fixes applied to ExamScreens.kt")
