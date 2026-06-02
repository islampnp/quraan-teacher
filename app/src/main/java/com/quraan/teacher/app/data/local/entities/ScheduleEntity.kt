package com.quraan.teacher.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subject: String = "",
    val sessionType: String,
    val isRecurring: Boolean = true,
    val notificationEnabled: Boolean = true,
    val notes: String = ""
)
