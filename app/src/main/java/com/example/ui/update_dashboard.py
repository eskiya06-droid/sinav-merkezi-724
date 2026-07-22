import sys
import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamScreens.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure imports are present (Logout, Person, Icons)
if "import androidx.compose.material.icons.filled.Logout" not in content:
    content = content.replace(
        "import androidx.compose.material.icons.filled.VisibilityOff",
        "import androidx.compose.material.icons.filled.VisibilityOff\nimport androidx.compose.material.icons.filled.Logout\nimport androidx.compose.material.icons.filled.Person"
    )

replacement = """// --- Profile Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: UserProfile,
    onSave: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedExam by remember { mutableStateOf(if (profile.targetExam == "Belirtilmedi") "YKS (TYT-AYT)" else profile.targetExam) }
    var selectedField by remember { mutableStateOf(if (profile.field == "Belirtilmedi") "Sayısal" else profile.field) }

    val exams = listOf("YKS (TYT-AYT)", "LGS", "KPSS", "DGS")
    val fields = listOf("Sayısal", "Sözel", "Eşit Ağırlık", "Genel")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Profilim",
            color = TextLight,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Hesap ve Sınav Ayarlarınızı Yönetin",
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // User Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(BrandPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = BrandPrimary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = profile.username, color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = profile.identifier, color = TextMuted, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Hedeflediğiniz Sınav", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exams.forEach { exam ->
                        val isSelected = selectedExam == exam
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandPrimary else BrandBackground)
                                .border(1.dp, if (isSelected) BrandPrimary else BrandBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedExam = exam }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = exam, color = if (isSelected) BrandSurface else TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Alanınız / Branşınız", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fields.forEach { field ->
                        val isSelected = selectedField == field
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandPrimary else BrandBackground)
                                .border(1.dp, if (isSelected) BrandPrimary else BrandBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedField = field }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = field, color = if (isSelected) BrandSurface else TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSave(selectedExam, selectedField) },
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = BrandSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Değişiklikleri Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WrongRed),
            border = BorderStroke(1.dp, WrongRed),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Çıkış Yap", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Çıkış Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- 5. Student Dashboard Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    profile: UserProfile,
    historyList: List<ExamHistory>,
    mistakeCount: Int,
    onNavigate: (AppScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Merhaba, ${profile.username}",
                        color = TextMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Çalışmaya Başla 👋",
                        color = TextLight,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                // Target Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${profile.targetExam} | ${profile.field}",
                        color = BrandPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Stats
            val totalSolved = historyList.size
            val totalCorrect = historyList.sumOf { it.correctCount }
            val totalWrong = historyList.sumOf { it.wrongCount }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), title = "Deneme", value = "$totalSolved", color = BrandPrimary)
                StatCard(modifier = Modifier.weight(1f), title = "Doğru", value = "$totalCorrect", color = CorrectGreen)
                StatCard(modifier = Modifier.weight(1f), title = "Hatalı", value = "$totalWrong", color = WrongRed)
                StatCard(modifier = Modifier.weight(1f), title = "Kutuda", value = "$mistakeCount", color = WarningOrange)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Grid
            Text(
                text = "Modüller",
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Konu Testi",
                    icon = Icons.Default.OfflineBolt,
                    color = BrandPrimary,
                    onClick = { onNavigate(AppScreen.TEST_CONFIG) }
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Tam Deneme",
                    icon = Icons.Default.Timer,
                    color = BrandSecondary,
                    onClick = { onNavigate(AppScreen.ACTIVE_EXAM) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "AI Karne",
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF673AB7),
                    onClick = { onNavigate(AppScreen.AI_ANALYSIS) }
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "7/24 Rehber",
                    icon = Icons.Default.QuestionAnswer,
                    color = Color(0xFF009688),
                    onClick = { onNavigate(AppScreen.AI_TEACHER) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() }.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}"""

# Use regex to replace the old Dashboard block up to the start of the next section
# The old block starts at "// --- 5. Student Dashboard Screen ---"
# And the next section is probably "// --- 6. Exam Configuration ---" or similar
# Let's check for // --- 6.
pattern = re.compile(r"// --- 5\. Student Dashboard Screen ---.*?// --- 6\.", re.DOTALL)
new_content = pattern.sub(replacement + "\n\n// --- 6.", content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("SUCCESS")
