import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\MainActivity.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add SmartToy import if not present
if "import androidx.compose.material.icons.filled.SmartToy" not in content:
    content = content.replace(
        "import androidx.compose.material.icons.filled.QuestionAnswer",
        "import androidx.compose.material.icons.filled.QuestionAnswer\nimport androidx.compose.material.icons.filled.SmartToy"
    )

new_nav_code = """                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 16.dp
                ) {
                    val indicatorBlue = Color(0xFFE3F2FD) // Very light blue pill
                    val selectedIconTint = Color(0xFF1E88E5)
                    val unselectedIconTint = Color(0xFF9E9E9E)

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.TEST_CONFIG || currentScreen == AppScreen.EXAM_RESULT,
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Panel") },
                        label = { Text("Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedIconTint,
                            selectedTextColor = selectedIconTint,
                            unselectedIconColor = unselectedIconTint,
                            unselectedTextColor = unselectedIconTint,
                            indicatorColor = indicatorBlue
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.AI_ANALYSIS,
                        onClick = { viewModel.navigateTo(AppScreen.AI_ANALYSIS) },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = "AI Karne") },
                        label = { Text("AI Karne", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedIconTint,
                            selectedTextColor = selectedIconTint,
                            unselectedIconColor = unselectedIconTint,
                            unselectedTextColor = unselectedIconTint,
                            indicatorColor = indicatorBlue
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.MISTAKE_BOX,
                        onClick = { viewModel.navigateTo(AppScreen.MISTAKE_BOX) },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Yanlışlarım") },
                        label = { Text("Yanlışlarım", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedIconTint,
                            selectedTextColor = selectedIconTint,
                            unselectedIconColor = unselectedIconTint,
                            unselectedTextColor = unselectedIconTint,
                            indicatorColor = indicatorBlue
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.AI_TEACHER,
                        onClick = { viewModel.navigateTo(AppScreen.AI_TEACHER) },
                        icon = { 
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Brush.radialGradient(listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = "7/24 AI", tint = Color(0xFF006064), modifier = Modifier.size(24.dp))
                            }
                        },
                        label = { Text("7/24 AI", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF006064),
                            selectedTextColor = Color(0xFF006064),
                            unselectedIconColor = unselectedIconTint,
                            unselectedTextColor = unselectedIconTint,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PROFILE,
                        onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                        label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedIconTint,
                            selectedTextColor = selectedIconTint,
                            unselectedIconColor = unselectedIconTint,
                            unselectedTextColor = unselectedIconTint,
                            indicatorColor = indicatorBlue
                        )
                    )
                }"""

pattern = re.compile(r"                NavigationBar\([\s\S]*?                }", re.MULTILINE)
new_content = pattern.sub(new_nav_code, content, count=1)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("SUCCESS")
