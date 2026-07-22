package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- 1. Entities ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val identifier: String, // email, phone, or username
    val passwordHash: String, // Mock password
    val targetExam: String, // "YKS (TYT-AYT)", "LGS", "KPSS", "DGS"
    val field: String,      // "Sayısal", "Sözel", "Eşit Ağırlık", "Genel"
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exam_history")
data class ExamHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examType: String, // "Instant" or "Mock"
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val blankCount: Int,
    val aiAnalysisReport: String // Markdown report from Gemini
)

@Entity(tableName = "solved_question")
data class SolvedQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val historyId: Int,
    val lesson: String,
    val topic: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val chosenAnswer: String,  // "A", "B", "C", "D", or "" (blank)
    val explanation: String,   // Step by step explanation
    val isFlagged: Boolean = false,
    val drawingCommands: String? = null // JSON coordinates for geometry/charts
)

@Entity(tableName = "mistake_question")
data class MistakeQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lesson: String,
    val topic: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val chosenAnswer: String,
    val explanation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val drawingCommands: String? = null
)

// --- 2. DAOs ---

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)
}

@Dao
interface ExamHistoryDao {
    @Query("SELECT * FROM exam_history ORDER BY date DESC")
    fun getAllHistoryFlow(): Flow<List<ExamHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ExamHistory): Long

    @Query("DELETE FROM exam_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)
}

@Dao
interface SolvedQuestionDao {
    @Query("SELECT * FROM solved_question WHERE historyId = :historyId ORDER BY id ASC")
    suspend fun getQuestionsForHistory(historyId: Int): List<SolvedQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<SolvedQuestion>)
}

@Dao
interface MistakeQuestionDao {
    @Query("SELECT * FROM mistake_question ORDER BY timestamp DESC")
    fun getAllMistakesFlow(): Flow<List<MistakeQuestion>>

    @Query("SELECT * FROM mistake_question ORDER BY timestamp DESC")
    suspend fun getAllMistakes(): List<MistakeQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(question: MistakeQuestion)

    @Query("DELETE FROM mistake_question WHERE id = :id")
    suspend fun deleteMistakeById(id: Int)

    @Query("DELETE FROM mistake_question WHERE questionText = :questionText")
    suspend fun deleteMistakeByText(questionText: String)
}

// --- 3. Database ---

@Database(
    entities = [UserProfile::class, ExamHistory::class, SolvedQuestion::class, MistakeQuestion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun examHistoryDao(): ExamHistoryDao
    abstract fun solvedQuestionDao(): SolvedQuestionDao
    abstract fun mistakeQuestionDao(): MistakeQuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sinav_merkezi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
