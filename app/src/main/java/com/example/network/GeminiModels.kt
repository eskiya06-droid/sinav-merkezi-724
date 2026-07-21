package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

// --- Custom Parsed JSON Classes for our Exam App ---

@JsonClass(generateAdapter = true)
data class GeneratedQuestionList(
    @Json(name = "questions") val questions: List<GeneratedQuestion>
)

@JsonClass(generateAdapter = true)
data class GeneratedQuestion(
    @Json(name = "lesson") val lesson: String,
    @Json(name = "topic") val topic: String,
    @Json(name = "questionText") val questionText: String,
    @Json(name = "optionA") val optionA: String,
    @Json(name = "optionB") val optionB: String,
    @Json(name = "optionC") val optionC: String,
    @Json(name = "optionD") val optionD: String,
    @Json(name = "correctAnswer") val correctAnswer: String, // "A", "B", "C", "D"
    @Json(name = "explanation") val explanation: String,      // Step-by-step logic
    @Json(name = "drawingCommands") val drawingCommands: String? = null // Coordinates/instructions for visual drawing if applicable
)
