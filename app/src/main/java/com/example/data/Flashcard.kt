package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val englishWord: String,
    val translatedWord: String,
    val targetLanguage: String,
    val timestamp: Long = System.currentTimeMillis()
)
