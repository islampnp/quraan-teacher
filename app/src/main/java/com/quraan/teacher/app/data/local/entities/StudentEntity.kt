package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val age: Int,
    val phone: String,
    val guardianName: String,
    val guardianPhone: String,
    val level: String,
    val enrollmentDate: Long,
    val isActive: Boolean = true,
    val totalMemorizedAyahs: Int = 0,
    val currentSurah: String = "",
    val currentAyah: Int = 0,
    val learningStyle: String = "MIXED",
    val weakPoints: String = "[]",
    val strongPoints: String = "[]",
    val averageSessionDuration: Int = 30,
    val preferredSessionTime: String = "MORNING"
)
