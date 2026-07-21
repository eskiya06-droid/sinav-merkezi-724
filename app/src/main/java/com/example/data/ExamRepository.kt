package com.example.data

import kotlinx.coroutines.flow.Flow

class ExamRepository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfile?> = db.userProfileDao().getProfileFlow()
    val examHistoryList: Flow<List<ExamHistory>> = db.examHistoryDao().getAllHistoryFlow()
    val mistakeList: Flow<List<MistakeQuestion>> = db.mistakeQuestionDao().getAllMistakesFlow()

    suspend fun getProfile(): UserProfile? = db.userProfileDao().getProfile()

    suspend fun updateProfile(profile: UserProfile) {
        db.userProfileDao().insertOrUpdate(profile)
    }

    suspend fun insertExamHistory(
        history: ExamHistory,
        questions: List<SolvedQuestion>
    ) {
        val historyId = db.examHistoryDao().insertHistory(history).toInt()
        val questionsWithHistoryId = questions.map { it.copy(historyId = historyId) }
        db.solvedQuestionDao().insertAll(questionsWithHistoryId)

        // For each wrong question, automatically save to Hata Kutusu (Mistake Box)
        questionsWithHistoryId.forEach { q ->
            if (q.chosenAnswer.isNotEmpty() && q.chosenAnswer != q.correctAnswer) {
                db.mistakeQuestionDao().insertMistake(
                    MistakeQuestion(
                        lesson = q.lesson,
                        topic = q.topic,
                        questionText = q.questionText,
                        optionA = q.optionA,
                        optionB = q.optionB,
                        optionC = q.optionC,
                        optionD = q.optionD,
                        correctAnswer = q.correctAnswer,
                        chosenAnswer = q.chosenAnswer,
                        explanation = q.explanation,
                        drawingCommands = q.drawingCommands
                    )
                )
            }
        }
    }

    suspend fun deleteHistory(historyId: Int) {
        db.examHistoryDao().deleteHistoryById(historyId)
    }

    suspend fun getQuestionsForHistory(historyId: Int): List<SolvedQuestion> {
        return db.solvedQuestionDao().getQuestionsForHistory(historyId)
    }

    suspend fun addMistake(mistake: MistakeQuestion) {
        db.mistakeQuestionDao().insertMistake(mistake)
    }

    suspend fun resolveMistake(id: Int) {
        db.mistakeQuestionDao().deleteMistakeById(id)
    }
    
    suspend fun getMistakeQuestions(): List<MistakeQuestion> {
        return db.mistakeQuestionDao().getAllMistakes()
    }
}
