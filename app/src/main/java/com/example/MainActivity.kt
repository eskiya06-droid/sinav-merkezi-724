package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: ExamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent(viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: ExamViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val historyList by viewModel.examHistoryList.collectAsState()
    val mistakesList by viewModel.mistakes.collectAsState()
    
    val activeExamState by viewModel.activeExamState.collectAsState()
    val lastSolvedExam by viewModel.lastSolvedExam.collectAsState()
    val lastSolvedQuestions by viewModel.lastSolvedQuestions.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    
    val isGeneratingQuestions by viewModel.isGeneratingQuestions.collectAsState()
    val isSendingChatMessage by viewModel.isSendingChatMessage.collectAsState()
    val aiReportText by viewModel.aiReportText.collectAsState()
    val isGeneratingReport by viewModel.isGeneratingReport.collectAsState()
    
    val activeExplanation by viewModel.activeExplanation.collectAsState()
    val isGeneratingExplanation by viewModel.isGeneratingExplanation.collectAsState()

    // Explanation Dialog Overlay
    ExplanationDialog(
        explanationText = activeExplanation,
        isLoading = isGeneratingExplanation,
        onDismiss = { viewModel.clearExplanation() }
    )

    // Interactive Loading Dialog Overlay
    if (isGeneratingQuestions) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6FDF7FF)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Yapay Zeka Sorularını Hazırlıyor...",
                        color = TextLight,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sınav Merkezi 7/24 müfredatınıza uygun özgün sorular türetiyor. Lütfen bekleyin.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }

    // Outer Shell with edge to edge safeDrawing margins
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (profile != null && currentScreen != AppScreen.ONBOARDING && currentScreen != AppScreen.ACTIVE_EXAM) {
                NavigationBar(
                    containerColor = Color(0xFFF3EDF7),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.TEST_CONFIG || currentScreen == AppScreen.EXAM_RESULT,
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Panel") },
                        label = { Text("Panel", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.AI_ANALYSIS,
                        onClick = { viewModel.navigateTo(AppScreen.AI_ANALYSIS) },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = "AI Karne") },
                        label = { Text("AI Karne", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.MISTAKE_BOX,
                        onClick = { viewModel.navigateTo(AppScreen.MISTAKE_BOX) },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Yanlışlarım") },
                        label = { Text("Yanlışlarım", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.AI_TEACHER,
                        onClick = { viewModel.navigateTo(AppScreen.AI_TEACHER) },
                        icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "7/24 AI") },
                        label = { Text("7/24 AI", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandBackground)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.ONBOARDING -> {
                    AuthScreen(
                        onRegister = { name, identifier, pass, exam, field ->
                            viewModel.registerUser(name, identifier, pass, exam, field)
                        },
                        onLogin = { identifier, pass ->
                            viewModel.loginUser(identifier, pass)
                        }
                    )
                }
                AppScreen.DASHBOARD -> {
                    profile?.let { p ->
                        DashboardScreen(
                            profile = p,
                            historyList = historyList,
                            mistakeCount = mistakesList.size,
                            onNavigate = { screen ->
                                if (screen == AppScreen.ACTIVE_EXAM) {
                                    viewModel.startFullMockExam()
                                } else {
                                    viewModel.navigateTo(screen)
                                }
                            }
                        )
                    }
                }
                AppScreen.TEST_CONFIG -> {
                    TestConfigScreen(
                        onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        onStartTest = { lesson, topic, count ->
                            viewModel.startInstantTest(lesson, topic, count)
                        }
                    )
                }
                AppScreen.ACTIVE_EXAM -> {
                    activeExamState?.let { s ->
                        ActiveExamScreen(
                            state = s,
                            onSelectAnswer = { index, answer -> viewModel.selectAnswer(index, answer) },
                            onToggleFlag = { index -> viewModel.toggleFlag(index) },
                            onSetQuestionIndex = { index -> viewModel.setQuestionIndex(index) },
                            onFinishExam = { viewModel.finishExam() }
                        )
                    }
                }
                AppScreen.EXAM_RESULT -> {
                    lastSolvedExam?.let { h ->
                        ExamResultScreen(
                            history = h,
                            questions = lastSolvedQuestions,
                            onExplainQuestion = { q -> viewModel.explainQuestionText(q) },
                            onDashboard = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                        )
                    }
                }
                AppScreen.AI_ANALYSIS -> {
                    AIAnalysisScreen(
                        reportText = aiReportText,
                        isLoading = isGeneratingReport,
                        onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                }
                AppScreen.MISTAKE_BOX -> {
                    MistakeBoxScreen(
                        mistakes = mistakesList,
                        onResolveMistake = { id -> viewModel.resolveMistake(id) },
                        onExplainMistake = { q -> viewModel.explainMistakeQuestionText(q) },
                        onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                }
                AppScreen.AI_TEACHER -> {
                    AITeacherScreen(
                        messages = chatMessages,
                        isSending = isSendingChatMessage,
                        onSendMessage = { text -> viewModel.sendChatMessage(text) },
                        onResetChat = { viewModel.resetChat() },
                        onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                }
            }
        }
    }
}

