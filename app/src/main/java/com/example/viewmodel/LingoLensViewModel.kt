package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.Flashcard
import com.example.repository.AppRepository
import com.example.repository.TranslationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizStats(
    val correctGuesses: Int = 0,
    val totalGuesses: Int = 0,
    val quizzesCompleted: Int = 0
) {
    val accuracyPercentage: Int
        get() = if (totalGuesses > 0) ((correctGuesses.toFloat() / totalGuesses) * 100).toInt() else 0
}

class LingoLensViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val sharedPreferences = application.getSharedPreferences("lingolens_prefs", Context.MODE_PRIVATE)

    val flashcards: StateFlow<List<Flashcard>>

    private val _quizStats = MutableStateFlow(loadQuizStats())
    val quizStats: StateFlow<QuizStats> = _quizStats.asStateFlow()

    init {
        val flashcardDao = AppDatabase.getDatabase(application).flashcardDao()
        val apiKey = BuildConfig.GEMINI_API_KEY
        repository = AppRepository(flashcardDao, apiKey)
        flashcards = repository.allFlashcards.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun loadQuizStats(): QuizStats {
        val correct = sharedPreferences.getInt("correct_guesses", 0)
        val total = sharedPreferences.getInt("total_guesses", 0)
        val quizzes = sharedPreferences.getInt("quizzes_completed", 0)
        return QuizStats(correctGuesses = correct, totalGuesses = total, quizzesCompleted = quizzes)
    }

    fun recordQuizGuess(isCorrect: Boolean) {
        val current = _quizStats.value
        val newCorrect = if (isCorrect) current.correctGuesses + 1 else current.correctGuesses
        val newTotal = current.totalGuesses + 1
        sharedPreferences.edit()
            .putInt("correct_guesses", newCorrect)
            .putInt("total_guesses", newTotal)
            .apply()
        _quizStats.value = current.copy(correctGuesses = newCorrect, totalGuesses = newTotal)
    }

    fun recordQuizCompleted() {
        val current = _quizStats.value
        val newQuizzes = current.quizzesCompleted + 1
        sharedPreferences.edit()
            .putInt("quizzes_completed", newQuizzes)
            .apply()
        _quizStats.value = current.copy(quizzesCompleted = newQuizzes)
    }

    fun resetStats() {
        sharedPreferences.edit().clear().apply()
        _quizStats.value = QuizStats()
    }

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    private val _targetLanguage = MutableStateFlow("Spanish")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
    }

    fun translateImage(base64Image: String) {
        _translationState.value = TranslationState.Loading
        viewModelScope.launch {
            try {
                val result = repository.translateImage(base64Image, _targetLanguage.value)
                _translationState.value = TranslationState.Success(result)
            } catch (e: Exception) {
                _translationState.value = TranslationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveCurrentTranslation() {
        val currentState = _translationState.value
        if (currentState is TranslationState.Success) {
            viewModelScope.launch {
                repository.insertFlashcard(
                    Flashcard(
                        englishWord = currentState.result.englishWord,
                        translatedWord = currentState.result.translatedWord,
                        targetLanguage = currentState.result.targetLanguage
                    )
                )
                // Clear the state after saving
                _translationState.value = TranslationState.Idle
            }
        }
    }
    
    fun dismissTranslation() {
        _translationState.value = TranslationState.Idle
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcardById(flashcard.id)
        }
    }
}

sealed class TranslationState {
    object Idle : TranslationState()
    object Loading : TranslationState()
    data class Success(val result: TranslationResult) : TranslationState()
    data class Error(val message: String) : TranslationState()
}
