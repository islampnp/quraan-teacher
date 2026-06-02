package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_recordings")
data class AudioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val filePath: String,
    val uploadDate: Long,
    val surahName: String,
    val durationSeconds: Int = 0,
    val teacherRating: Int = 0,
    val feedback: String = "",
    val analysisNotes: String = ""
)
