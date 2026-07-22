import re

file_path = r'c:\Users\v23\Desktop\sınav-merkezi-724\app\src\main\java\com\example\ui\ExamScreens.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure imports are present
imports_to_add = [
    "import androidx.compose.ui.draw.drawBehind",
    "import androidx.compose.ui.graphics.Paint",
    "import androidx.compose.ui.graphics.drawscope.drawIntoCanvas",
    "import androidx.compose.ui.graphics.toArgb",
    "import androidx.compose.ui.graphics.Brush",
    "import androidx.compose.ui.graphics.Path",
    "import androidx.compose.ui.graphics.drawscope.Stroke",
    "import androidx.compose.ui.graphics.StrokeCap",
    "import androidx.compose.ui.graphics.StrokeJoin",
    "import androidx.compose.material.icons.filled.Science",
    "import androidx.compose.material.icons.filled.Face",
    "import androidx.compose.material.icons.filled.Bolt",
    "import androidx.compose.material.icons.filled.FlashOn",
    "import androidx.compose.material.icons.filled.Schedule",
    "import androidx.compose.material.icons.filled.BarChart",
    "import androidx.compose.material.icons.filled.GraphicEq",
]

for imp in imports_to_add:
    if imp not in content:
        content = content.replace("import androidx.compose.ui.unit.sp", f"import androidx.compose.ui.unit.sp\n{imp}")

new_dashboard_code = """// Helper for custom colored glow shadow
fun Modifier.coloredGlow(
    color: Color,
    alpha: Float = 0.4f,
    borderRadius: Dp = 16.dp,
    blurRadius: Dp = 16.dp,
    offsetY: Dp = 8.dp
) = this.drawBehind {
    val transparentColor = android.graphics.Color.toArgb(color.copy(alpha = 0.0f).value.toLong())
    val shadowColor = android.graphics.Color.toArgb(color.copy(alpha = alpha).value.toLong())
    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            0f,
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
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
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF0E5), // Very light peach
            Color(0xFFF3F7FF), // Light blueish
            Color(0xFFFFFFFF)  // White
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            tint = Color(0xFFE0E5FF).copy(alpha = 0.4f),
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFFFFE0B2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Face, contentDescription = "Avatar", tint = Color(0xFFE65100), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.3f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2C5282))
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Merhaba, ${profile.username}",
                        color = Color(0xFF8D6E63),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Çalışmaya Başla 👋",
                        color = Color(0xFF212121),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Box(
                    modifier = Modifier
                        .coloredGlow(color = Color(0xFF64B5F6), alpha = 0.2f, blurRadius = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "YKS", color = Color(0xFF1E88E5), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "(TYT-AYT) |", color = Color(0xFF1E88E5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Sayısal", color = Color(0xFF1E88E5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val totalSolved = historyList.size
            val totalCorrect = historyList.sumOf { it.correctCount }
            val totalWrong = historyList.sumOf { it.wrongCount }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SparklineStatCard(modifier = Modifier.weight(1f), title = "Deneme", value = "$totalSolved", color = Color(0xFF1E88E5))
                SparklineStatCard(modifier = Modifier.weight(1f), title = "Doğru", value = "$totalCorrect", color = Color(0xFF43A047))
                SparklineStatCard(modifier = Modifier.weight(1f), title = "Hatalı", value = "$totalWrong", color = Color(0xFFE53935))
                SparklineStatCard(modifier = Modifier.weight(1f), title = "Kutuda", value = "$mistakeCount", color = Color(0xFFFDD835))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Modüller",
                color = Color(0xFF212121),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GradientActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Konu Testi",
                    icon = Icons.Default.Bolt,
                    bgIcon = Icons.Default.FlashOn,
                    gradient = Brush.linearGradient(listOf(Color(0xFFFDFBF7), Color(0xFFE3EBF7))),
                    iconTint = Color(0xFF395B9A),
                    onClick = { onNavigate(AppScreen.TEST_CONFIG) }
                )
                GradientActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Tam Deneme",
                    icon = Icons.Default.Timer,
                    bgIcon = Icons.Default.Schedule,
                    gradient = Brush.linearGradient(listOf(Color(0xFFFFF2E8), Color(0xFFFFDFD6))),
                    iconTint = Color(0xFFD66947),
                    onClick = { onNavigate(AppScreen.ACTIVE_EXAM) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GradientActionCard(
                    modifier = Modifier.weight(1f),
                    title = "AI Karne",
                    icon = Icons.Default.Analytics,
                    bgIcon = Icons.Default.BarChart,
                    gradient = Brush.linearGradient(listOf(Color(0xFFF4EDFF), Color(0xFFE8DDFB))),
                    iconTint = Color(0xFF6B459E),
                    onClick = { onNavigate(AppScreen.AI_ANALYSIS) }
                )
                GradientActionCard(
                    modifier = Modifier.weight(1f),
                    title = "7/24 Rehber",
                    icon = Icons.Default.QuestionAnswer,
                    bgIcon = Icons.Default.GraphicEq,
                    gradient = Brush.linearGradient(listOf(Color(0xFFE0F7FA), Color(0xFFD3EFEF))),
                    iconTint = Color(0xFF368A86),
                    onClick = { onNavigate(AppScreen.AI_TEACHER) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onNavigate(AppScreen.ACTIVE_EXAM) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF104678)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .coloredGlow(color = Color(0xFF104678), alpha = 0.3f, blurRadius = 20.dp, offsetY = 10.dp)
            ) {
                Text("Start Your First Test", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SparklineStatCard(modifier: Modifier, title: String, value: String, color: Color) {
    Box(
        modifier = modifier
            .coloredGlow(color = color, alpha = 0.2f, borderRadius = 16.dp, blurRadius = 12.dp, offsetY = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(top = 16.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = Color(0xFF757575), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                val path = Path()
                val width = size.width
                val height = size.height
                path.moveTo(10f, height / 2)
                path.cubicTo(width * 0.25f, 0f, width * 0.25f, height, width * 0.5f, height / 2)
                path.cubicTo(width * 0.75f, 0f, width * 0.75f, height, width - 10f, height / 2)
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawCircle(color = color, radius = 6f, center = androidx.compose.ui.geometry.Offset(width - 10f, height / 2))
            }
        }
    }
}

@Composable
fun GradientActionCard(
    modifier: Modifier, 
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    bgIcon: androidx.compose.ui.graphics.vector.ImageVector, 
    gradient: Brush, 
    iconTint: Color, 
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .coloredGlow(color = iconTint, alpha = 0.15f, borderRadius = 24.dp, blurRadius = 16.dp, offsetY = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .clickable { onClick() }
            .height(110.dp)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = bgIcon,
            contentDescription = null,
            tint = iconTint.copy(alpha = 0.12f),
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp, y = 0.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Text(text = title, color = Color(0xFF212121), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
"""

pattern = re.compile(r"// --- 5\. Student Dashboard Screen ---.*?// --- 6\.", re.DOTALL)
new_content = pattern.sub(new_dashboard_code + "\n\n// --- 6.", content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("SUCCESS")
