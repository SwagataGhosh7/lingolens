package com.example.repository

import com.example.data.Flashcard
import com.example.data.FlashcardDao
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.InlineData
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AppRepository(private val flashcardDao: FlashcardDao, private val apiKey: String) {

    val allFlashcards: Flow<List<Flashcard>> = flashcardDao.getAllFlashcards()

    suspend fun insertFlashcard(flashcard: Flashcard) {
        flashcardDao.insertFlashcard(flashcard)
    }

    suspend fun deleteFlashcardById(id: Int) {
        flashcardDao.deleteFlashcardById(id)
    }

    suspend fun translateImage(base64Image: String, targetLanguage: String): TranslationResult {
        val prompt = "Identify the main object in this image. Respond with ONLY a JSON object containing two keys: 'english_word' (the object in English) and 'translated_word' (the object translated into the user's selected language: $targetLanguage). Do not include markdown blocks."
        
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw Exception("No response text")
        
        // Parse the JSON directly
        return try {
            val jsonObject = Json.parseToJsonElement(responseText).jsonObject
            val englishWord = jsonObject["english_word"]?.jsonPrimitive?.content ?: "Unknown"
            val translatedWord = jsonObject["translated_word"]?.jsonPrimitive?.content ?: "Unknown"
            TranslationResult(englishWord, translatedWord, targetLanguage)
        } catch (e: Exception) {
            throw Exception("Failed to parse response: $responseText")
        }
    }
}

data class TranslationResult(
    val englishWord: String,
    val translatedWord: String,
    val targetLanguage: String
)
