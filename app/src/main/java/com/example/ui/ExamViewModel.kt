package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ExamHistory
import com.example.data.ExamRepository
import com.example.data.SolvedQuestion
import com.example.data.UserProfile
import com.example.data.MistakeQuestion
import com.example.network.Content
import com.example.network.GeminiService
import com.example.network.GeneratedQuestion
import com.example.network.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    ONBOARDING,
    DASHBOARD,
    TEST_CONFIG,
    ACTIVE_EXAM,
    EXAM_RESULT,
    AI_ANALYSIS,
    MISTAKE_BOX,
    AI_TEACHER,
    PROFILE
}

data class ActiveExamState(
    val examType: String, // "Instant" or "Mock"
    val title: String,
    val questions: List<GeneratedQuestion>,
    val answers: Map<Int, String> = emptyMap(), // questionIndex -> "A"|"B"|"C"|"D"
    val flagged: Set<Int> = emptySet(),
    val currentQuestionIndex: Int = 0,
    val timeRemainingSeconds: Int,
    val totalTimeSeconds: Int,
    val isTimerActive: Boolean = true
)

data class ChatMessage(
    val sender: String, // "User" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ExamRepository(db)
    private val geminiService = GeminiService()

    // --- State Flows ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val examHistoryList: StateFlow<List<ExamHistory>> = repository.examHistoryList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<MistakeQuestion>> = repository.mistakeList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.ONBOARDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeExamState = MutableStateFlow<ActiveExamState?>(null)
    val activeExamState: StateFlow<ActiveExamState?> = _activeExamState.asStateFlow()

    private val _lastSolvedExam = MutableStateFlow<ExamHistory?>(null)
    val lastSolvedExam: StateFlow<ExamHistory?> = _lastSolvedExam.asStateFlow()

    private val _lastSolvedQuestions = MutableStateFlow<List<SolvedQuestion>>(emptyList())
    val lastSolvedQuestions: StateFlow<List<SolvedQuestion>> = _lastSolvedQuestions.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("AI", "Merhaba! Ben Yapay Zeka Öğretmeniniz. Sınav hazırlık sürecinizde konu özetleri, çalışma programları, Pomodoro planlaması yapmak veya kafanıza takılan soruları sormak için 7/24 buradayım. Bugün hangi konuyu çalışalım?"))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGeneratingQuestions = MutableStateFlow(false)
    val isGeneratingQuestions: StateFlow<Boolean> = _isGeneratingQuestions.asStateFlow()

    private val _isSendingChatMessage = MutableStateFlow(false)
    val isSendingChatMessage: StateFlow<Boolean> = _isSendingChatMessage.asStateFlow()

    private val _aiReportText = MutableStateFlow<String?>(null)
    val aiReportText: StateFlow<String?> = _aiReportText.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    private val _activeExplanation = MutableStateFlow<String?>(null)
    val activeExplanation: StateFlow<String?> = _activeExplanation.asStateFlow()

    private val _isGeneratingExplanation = MutableStateFlow(false)
    val isGeneratingExplanation: StateFlow<Boolean> = _isGeneratingExplanation.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Detect if profile exists to determine initial screen
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                if (profile != null) {
                    if (_currentScreen.value == AppScreen.ONBOARDING) {
                        _currentScreen.value = AppScreen.DASHBOARD
                    }
                } else {
                    _currentScreen.value = AppScreen.ONBOARDING
                }
            }
        }
    }

    // --- Onboarding / Profile ---
    fun registerUser(username: String, identifier: String, passwordHash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(
                UserProfile(username = username, identifier = identifier, passwordHash = passwordHash, targetExam = "Belirtilmedi", field = "Belirtilmedi")
            )
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun loginUser(identifier: String, passwordHash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var profile = repository.getProfile()
            if (profile != null) {
                _currentScreen.value = AppScreen.DASHBOARD
            } else {
                profile = UserProfile(username = "Öğrenci", identifier = identifier, passwordHash = passwordHash, targetExam = "YKS (TYT-AYT)", field = "Sayısal")
                repository.updateProfile(profile)
                _currentScreen.value = AppScreen.DASHBOARD
            }
        }
    }

    fun updateProfile(username: String, identifier: String, passwordHash: String, targetExam: String, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(
                UserProfile(username = username, identifier = identifier, passwordHash = passwordHash, targetExam = targetExam, field = field)
            )
        }
    }

    fun updateExamPreferences(targetExam: String, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProfile()?.let { profile ->
                repository.updateProfile(profile.copy(targetExam = targetExam, field = field))
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            // Mock logout: just navigate back to ONBOARDING for now
            // In a real app, you might clear session tokens, or even clear the DB profile if needed.
            // But we keep the profile locally and just show the AuthScreen
            _currentScreen.value = AppScreen.ONBOARDING
        }
    }

    // --- Screen Navigation ---
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.AI_ANALYSIS) {
            triggerAIReportAnalysis()
        }
    }

    // --- Exam Setup & Game Loop ---
    fun startInstantTest(lesson: String, topic: String, count: Int) {
        viewModelScope.launch {
            _isGeneratingQuestions.value = true
            val profile = repository.getProfile() ?: UserProfile(username = "Öğrenci", identifier = "demo@demo.com", passwordHash = "123", targetExam = "TYT", field = "Sayısal")
            
            try {
                val questions = geminiService.generateQuestions(
                    lesson = lesson,
                    topic = topic,
                    count = count,
                    examType = profile.targetExam,
                    field = profile.field
                )

                _activeExamState.value = ActiveExamState(
                    examType = "Instant",
                    title = "$lesson - $topic Testi",
                    questions = questions,
                    timeRemainingSeconds = count * 90, // 1.5 minutes per question
                    totalTimeSeconds = count * 90
                )
                
                _currentScreen.value = AppScreen.ACTIVE_EXAM
                startTimer()
            } catch (e: Exception) {
                Log.e("ExamViewModel", "Failed to start instant test", e)
            } finally {
                _isGeneratingQuestions.value = false
            }
        }
    }

    fun startFullMockExam() {
        viewModelScope.launch {
            _isGeneratingQuestions.value = true
            val profile = repository.getProfile() ?: UserProfile(username = "Öğrenci", identifier = "demo@demo.com", passwordHash = "123", targetExam = "TYT", field = "Sayısal")
            
            val examTitle = "${profile.targetExam} Pro Deneme Sınavı"
            val totalQuestionsCount = 10 // For smooth loading and best experience, a high-quality 10 question multi-subject mock
            
            try {
                // Generate across major subjects depending on field
                val lesson1 = "Matematik"
                val lesson2 = if (profile.field == "Sayısal") "Fen Bilimleri" else "Türkçe"
                
                val q1 = geminiService.generateQuestions(lesson1, "Genel Karışık", totalQuestionsCount / 2, profile.targetExam, profile.field)
                val q2 = geminiService.generateQuestions(lesson2, "Genel Karışık", totalQuestionsCount / 2, profile.targetExam, profile.field)
                
                val combined = q1 + q2

                _activeExamState.value = ActiveExamState(
                    examType = "Mock",
                    title = examTitle,
                    questions = combined,
                    timeRemainingSeconds = totalQuestionsCount * 120, // 2 minutes per question for mock
                    totalTimeSeconds = totalQuestionsCount * 120
                )
                
                _currentScreen.value = AppScreen.ACTIVE_EXAM
                startTimer()
            } catch (e: Exception) {
                Log.e("ExamViewModel", "Failed to start mock exam", e)
            } finally {
                _isGeneratingQuestions.value = false
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _activeExamState.value ?: break
                if (!currentState.isTimerActive) continue
                
                if (currentState.timeRemainingSeconds <= 1) {
                    _activeExamState.value = currentState.copy(timeRemainingSeconds = 0)
                    finishExam()
                    break
                } else {
                    _activeExamState.value = currentState.copy(
                        timeRemainingSeconds = currentState.timeRemainingSeconds - 1
                    )
                }
            }
        }
    }

    fun selectAnswer(questionIndex: Int, answer: String) {
        val currentState = _activeExamState.value ?: return
        val updatedAnswers = currentState.answers.toMutableMap().apply {
            put(questionIndex, answer)
        }
        _activeExamState.value = currentState.copy(answers = updatedAnswers)
    }

    fun toggleFlag(questionIndex: Int) {
        val currentState = _activeExamState.value ?: return
        val updatedFlagged = currentState.flagged.toMutableSet().apply {
            if (contains(questionIndex)) remove(questionIndex) else add(questionIndex)
        }
        _activeExamState.value = currentState.copy(flagged = updatedFlagged)
    }

    fun setQuestionIndex(index: Int) {
        val currentState = _activeExamState.value ?: return
        if (index in 0 until currentState.questions.size) {
            _activeExamState.value = currentState.copy(currentQuestionIndex = index)
        }
    }

    fun finishExam() {
        timerJob?.cancel()
        val examState = _activeExamState.value ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            var corrects = 0
            var wrongs = 0
            var blanks = 0

            val solvedQuestionsList = examState.questions.mapIndexed { idx, q ->
                val chosen = examState.answers[idx] ?: ""
                if (chosen.isEmpty()) {
                    blanks++
                } else if (chosen == q.correctAnswer) {
                    corrects++
                } else {
                    wrongs++
                }

                SolvedQuestion(
                    historyId = 0, // Will be set in repo
                    lesson = q.lesson,
                    topic = q.topic,
                    questionText = q.questionText,
                    optionA = q.optionA,
                    optionB = q.optionB,
                    optionC = q.optionC,
                    optionD = q.optionD,
                    correctAnswer = q.correctAnswer,
                    chosenAnswer = chosen,
                    explanation = q.explanation,
                    isFlagged = examState.flagged.contains(idx),
                    drawingCommands = q.drawingCommands
                )
            }

            val durationSpent = examState.totalTimeSeconds - examState.timeRemainingSeconds

            val historyEntry = ExamHistory(
                examType = examState.examType,
                title = examState.title,
                durationSeconds = durationSpent,
                totalQuestions = examState.questions.size,
                correctCount = corrects,
                wrongCount = wrongs,
                blankCount = blanks,
                aiAnalysisReport = "" // Can be empty initially or generated
            )

            // Save to database & repository (handles adding mistakes automatically)
            repository.insertExamHistory(historyEntry, solvedQuestionsList)

            // Query the newly inserted history item from history flow to display in results
            _lastSolvedExam.value = historyEntry
            _lastSolvedQuestions.value = solvedQuestionsList
            
            // Wipe active exam state
            _activeExamState.value = null
            
            // Navigate to results screen
            viewModelScope.launch(Dispatchers.Main) {
                _currentScreen.value = AppScreen.EXAM_RESULT
            }
        }
    }

    // --- Hata Kutusu (Mistakes Box) Retries & Explanations ---
    fun explainQuestionText(question: SolvedQuestion) {
        viewModelScope.launch {
            _isGeneratingExplanation.value = true
            _activeExplanation.value = null
            try {
                val explanation = geminiService.explainMistake(
                    questionText = question.questionText,
                    options = listOf(question.optionA, question.optionB, question.optionC, question.optionD),
                    correctAnswer = question.correctAnswer,
                    chosenAnswer = question.chosenAnswer,
                    explanation = question.explanation
                )
                _activeExplanation.value = explanation
            } catch (e: Exception) {
                _activeExplanation.value = "Çözüm açıklaması yüklenirken hata oluştu: ${e.localizedMessage}"
            } finally {
                _isGeneratingExplanation.value = false
            }
        }
    }

    fun explainMistakeQuestionText(question: MistakeQuestion) {
        viewModelScope.launch {
            _isGeneratingExplanation.value = true
            _activeExplanation.value = null
            try {
                val explanation = geminiService.explainMistake(
                    questionText = question.questionText,
                    options = listOf(question.optionA, question.optionB, question.optionC, question.optionD),
                    correctAnswer = question.correctAnswer,
                    chosenAnswer = question.chosenAnswer,
                    explanation = question.explanation
                )
                _activeExplanation.value = explanation
            } catch (e: Exception) {
                _activeExplanation.value = "Çözüm açıklaması yüklenirken hata oluştu: ${e.localizedMessage}"
            } finally {
                _isGeneratingExplanation.value = false
            }
        }
    }

    fun clearExplanation() {
        _activeExplanation.value = null
    }

    fun resolveMistake(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resolveMistake(id)
        }
    }

    // --- AI Report Analysis ---
    fun triggerAIReportAnalysis() {
        viewModelScope.launch {
            _isGeneratingReport.value = true
            try {
                val profile = repository.getProfile() ?: return@launch
                val history = repository.examHistoryList.stateIn(viewModelScope).value
                val report = geminiService.generateAnalysisReport(profile, history)
                _aiReportText.value = report
            } catch (e: Exception) {
                _aiReportText.value = "Analiz raporu oluşturulamadı. Lütfen sınav çözdükten sonra tekrar deneyin."
            } finally {
                _isGeneratingReport.value = false
            }
        }
    }

    // --- AI Teacher Chat ---
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        
        val userMsg = ChatMessage("User", text)
        _chatMessages.value = _chatMessages.value + userMsg
        _isSendingChatMessage.value = true

        viewModelScope.launch {
            try {
                val profile = repository.getProfile() ?: UserProfile(username = "Öğrenci", identifier = "demo@demo.com", passwordHash = "123", targetExam = "TYT", field = "Sayısal")
                
                // Map local chat message structure to Gemini API Content/Part structure
                val apiHistory = _chatMessages.value.dropLast(1).map { msg ->
                    Content(parts = listOf(Part(text = "${if (msg.sender == "User") "Öğrenci" else "Öğretmen"}: ${msg.text}")))
                }

                val reply = geminiService.chatWithTeacher(
                    chatHistory = apiHistory,
                    userMessage = text,
                    profile = profile
                )

                _chatMessages.value = _chatMessages.value + ChatMessage("AI", reply)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("AI", "Bağlantı kesildi. Lütfen tekrar deneyin.")
            } finally {
                _isSendingChatMessage.value = false
            }
        }
    }

    fun resetChat() {
        _chatMessages.value = listOf(
            ChatMessage("AI", "Merhaba! Ben Yapay Zeka Öğretmeniniz. Sınav hazırlık sürecinizde konu özetleri, çalışma programları, Pomodoro planlaması yapmak veya kafanıza takılan soruları sormak için 7/24 buradayım. Bugün hangi konuyu çalışalım?")
        )
    }
}
