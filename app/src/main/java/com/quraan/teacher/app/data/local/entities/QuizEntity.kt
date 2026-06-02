package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val surahName: String,
    val ayahRange: String,
    val totalQuestions: Int,
    val passScore: Int = 70,
    val createdAt: Long = System.currentTimeMillis()
)
