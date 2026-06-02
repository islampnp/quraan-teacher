package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: Long,
    val questionText: String,
    val options: String,
    val correctAnswer: Int,
    val ayahReference: String = "",
    val points: Int = 1
)
