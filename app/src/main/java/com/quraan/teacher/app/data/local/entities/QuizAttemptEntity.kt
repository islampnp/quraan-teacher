package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val quizId: Long,
    val attemptDate: Long = System.currentTimeMillis(),
    val score: Int,
    val totalPoints: Int,
    val answers: String = "[]",
    val passed: Boolean = false,
    val timeTakenSeconds: Int = 0
)
