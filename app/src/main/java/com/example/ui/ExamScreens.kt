package com.example.ui

import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ExamHistory
import com.example.data.SolvedQuestion
import com.example.data.UserProfile
import com.example.data.MistakeQuestion
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONObject

// --- 1. Geometric Blueprint Drawing Canvas ---
@Composable
fun DrawingCanvas(commandsJson: String?, modifier: Modifier = Modifier) {
    if (commandsJson.isNullOrEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3EDF7))
            .border(1.dp, BrandBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .drawBehind {
                // Draw cool blueprint grid
                val step = 25.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    drawLine(
                        color = Color(0xFFE5DDF5).copy(alpha = 0.8f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(
                        color = Color(0xFFE5DDF5).copy(alpha = 0.8f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Scale factor to map 0-200 coordinates to actual canvas width/height
            val scaleX = canvasWidth / 200f
            val scaleY = canvasHeight / 200f

            try {
                val json = JSONObject(commandsJson)
                val commands = json.optJSONArray("commands")
                if (commands != null) {
                    for (i in 0 until commands.length()) {
                        val cmd = commands.getJSONObject(i)
                        when (cmd.optString("type")) {
                            "line" -> {
                                val x1 = cmd.optDouble("x1", 0.0).toFloat() * scaleX
                                val y1 = cmd.optDouble("y1", 0.0).toFloat() * scaleY
                                val x2 = cmd.optDouble("x2", 0.0).toFloat() * scaleX
                                val y2 = cmd.optDouble("y2", 0.0).toFloat() * scaleY
                                drawLine(
                                    color = BrandPrimary,
                                    start = Offset(x1, y1),
                                    end = Offset(x2, y2),
                                    strokeWidth = 3f,
                                    cap = StrokeCap.Round
                                )
                            }
                            "circle" -> {
                                val cx = cmd.optDouble("cx", 100.0).toFloat() * scaleX
                                val cy = cmd.optDouble("cy", 100.0).toFloat() * scaleY
                                val r = cmd.optDouble("r", 30.0).toFloat() * scaleX
                                drawCircle(
                                    color = BrandSecondary,
                                    center = Offset(cx, cy),
                                    radius = r,
                                    style = Stroke(width = 3f)
                                )
                            }
                            "rect" -> {
                                val x = cmd.optDouble("x", 50.0).toFloat() * scaleX
                                val y = cmd.optDouble("y", 50.0).toFloat() * scaleY
                                val w = cmd.optDouble("w", 100.0).toFloat() * scaleX
                                val h = cmd.optDouble("h", 60.0).toFloat() * scaleY
                                drawRect(
                                    color = BrandPrimary,
                                    topLeft = Offset(x, y),
                                    size = Size(w, h),
                                    style = Stroke(width = 3f)
                                )
                            }
                            "triangle" -> {
                                val x1 = cmd.optDouble("x1", 100.0).toFloat() * scaleX
                                val y1 = cmd.optDouble("y1", 30.0).toFloat() * scaleY
                                val x2 = cmd.optDouble("x2", 40.0).toFloat() * scaleX
                                val y2 = cmd.optDouble("y2", 150.0).toFloat() * scaleY
                                val x3 = cmd.optDouble("x3", 160.0).toFloat() * scaleX
                                val y3 = cmd.optDouble("y3", 150.0).toFloat() * scaleY

                                val path = Path().apply {
                                    moveTo(x1, y1)
                                    lineTo(x2, y2)
                                    lineTo(x3, y3)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = BrandPrimary,
                                    style = Stroke(width = 3f)
                                )
                            }
                            "text" -> {
                                val text = cmd.optString("text", "")
                                val x = cmd.optDouble("x", 100.0).toFloat() * scaleX
                                val y = cmd.optDouble("y", 100.0).toFloat() * scaleY

                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawText(
                                        text,
                                        x,
                                        y,
                                        Paint().apply {
                                            color = android.graphics.Color.parseColor("#1D1B20")
                                            textSize = 28f
                                            isFakeBoldText = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // If parsing failed, show simple schematic text
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "[Görsel Şema Çizimi]",
                        canvasWidth / 2f - 80f,
                        canvasHeight / 2f,
                        Paint().apply {
                            color = android.graphics.Color.parseColor("#79747E")
                            textSize = 30f
                        }
                    )
                }
            }
        }
    }
}

// --- 2. Lightweight Markdown Custom Formatter ---
@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val lines = text.split("\n")
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("###") -> {
                    Text(
                        text = trimmed.removePrefix("###").trim(),
                        color = BrandPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("##") -> {
                    Text(
                        text = trimmed.removePrefix("##").trim(),
                        color = BrandPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )
                }
                trimmed.startsWith("#") -> {
                    Text(
                        text = trimmed.removePrefix("#").trim(),
                        color = TextLight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )
                }
                trimmed.startsWith("*") || trimmed.startsWith("-") -> {
                    val rawContent = if (trimmed.startsWith("*")) trimmed.removePrefix("*").trim() else trimmed.removePrefix("-").trim()
                    Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)) {
                        Text(text = "•", color = BrandPrimary, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                        FormattedLineText(rawContent)
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                else -> {
                    FormattedLineText(trimmed, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun FormattedLineText(rawText: String, modifier: Modifier = Modifier) {
    // Basic bold ** formatting extraction
    val parts = rawText.split("**")
    if (parts.size > 1) {
        Text(
            text = androidx.compose.ui.text.buildAnnotatedString {
                parts.forEachIndexed { index, part ->
                    if (index % 2 == 1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = BrandPrimary))
                        append(part)
                        pop()
                    } else {
                        append(part)
                    }
                }
            },
            color = TextLight,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = modifier
        )
    } else {
        Text(
            text = rawText,
            color = TextLight,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = modifier
        )
    }
}

// --- 3. Circular Stat Ring Component ---
@Composable
fun CircularStatRing(
    percentage: Float,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = BrandBorder.copy(alpha = 0.3f),
                style = Stroke(width = 8.dp.toPx())
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * percentage,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- 4. User Auth Screen ---
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onRegister: (String, String, String) -> Unit,
    onLogin: (String, String) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(false) } // Default to Kayıt Ol
    var name by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Hero Section (Fixed at top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandPrimary, BrandPrimary.copy(alpha = 0.9f))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-20).dp)) {
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

        // Scrollable Form Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(260.dp)) // Push the card down to overlap the hero section slightly
            
            // Auth Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
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
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isLoginMode) {
                        if (identifier.isNotEmpty() && password.isNotEmpty()) onLogin(identifier, password)
                    } else {
                        if (name.isNotEmpty() && identifier.isNotEmpty() && password.isNotEmpty()) onRegister(name, identifier, password)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = if (isLoginMode) "Giriş Yap" else "Kayıt Ol ve Başla",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- Profile Screen ---
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
}

// --- 6. Test Configuration Screen (Mod 1) ---
@Composable
fun TestConfigScreen(
    onBack: () -> Unit,
    onStartTest: (String, String, Int) -> Unit
) {
    var selectedLesson by remember { mutableStateOf("Matematik") }
    var selectedTopic by remember { mutableStateOf("Üslü Sayılar") }
    var selectedCount by remember { mutableIntStateOf(5) }

    val lessons = listOf("Matematik", "Türkçe", "Fen Bilimleri", "Sosyal Bilgiler")
    
    val topicsMap = mapOf(
        "Matematik" to listOf("Üslü Sayılar", "Köklü Sayılar", "Geometri", "Genel Karışık"),
        "Türkçe" to listOf("Paragrafta Anlam", "Yazım Kuralları", "Noktalama İşaretleri", "Dil Bilgisi"),
        "Fen Bilimleri" to listOf("Kuvvet ve Hareket", "Madde ve Özellikleri", "Hücre ve Bölünmeler", "Genel Karışık"),
        "Sosyal Bilgiler" to listOf("Tarih / Coğrafya", "Demokrasi ve İnsan Hakları", "Ekonomi", "Kültür ve Miras")
    )

    // Sync selected topic when lesson changes
    LaunchedEffect(selectedLesson) {
        selectedTopic = topicsMap[selectedLesson]?.firstOrNull() ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BrandSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextLight
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Konu Testi Oluştur",
                    color = TextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Lesson Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                border = BorderStroke(1.dp, BrandBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Ders Seçimi",
                        color = BrandPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    lessons.forEach { lesson ->
                        val isSelected = selectedLesson == lesson
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BrandPrimaryLight.copy(alpha = 0.3f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) BrandPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedLesson = lesson }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lesson,
                                color = if (isSelected) Color.White else TextLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Seçildi",
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Topic Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                border = BorderStroke(1.dp, BrandBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Çalışmak İstediğiniz Konu",
                        color = BrandPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val topics = topicsMap[selectedLesson] ?: emptyList()
                    
                    // Simple flow or grid layout for topics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topics.take(2).forEach { topic ->
                            val isSelected = selectedTopic == topic
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BrandPrimaryLight else Color(0xFF131A30))
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandPrimary else BrandBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedTopic = topic }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = topic,
                                    color = if (isSelected) Color.White else TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topics.drop(2).forEach { topic ->
                            val isSelected = selectedTopic == topic
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BrandPrimaryLight else Color(0xFF131A30))
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandPrimary else BrandBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedTopic = topic }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = topic,
                                    color = if (isSelected) Color.White else TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Count Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                border = BorderStroke(1.dp, BrandBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Soru Sayısı",
                        color = BrandPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(5, 10, 15).forEach { count ->
                            val isSelected = selectedCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BrandPrimaryLight else Color(0xFF131A30))
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandPrimary else BrandBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedCount = count }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count Soru",
                                    color = if (isSelected) Color.White else TextLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onStartTest(selectedLesson, selectedTopic, selectedCount) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = BrandBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_test_button")
            ) {
                Text(
                    text = "AI Test Sorularını Hazırla",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- 7. ACTIVE EXAM / SIMULATION COCKPIT (Sınav Paneli) ---
@Composable
fun ActiveExamScreen(
    state: ActiveExamState,
    onSelectAnswer: (Int, String) -> Unit,
    onToggleFlag: (Int) -> Unit,
    onSetQuestionIndex: (Int) -> Unit,
    onFinishExam: () -> Unit
) {
    val qIndex = state.currentQuestionIndex
    val totalQuestions = state.questions.size
    val currentQuestion = state.questions.getOrNull(qIndex) ?: return
    val selectedOption = state.answers[qIndex] ?: ""
    val isFlagged = state.flagged.contains(qIndex)

    val minutes = state.timeRemainingSeconds / 60
    val seconds = state.timeRemainingSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    
    // Choose countdown color based on remaining urgency
    val timerColor = when {
        state.timeRemainingSeconds < 60 -> WrongRed
        state.timeRemainingSeconds < 180 -> WarningOrange
        else -> BrandPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Soru ${qIndex + 1} / $totalQuestions",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Flag button
                    IconButton(
                        onClick = { onToggleFlag(qIndex) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isFlagged) WarningOrange.copy(alpha = 0.15f) else BrandSurface)
                    ) {
                        Icon(
                            imageVector = if (isFlagged) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "İşaretle",
                            tint = if (isFlagged) WarningOrange else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Countdown Timer
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(timerColor.copy(alpha = 0.1f))
                            .border(1.dp, timerColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Zamanlayıcı",
                                tint = timerColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timeString,
                                color = timerColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Question Board Scrollable Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // If question has drawing blueprint canvas commands, render it
                if (!currentQuestion.drawingCommands.isNullOrEmpty()) {
                    DrawingCanvas(
                        commandsJson = currentQuestion.drawingCommands,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Lesson / Topic indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${currentQuestion.lesson} • ${currentQuestion.topic}",
                        color = BrandPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Text
                Text(
                    text = currentQuestion.questionText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Options
                val options = listOf(
                    "A" to currentQuestion.optionA,
                    "B" to currentQuestion.optionB,
                    "C" to currentQuestion.optionC,
                    "D" to currentQuestion.optionD
                )

                options.forEach { (label, optionText) ->
                    val isSelected = selectedOption == label
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BrandPrimaryLight.copy(alpha = 0.3f) else BrandSurface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) BrandPrimary else BrandBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onSelectAnswer(qIndex, label) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circle label (A, B, C, D)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BrandPrimary else Color(0xFF131A30)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BrandBackground else TextLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = optionText,
                                color = TextLight,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Virtual Optical Form Sheet (Optik Form Hissi)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101E))
                    .border(BorderStroke(1.dp, BrandBorder.copy(alpha = 0.5f)))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "VIRTÜEL OPTİK FORM KAĞIDI",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Horizontal Row of Question select bubbles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0 until totalQuestions) {
                        val ans = state.answers[i] ?: ""
                        val hasAnswered = ans.isNotEmpty()
                        val isCurr = i == qIndex
                        val isFlag = state.flagged.contains(i)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurr) BrandPrimaryLight.copy(alpha = 0.4f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isCurr) BrandPrimary else if (isFlag) WarningOrange else BrandBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSetQuestionIndex(i) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${i + 1}",
                                color = if (isCurr) BrandPrimary else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasAnswered) BrandPrimary else if (isFlag) WarningOrange.copy(alpha = 0.2f) else BrandBorder.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasAnswered) {
                                    Text(
                                        text = ans,
                                        color = BrandBackground,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Question
                    TextButton(
                        onClick = { onSetQuestionIndex(qIndex - 1) },
                        enabled = qIndex > 0,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White, disabledContentColor = TextMuted)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Önceki")
                            Text("Önceki Soru", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Finish exam prominent button
                    Button(
                        onClick = onFinishExam,
                        colors = ButtonDefaults.buttonColors(containerColor = WrongRed, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("finish_exam_button")
                    ) {
                        Text("Sınavı Bitir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Next Question
                    TextButton(
                        onClick = { onSetQuestionIndex(qIndex + 1) },
                        enabled = qIndex < totalQuestions - 1,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White, disabledContentColor = TextMuted)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sonraki Soru", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Sonraki")
                        }
                    }
                }
            }
        }
    }
}

// --- 8. EXAM RESULTS REPORT BOARD ---
@Composable
fun ExamResultScreen(
    history: ExamHistory,
    questions: List<SolvedQuestion>,
    onExplainQuestion: (SolvedQuestion) -> Unit,
    onDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sınav Sonucu",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDashboard,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BrandSurface)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // Ring Stats Row
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        border = BorderStroke(1.dp, BrandBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BAŞARI KARNENİZ",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                val total = history.totalQuestions.toFloat()
                                CircularStatRing(
                                    percentage = if (total > 0) history.correctCount / total else 0f,
                                    label = "Doğru",
                                    value = "${history.correctCount}",
                                    color = CorrectGreen
                                )
                                CircularStatRing(
                                    percentage = if (total > 0) history.wrongCount / total else 0f,
                                    label = "Yanlış",
                                    value = "${history.wrongCount}",
                                    color = WrongRed
                                )
                                CircularStatRing(
                                    percentage = if (total > 0) history.blankCount / total else 0f,
                                    label = "Boş",
                                    value = "${history.blankCount}",
                                    color = TextMuted
                                )
                            }

                            Divider(
                                color = BrandBorder.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 16.dp)
                            )

                            // Performance Tip
                            val correctRatio = if (history.totalQuestions > 0) history.correctCount.toFloat() / history.totalQuestions else 0f
                            val message = when {
                                correctRatio >= 0.8f -> "Harika bir performans! Konuyu neredeyse tamamen kavramışsınız. Çözemediğiniz soruların AI açıklamalarını inceleyip bir sonraki konuya geçebilirsiniz! 🚀"
                                correctRatio >= 0.5f -> "Güzel gidiyorsunuz! Temeliniz oluşmuş ancak hala bazı eksiklikler mevcut. Yanlış yaptığınız sorulardaki çeldiricileri inceleyip pekiştirin. 👍"
                                else -> "Moral bozmak yok! Sınav Merkezi AI Öğretmeni her zaman yanınızda. Aşağıdaki her bir yanlış soru için 'Neden Yanlış Yaptım?' butonuna tıklayarak eksiklerinizi anında kapatın! 💪"
                            }

                            Text(
                                text = message,
                                color = TextLight,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Soru Soru Analiz",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Questions solved list
                itemsIndexed(questions) { index, q ->
                    val isCorrect = q.chosenAnswer == q.correctAnswer
                    val isBlank = q.chosenAnswer.isEmpty()
                    
                    val statusColor = when {
                        isBlank -> TextMuted
                        isCorrect -> CorrectGreen
                        else -> WrongRed
                    }

                    val statusText = when {
                        isBlank -> "Boş Bırakıldı"
                        isCorrect -> "Doğru Yapıldı"
                        else -> "Yanlış Yapıldı (Sizin Şıkkınız: ${q.chosenAnswer})"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandSurface.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(statusColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = statusColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BrandBorder.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Doğru Şık: ${q.correctAnswer}",
                                        color = TextLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = q.questionText,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            // Show drawing canvas blueprint if exists in history
                            if (!q.drawingCommands.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                DrawingCanvas(commandsJson = q.drawingCommands)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive "Neden Yanlış Yaptım?" AI Button
                            Button(
                                onClick = { onExplainQuestion(q) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCorrect) BrandPrimaryLight.copy(alpha = 0.6f) else BrandPrimaryLight,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("explain_question_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Çözüm Anlatımı",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCorrect) "Soruyu AI'dan Derinlemesine Öğren" else "Neden Yanlış Yaptım? (AI Öğretmen Açıklasın)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// --- 9. AI ANALYSIS DEPOT BOARD (AI Analiz Deposu) ---
@Composable
fun AIAnalysisScreen(
    reportText: String?,
    isLoading: Boolean,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BrandSurface)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextLight)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "AI Analiz Deposu & Gelişim Grafiği",
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BrandPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Yapay Zeka Karne Verilerinizi Analiz Ediyor...",
                            color = TextLight,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Gelişim grafikleri ve 2 günlük özel reçete hazırlanıyor.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Header Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        border = BorderStroke(1.dp, BrandBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Analiz",
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "7/24 Akıllı Rehberlik",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Son çözülen sınavlarınızdaki güçlü ve zayıf halkalar",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Report Card with Markdown formatted text
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandSurface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BrandBorder.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            if (reportText != null) {
                                MarkdownText(text = reportText)
                            } else {
                                Text(
                                    text = "Karne raporu hazırlanamadı. Lütfen analizi tetiklemek için önce sınav çözün.",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// --- 10. MISTAKE BOX (Hata Kutusu / Yanlışlarım) ---
@Composable
fun MistakeBoxScreen(
    mistakes: List<MistakeQuestion>,
    onResolveMistake: (Int) -> Unit,
    onExplainMistake: (MistakeQuestion) -> Unit,
    onBack: () -> Unit
) {
    var selectedLessonFilter by remember { mutableStateOf("Hepsi") }
    val lessons = listOf("Hepsi", "Matematik", "Türkçe", "Fen Bilimleri", "Sosyal Bilgiler")

    val filteredMistakes = remember(mistakes, selectedLessonFilter) {
        if (selectedLessonFilter == "Hepsi") mistakes
        else mistakes.filter { it.lesson == selectedLessonFilter }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BrandSurface)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextLight)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Hata Kutusu (Yanlışlarım)",
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Filters horizontal row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lessons.forEach { lesson ->
                    val isSelected = selectedLessonFilter == lesson
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandPrimaryLight else BrandSurface)
                            .border(
                                1.dp,
                                if (isSelected) BrandPrimary else BrandBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedLessonFilter = lesson }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = lesson,
                            color = if (isSelected) Color.White else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mistakes list
            if (filteredMistakes.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ThumbUpAlt,
                            contentDescription = "Temiz",
                            tint = CorrectGreen,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hata Kutunuz Bomboş! 🌟",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Harika çalışıyorsunuz, yanlış yaptığınız tüm soruları öğrendiniz ve arşivlediniz.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    items(filteredMistakes) { q ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandSurface.copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, BrandBorder.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BrandPrimary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${q.lesson} • ${q.topic}",
                                            color = BrandPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Mark as Resolved Button
                                    TextButton(
                                        onClick = { onResolveMistake(q.id) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = CorrectGreen)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = "Öğrendim", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Öğrendim, Kutudan Çıkar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = q.questionText,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                if (!q.drawingCommands.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DrawingCanvas(commandsJson = q.drawingCommands)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Options block
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131A30).copy(alpha = 0.5f))
                                        .clip(RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(text = "A) ", color = BrandPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = q.optionA, color = TextLight, fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(text = "B) ", color = BrandPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = q.optionB, color = TextLight, fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(text = "C) ", color = BrandPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = q.optionC, color = TextLight, fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(text = "D) ", color = BrandPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = q.optionD, color = TextLight, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CorrectGreen.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Doğru Şık: ${q.correctAnswer}", color = CorrectGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (q.chosenAnswer.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(WrongRed.copy(alpha = 0.1f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "Sizin Şıkkınız: ${q.chosenAnswer}", color = WrongRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Explanations Trigger
                                Button(
                                    onClick = { onExplainMistake(q) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryLight, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Öğretmen Açıklasın", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Adım Adım Açıklasın (Hata Analizi)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 11. 7/24 INTERACTIVE CHAT SCREEN (7/24 AI Öğretmen) ---
@Composable
fun AITeacherScreen(
    messages: List<ChatMessage>,
    isSending: Boolean,
    onSendMessage: (String) -> Unit,
    onResetChat: () -> Unit,
    onBack: () -> Unit
) {
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Suggestions chip row list
    val suggestionChips = listOf(
        "Matematik formüllerini ezberleme taktiği ver.",
        "Müfredat LGS Matematik konuları nelerdir?",
        "2 saatlik Pomodoro ders çalışma planı hazırlar mısın?",
        "Sınav stresi ve odaklanma problemi için tavsiyeler."
    )

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrandSurface)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextLight)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "7/24 AI Öğretmen",
                            color = TextLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CorrectGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Aktif ve Hazır", color = CorrectGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                TextButton(onClick = onResetChat, colors = ButtonDefaults.textButtonColors(contentColor = WrongRed)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sohbeti Sıfırla", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sohbeti Sıfırla", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Message History list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "User"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            // Avatar placeholder
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BrandSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Face, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) BrandPrimary else BrandSurface
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            border = if (isUser) null else BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (isUser) {
                                    Text(
                                        text = msg.text,
                                        color = BrandBackground,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                } else {
                                    MarkdownText(text = msg.text)
                                }
                            }
                        }

                        if (isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = "Ben", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (isSending) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BrandSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Face, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                                border = BorderStroke(1.dp, BrandBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "Yapay Zeka Öğretmeni yazıyor...", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestionChips.forEach { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandSurface)
                            .border(1.dp, BrandBorder, RoundedCornerShape(8.dp))
                            .clickable { onSendMessage(chip) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = chip, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Input panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandSurface)
                    .border(BorderStroke(1.dp, BrandBorder.copy(alpha = 0.5f)))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = { Text("AI Öğretmene sor...", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input")
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        if (textState.trim().isNotEmpty()) {
                            onSendMessage(textState)
                            textState = ""
                        }
                    },
                    enabled = textState.trim().isNotEmpty() && !isSending,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (textState.trim().isNotEmpty()) BrandPrimary else BrandBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gönder",
                        tint = if (textState.trim().isNotEmpty()) BrandBackground else TextMuted
                    )
                }
            }
        }
    }
}

// --- 12. EXPLANATION FLOATING POPUP DIALOG ---
@Composable
fun ExplanationDialog(
    explanationText: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    if (explanationText != null || isLoading) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Dialog Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C101E))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Çözüm", tint = BrandPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Yapay Zeka Çözüm Analizi", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                        }
                    }

                    // Content
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = BrandPrimary)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(text = "AI Adım Adım Çözümü Çıkarıyor...", color = TextLight, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(18.dp)
                        ) {
                            if (explanationText != null) {
                                MarkdownText(text = explanationText)
                            }
                        }
                    }

                    // Bottom close actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C101E))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = BrandBackground),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Anladım", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

