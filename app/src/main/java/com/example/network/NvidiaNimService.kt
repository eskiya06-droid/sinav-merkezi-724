package com.example.network

import android.util.Log
import com.example.data.ExamHistory
import com.example.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NvidiaNimService {

    private val apiKey = "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"

    // 1. Soru Üretimi ve Mantık (JSON ve Karmaşık Müfredat)
    private val jsonLogicPool = listOf(
        "meta/llama-3.3-70b-instruct",
        "meta/llama-3.1-70b-instruct",
        "nvidia/nemotron-4-340b-instruct",
        "mistralai/mistral-large"
    )

    // 2. Chat, Öğretmenlik ve Empati (Akıcı Türkçe, Sohbet, İpucu verme)
    private val teacherChatPool = listOf(
        "meta/llama-3.1-70b-instruct",
        "mistralai/mistral-large",
        "google/gemma-3-12b-it",
        "mistralai/mixtral-8x22b-instruct-v0.1",
        "microsoft/phi-3.5-moe-instruct"
    )

    // 3. Veri Analizi ve Karne (Sayısal analiz, Raporlama)
    private val analysisPool = listOf(
        "deepseek-ai/deepseek-v4-pro",
        "nvidia/llama-3.1-nemotron-70b-instruct",
        "ibm/granite-3.0-8b-instruct",
        "qwen/qwen3.5-397b-a17b"
    )

    // 4. Görsel Analiz (Gelecek için: Resimli Soru Okuma / OCR)
    private val visionPool = listOf(
        "meta/llama-3.2-90b-vision-instruct",
        "meta/llama-3.2-11b-vision-instruct",
        "nvidia/nemotron-nano-12b-v2-vl"
    )

    /**
     * Executes the API call and automatically falls back to the next model in the pool if an error occurs.
     */
    private suspend fun executeWithFallback(
        modelPool: List<String>,
        messages: List<NvidiaChatMessage>,
        temperature: Float = 0.7f,
        responseFormat: ResponseFormat? = null
    ): String? {
        val authHeader = "Bearer $apiKey"
        var lastException: Exception? = null

        for (modelName in modelPool) {
            try {
                Log.d("NvidiaNimService", "Trying model: $modelName")
                val request = ChatCompletionRequest(
                    model = modelName,
                    messages = messages,
                    temperature = temperature,
                    response_format = responseFormat
                )

                val response = NvidiaNimApiClient.service.generateContent(authHeader, request)
                val content = response.choices.firstOrNull()?.message?.content
                
                if (!content.isNullOrBlank()) {
                    Log.d("NvidiaNimService", "Success with model: $modelName")
                    return content
                }
            } catch (e: Exception) {
                Log.e("NvidiaNimService", "Model $modelName failed: ${e.message}")
                lastException = e
                // Continue to the next model in the pool
            }
        }
        
        Log.e("NvidiaNimService", "All models failed. Last error: ${lastException?.message}")
        return null
    }

    /**
     * Generates a list of questions for a specific lesson and topic.
     */
    suspend fun generateQuestions(
        lesson: String,
        topic: String,
        count: Int,
        examType: String,
        field: String
    ): List<GeneratedQuestion> = withContext(Dispatchers.IO) {
        val systemPrompt = "You are an AI Question Bank Engine. Output ONLY valid JSON."
        val userPrompt = """
            $examType ($field) sınavı için, $lesson dersinin "$topic" konusundan tam olarak $count adet özgün ve müfredata uygun çoktan seçmeli soru üret.
            Her soru mutlaka 4 şıklı olmalıdır (A, B, C, D) ve Türkçe dilinde yazılmalıdır.
            
            Eğer konu Matematik veya Geometri ise, sorunun görsel canlandırması için isteğe bağlı olarak 'drawingCommands' alanını doldurabilirsin.
            'drawingCommands' değeri, çizim komutlarını içeren geçerli bir JSON string olmalıdır.
            Çizim alanının koordinat sistemi 0 ile 200 arasındadır.
            Komut tipleri: line, circle, rect, triangle, text. Örnek: [{"type": "circle", "cx": 100, "cy": 100, "r": 40}]
            Sözel sorularda 'drawingCommands' alanını null bırak.
            
            Lütfen yanıtını SADECE aşağıda belirtilen JSON şemasına uygun olarak döndür, başka hiçbir açıklama metni ekleme:
            {
              "questions": [
                {
                  "lesson": "$lesson",
                  "topic": "$topic",
                  "questionText": "...",
                  "optionA": "...",
                  "optionB": "...",
                  "optionC": "...",
                  "optionD": "...",
                  "correctAnswer": "A",
                  "explanation": "...",
                  "drawingCommands": "[{\"type\": \"circle\", \"cx\": 100, \"cy\": 100, \"r\": 40}]"
                }
              ]
            }
        """.trimIndent()

        val messages = listOf(
            NvidiaChatMessage("system", systemPrompt),
            NvidiaChatMessage("user", userPrompt)
        )

        // Only some models support strict JSON response_format, we will just rely on the prompt instructing it.
        // If we strictly want json_object, we can pass ResponseFormat("json_object"). 
        // But since we fallback to various models, prompt engineering is safer.
        val resultJson = executeWithFallback(jsonLogicPool, messages, temperature = 0.5f)
        
        if (resultJson != null) {
            try {
                // Parse the JSON manually or via Moshi. Wait, Moshi is already used for GeneratedQuestionList.
                // Let's use Moshi to parse.
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(GeneratedQuestionList::class.java)
                
                // Clean markdown artifacts if the model wraps it in ```json ... ```
                val cleanJson = resultJson.replace("```json", "").replace("```", "").trim()
                val parsed = adapter.fromJson(cleanJson)
                if (parsed != null && parsed.questions.isNotEmpty()) {
                    return@withContext parsed.questions
                }
            } catch (e: Exception) {
                Log.e("NvidiaNimService", "JSON Parsing failed: ${e.message}")
            }
        }
        
        // Fallback
        getFallbackQuestions(lesson, topic, count)
    }

    /**
     * Explains why a user's answer is wrong and the correct solution.
     */
    suspend fun explainMistake(
        questionText: String,
        options: List<String>,
        correctAnswer: String,
        chosenAnswer: String,
        explanation: String
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = "Sen öğrencinin takıldığı sorulardaki kavram kargaşalarını anında çözen, onları motive eden, net ve yalın anlatan cana yakın bir 7/24 AI Öğretmensin."
        val userPrompt = """
            Öğrenci şu soruda hata yaptı veya anlamadı:
            Soru: $questionText
            Şıklar:
            A) ${options.getOrNull(0) ?: ""}
            B) ${options.getOrNull(1) ?: ""}
            C) ${options.getOrNull(2) ?: ""}
            D) ${options.getOrNull(3) ?: ""}
            
            Doğru Cevap: $correctAnswer
            Öğrencinin İşaretlediği Cevap: ${if (chosenAnswer.isEmpty()) "Boş bıraktı" else chosenAnswer}
            
            Soruya ait varsayılan çözüm adımları: $explanation

            Lütfen öğrenciye sevecen, cesaretlendirici ve profesyonel bir öğretmen diliyle (Türkçe):
            1. İşaretlediği yanlış şıkkın (eğer boş bırakmadıysa) neden bir çeldirici olduğunu ve neden yanlış olduğunu açıkla.
            2. Doğru seçeneğe nasıl ulaşılacağını adım adım, son derece anlaşılır bir dille ispatla.
            3. Bu soru tipini bir sonraki sefere doğru çözebilmesi için bir "Taktik/İpucu" ver.
            Metni düzgün paragraflar ve kalın başlıklar içeren güzel bir Markdown formatında sun.
        """.trimIndent()

        val messages = listOf(
            NvidiaChatMessage("system", systemPrompt),
            NvidiaChatMessage("user", userPrompt)
        )

        executeWithFallback(teacherChatPool, messages, temperature = 0.7f)
            ?: "Bağlantı hatası sebebiyle detaylı AI açıklaması oluşturulamadı. \n\nTemel Çözüm: $explanation"
    }

    /**
     * Analyzes overall exam history and provides a comprehensive progress analysis.
     */
    suspend fun generateAnalysisReport(
        profile: UserProfile,
        historyList: List<ExamHistory>
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = "Sen öğrencilerin verilerini titizlikle analiz edip onlara rehberlik eden profesyonel bir rehber öğretmen ve veri analisti yapay zekasın."
        
        val historyText = historyList.take(8).joinToString("\n") { h ->
            "- Başlık: ${h.title}, Tür: ${h.examType}, Doğru: ${h.correctCount}, Yanlış: ${h.wrongCount}, Boş: ${h.blankCount}"
        }

        val userPrompt = """
            Öğrencinin Profil Bilgileri:
            - İsim: ${profile.username}
            - Hedef Sınav: ${profile.targetExam}
            - Alan/Kol: ${profile.field}

            Öğrencinin Çözdüğü Son Deneme/Test Geçmişi:
            $historyText

            Lütfen bu verilere dayanarak öğrenciye özel, derinlemesine ve son derece samimi bir 'Yapay Zeka Karne Analizi' raporu oluştur.
            Raporda şu başlıklar yer almalıdır:
            1. **Genel Başarı Durumu ve Gelişim Eğrisi**: Öğrencinin doğru/yanlış oranına göre performansı nasıl gidiyor?
            2. **Güçlü Olduğu Alanlar ve Dersler**: Hangi konularda oldukça iyi?
            3. **Acil Odaklanması Gereken Zayıf Noktalar**: Hatalarının yoğunlaştığı ders ve konuları tespit et (Örn: "Matematik'te Üslü Sayılar konusunda işlem hatası yapıyorsun, Paragrafta ise olumsuz köklü sorularda takılıyorsun.")
            4. **Özel 2 Günlük Çalışma Reçetesi**: Öğrenciye sonraki 2 gün için ders ve konu bazlı nokta atışı çalışma tavsiyesi ve motivasyon ver.

            Yazım dilin tamamen Türkçe, teşvik edici, cana yakın ve net olmalı. Güzel markdown başlıkları ve madde işaretleri kullan.
        """.trimIndent()

        val messages = listOf(
            NvidiaChatMessage("system", systemPrompt),
            NvidiaChatMessage("user", userPrompt)
        )

        executeWithFallback(analysisPool, messages, temperature = 0.6f)
            ?: "Yapay zeka karne raporu oluşturulamadı. Rapor oluşturulurken bağlantı hatası gerçekleşti."
    }

    /**
     * Chat-based interaction with the 24/7 AI Teacher.
     */
    suspend fun chatWithTeacher(
        chatHistory: List<NvidiaChatMessage>,
        userMessage: String,
        imageBase64: String?,
        profile: UserProfile
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = """
            Sen Sınav Merkezi 724 uygulamasının resmi "7/24 Yapay Zeka Öğretmeni"sin.
            Öğrencinin adı: ${profile.username}, hedeflediği sınav: ${profile.targetExam} ve alanı: ${profile.field}.
            
            Öğrenciyle sınav hazırlık süreçleri, ders çalışma teknikleri, zorlandığı konuların özet anlatımları veya sınav stresiyle baş etme yolları hakkında sohbet etmelisin.
            Bunun dışında eğer öğrenci sana bir DOSYA veya GÖRSEL yolladıysa, onu analiz edip içeriği hakkında detaylı cevap vermelisin.
            Dilin daima cana yakın, destekleyici, motive edici ve bir öğretmen sabrında olmalıdır. Yanıtlarını gereksiz uzatmadan, markdown formatında düzenli ve net bir şekilde sun.
        """.trimIndent()

        if (imageBase64 != null) {
            var lastException: Exception? = null
            val authHeader = "Bearer $apiKey"
            
            val visionMessages = mutableListOf<NvidiaVisionChatMessage>()
            visionMessages.add(NvidiaVisionChatMessage("system", listOf(ContentPart("text", systemPrompt))))
            chatHistory.forEach {
                visionMessages.add(NvidiaVisionChatMessage(it.role, listOf(ContentPart("text", it.content))))
            }
            val parts = listOf(
                ContentPart(type = "text", text = userMessage),
                ContentPart(type = "image_url", image_url = ImageUrl(url = imageBase64))
            )
            visionMessages.add(NvidiaVisionChatMessage("user", parts))

            for (modelName in visionPool) {
                try {
                    Log.d("NvidiaNimService", "Trying vision model: $modelName")
                    val request = VisionChatCompletionRequest(
                        model = modelName,
                        messages = visionMessages,
                        temperature = 0.7f
                    )
                    val response = NvidiaNimApiClient.service.generateVisionContent(authHeader, request)
                    val content = response.choices.firstOrNull()?.message?.content
                    if (!content.isNullOrBlank()) {
                        return@withContext content
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }
            return@withContext "Resim analiz edilirken bir hata oluştu. Lütfen tekrar deneyin."
        } else {
            val messages = mutableListOf<NvidiaChatMessage>()
            messages.add(NvidiaChatMessage("system", systemPrompt))
            messages.addAll(chatHistory)
            messages.add(NvidiaChatMessage("user", userMessage))

            return@withContext executeWithFallback(teacherChatPool, messages, temperature = 0.7f)
                ?: "Bağlantı hatası oluştu. Sorunu çözmek için çalışıyorum. Lütfen daha sonra tekrar deneyin."
        }
    }

    /**
     * Fallback mock questions in Turkish
     */
    fun getFallbackQuestions(lesson: String, topic: String, count: Int): List<GeneratedQuestion> {
        val list = mutableListOf<GeneratedQuestion>()
        for (i in 1..count) {
            list.add(
                GeneratedQuestion(
                    lesson = lesson,
                    topic = topic,
                    questionText = "Bu otomatik üretilmiş yedek sorudur. $topic konusunda AI bağlantısı kurulamadığında çevrimdışı çalışabilmeniz için eklenmiştir. Soru $i:",
                    optionA = "Seçenek A",
                    optionB = "Seçenek B",
                    optionC = "Seçenek C",
                    optionD = "Seçenek D",
                    correctAnswer = "A",
                    explanation = "Çevrimdışı modda otomatik açıklamadır.",
                    drawingCommands = null
                )
            )
        }
        return list
    }
}
