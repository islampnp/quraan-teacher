package com.quraan.teacher.app.domain.usecase

import com.quraan.teacher.app.data.local.entities.ScheduleEntity
import com.quraan.teacher.app.data.repository.ScheduleRepository
import javax.inject.Inject

data class SessionInput(
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

class ScheduleSessionUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(input: SessionInput): Result<Long> = runCatching {
        val entity = ScheduleEntity(
            studentId = input.studentId,
            dayOfWeek = input.dayOfWeek,
            startTime = input.startTime,
            endTime = input.endTime,
            subject = input.subject,
            sessionType = input.sessionType,
            isRecurring = input.isRecurring,
            notificationEnabled = input.notificationEnabled,
            notes = input.notes
        )
        scheduleRepository.insert(entity)
    }

    suspend fun updateSchedule(schedule: ScheduleEntity): Result<Unit> = runCatching {
        scheduleRepository.update(schedule)
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity): Result<Unit> = runCatching {
        scheduleRepository.delete(schedule)
    }
}
