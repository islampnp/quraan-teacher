package com.quraan.teacher.app.data.repository

import com.quraan.teacher.app.data.local.dao.ScheduleDao
import com.quraan.teacher.app.data.local.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    fun getSchedulesByStudent(studentId: Long): Flow<List<ScheduleEntity>> =
        scheduleDao.getSchedulesByStudent(studentId)

    fun getSchedulesByDay(dayOfWeek: Int): Flow<List<ScheduleEntity>> =
        scheduleDao.getSchedulesByDay(dayOfWeek)

    fun getAllSchedules(): Flow<List<ScheduleEntity>> =
        scheduleDao.getAllSchedules()

    suspend fun getScheduleCountByDay(dayOfWeek: Int): Int =
        scheduleDao.getScheduleCountByDay(dayOfWeek)

    suspend fun getScheduleById(id: Long): ScheduleEntity? =
        scheduleDao.getScheduleById(id)

    suspend fun getSchedulesWithNotifications(): List<ScheduleEntity> =
        scheduleDao.getSchedulesWithNotifications()

    suspend fun insert(schedule: ScheduleEntity): Long = scheduleDao.insert(schedule)

    suspend fun update(schedule: ScheduleEntity) = scheduleDao.update(schedule)

    suspend fun delete(schedule: ScheduleEntity) = scheduleDao.delete(schedule)
}
