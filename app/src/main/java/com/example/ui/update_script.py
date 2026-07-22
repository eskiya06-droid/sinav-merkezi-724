import sys
import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamScreens.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Define the replacement AuthScreen code
replacement = """// --- 4. User Auth Screen ---
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onRegister: (String, String, String, String, String) -> Unit,
    onLogin: (String, String) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(false) } // Default to Kayıt Ol
    var name by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedExam by remember { mutableStateOf("YKS (TYT-AYT)") }
    var selectedField by remember { mutableStateOf("Sayısal") }

    val exams = listOf("YKS (TYT-AYT)", "LGS", "KPSS", "DGS")
    val fields = listOf("Sayısal", "Sözel", "Eşit Ağırlık", "Genel")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Hero Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandPrimary, BrandPrimary.copy(alpha = 0.9f))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Logo",
                    tint = BrandSurface,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "KİTAP",
                        color = BrandSurface,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "7/24",
                        color = BrandSecondary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Dijital Sınav Ortağınız",
                    color = BrandSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Form Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-32).dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Auth Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        
                        // Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandBackground)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isLoginMode) BrandSurface else Color.Transparent)
                                    .clickable { isLoginMode = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Kayıt Ol", color = if (!isLoginMode) BrandPrimary else TextMuted, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLoginMode) BrandSurface else Color.Transparent)
                                    .clickable { isLoginMode = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Giriş Yap", color = if (isLoginMode) BrandPrimary else TextMuted, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (!isLoginMode) {
                            Text(text = "Adınız Soyadınız", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("Örn: Ahmet Yılmaz", color = TextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, unfocusedBorderColor = BrandBorder, focusedTextColor = TextLight, unfocusedTextColor = TextLight, cursorColor = BrandPrimary),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(text = "Kullanıcı Adı / E-posta / Telefon", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            placeholder = { Text("Giriş bilginizi yazın", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, unfocusedBorderColor = BrandBorder, focusedTextColor = TextLight, unfocusedTextColor = TextLight, cursorColor = BrandPrimary),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Şifre", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = TextMuted) },
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(icon, "Görünürlük", tint = TextMuted)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, unfocusedBorderColor = BrandBorder, focusedTextColor = TextLight, unfocusedTextColor = TextLight, cursorColor = BrandPrimary),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!isLoginMode) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(text = "Hedeflediğiniz Sınav", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                exams.forEach { exam ->
                                    val isSelected = selectedExam == exam
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) BrandPrimary else BrandBackground)
                                            .border(1.dp, if (isSelected) BrandPrimary else BrandBorder, RoundedCornerShape(12.dp))
                                            .clickable { selectedExam = exam }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = exam, color = if (isSelected) BrandSurface else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = "Alanınız / Branşınız", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                fields.forEach { field ->
                                    val isSelected = selectedField == field
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) BrandPrimary else BrandBackground)
                                            .border(1.dp, if (isSelected) BrandPrimary else BrandBorder, RoundedCornerShape(12.dp))
                                            .clickable { selectedField = field }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = field, color = if (isSelected) BrandSurface else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (isLoginMode) {
                            if (identifier.isNotEmpty() && password.isNotEmpty()) onLogin(identifier, password)
                        } else {
                            if (name.isNotEmpty() && identifier.isNotEmpty() && password.isNotEmpty()) onRegister(name, identifier, password, selectedExam, selectedField)
                        }
                    },
                    enabled = if (isLoginMode) identifier.isNotEmpty() && password.isNotEmpty() else name.isNotEmpty() && identifier.isNotEmpty() && password.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandSecondary,
                        contentColor = BrandSurface,
                        disabledContainerColor = BrandBorder,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = if (isLoginMode) "Giriş Yap" else "Kayıt Ol ve Başla",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}"""

# Use regex to replace the old block
pattern = re.compile(r"// --- 4\. User Onboarding Screen ---.*?// --- 5\. Student Dashboard Screen ---", re.DOTALL)
new_content = pattern.sub(replacement + "\n\n// --- 5. Student Dashboard Screen ---", content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("SUCCESS")
