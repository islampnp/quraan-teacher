package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: Long,
    val sessionDuration: Int,
    val surahName: String,
    val ayahFrom: Int,
    val ayahTo: Int,
    val memorizedAyahs: Int,
    val reviewedAyahs: Int = 0,
    val mistakesCount: Int = 0,
    val mistakeDetails: String = "[]",
    val teacherNotes: String = "",
    val grade: Int = 5,
    val mood: String = "GOOD",
    val revisionNeeded: Boolean = false
)
