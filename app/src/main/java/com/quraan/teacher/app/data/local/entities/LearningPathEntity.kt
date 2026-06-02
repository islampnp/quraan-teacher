package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_paths")
data class LearningPathEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val generatedDate: Long = System.currentTimeMillis(),
    val pathTitle: String,
    val description: String = "",
    val totalWeeks: Int,
    val currentWeek: Int = 1,
    val weeklyGoalAyahs: Int,
    val dailyGoalMinutes: Int = 30,
    val status: String = "ACTIVE",
    val milestones: String = "[]",
    val nextRecommendedSurah: String = "",
    val focusAreas: String = "[]",
    val adaptationHistory: String = "[]"
)
