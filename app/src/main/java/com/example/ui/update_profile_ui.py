import re

# 1. Update MainActivity.kt
main_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\MainActivity.kt'
with open(main_path, 'r', encoding='utf-8') as f:
    main_content = f.read()

new_profile_call = """                        ProfileScreen(
                            profile = p,
                            onSave = { exam, field, phone, address, email, birthDate, name -> 
                                viewModel.updateExamPreferences(exam, field, phone, address, email, birthDate, name) 
                            },
                            onLogout = { viewModel.logout() }
                        )"""

main_content = re.sub(r'                        ProfileScreen\([\s\S]*?onLogout = \{ viewModel\.logout\(\) \}\n                        \)', new_profile_call, main_content)

with open(main_path, 'w', encoding='utf-8') as f:
    f.write(main_content)


# 2. Update ExamScreens.kt AuthScreen Logo and add ProfileScreen
exam_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamScreens.kt'
with open(exam_path, 'r', encoding='utf-8') as f:
    exam_content = f.read()

# Add Image import if not present
if "import androidx.compose.foundation.Image" not in exam_content:
    exam_content = exam_content.replace(
        "import androidx.compose.foundation.background",
        "import androidx.compose.foundation.background\nimport androidx.compose.foundation.Image\nimport androidx.compose.ui.res.painterResource\nimport com.example.R"
    )

auth_hero_pattern = re.compile(r'                Icon\(\s*imageVector = Icons\.Default\.MenuBook,.*?Text\(\s*text = "Dijital Sınav Ortağınız",.*?modifier = Modifier\.padding\(top = 4\.dp\)\n                \)', re.DOTALL)

new_auth_hero = """                Image(
                    painter = painterResource(id = R.drawable.kitap724_logo),
                    contentDescription = "Kitap 7/24 Logo",
                    modifier = Modifier.height(70.dp).fillMaxWidth().padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dijital Sınav Ortağınız",
                    color = BrandSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "www.kitap724.com",
                    color = BrandSurface.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )"""

exam_content = auth_hero_pattern.sub(new_auth_hero, exam_content)


profile_screen_code = """

// --- Profile Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: com.example.data.UserProfile,
    onSave: (String, String, String, String, String, String, String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedExam by remember { mutableStateOf(if (profile.targetExam == "Belirtilmedi") "YKS (TYT-AYT)" else profile.targetExam) }
    var selectedField by remember { mutableStateOf(if (profile.field == "Belirtilmedi") "Sayısal" else profile.field) }
    
    var name by remember { mutableStateOf(profile.username) }
    var phone by remember { mutableStateOf(profile.phone) }
    var email by remember { mutableStateOf(profile.email) }
    var address by remember { mutableStateOf(profile.address) }
    var birthDate by remember { mutableStateOf(profile.birthDate) }

    val exams = listOf("YKS (TYT-AYT)", "LGS", "KPSS", "DGS")
    val fields = listOf("Sayısal", "Sözel", "Eşit Ağırlık", "Genel", "Dil")

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
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Kişisel Bilgiler", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, modifier = Modifier.padding(bottom = 16.dp))

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Ad Soyad") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("E-posta Adresi") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = birthDate, onValueChange = { birthDate = it },
                    label = { Text("Doğum Tarihi (GG/AA/YYYY)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Adres") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Sınav Hedefi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, modifier = Modifier.padding(bottom = 16.dp))

                Text("Hedeflediğiniz Sınav", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    exams.forEach { ex ->
                        val isSel = selectedExam == ex
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) BrandPrimary else BrandBackground)
                                .clickable { selectedExam = ex }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(ex, color = if (isSel) BrandSurface else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Alanınız / Branşınız", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fields.forEach { fld ->
                        val isSel = selectedField == fld
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) BrandPrimary else BrandBackground)
                                .clickable { selectedField = fld }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(fld, color = if (isSel) BrandSurface else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onSave(selectedExam, selectedField, phone, address, email, birthDate, name) },
            colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp)
        ) {
            Text("Bilgileri Güncelle", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WrongRed),
            border = BorderStroke(1.dp, WrongRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 32.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Çıkış", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Çıkış Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
"""

if "fun ProfileScreen(" not in exam_content:
    exam_content += profile_screen_code
else:
    # If it was there, replace it
    exam_content = re.sub(r'// --- Profile Screen ---\n.*?fun ProfileScreen.*?\}\n\}\n', profile_screen_code, exam_content, flags=re.DOTALL)


with open(exam_path, 'w', encoding='utf-8') as f:
    f.write(exam_content)

print("UI components updated")
