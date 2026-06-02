package com.quraan.teacher.app.data.repository

import com.quraan.teacher.app.data.local.dao.ProgressDao
import com.quraan.teacher.app.data.local.entities.ProgressEntity
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao
) {
    fun getProgressByStudent(studentId: Long): Flow<List<ProgressEntity>> =
        progressDao.getProgressByStudent(studentId)

    fun getRecentProgressByStudent(studentId: Long): Flow<List<ProgressEntity>> =
        progressDao.getRecentProgressByStudent(studentId)

    fun getRecentProgressAll(): Flow<List<ProgressEntity>> =
        progressDao.getRecentProgressAll()

    fun getLastFiveSessions(studentId: Long): Flow<List<ProgressEntity>> =
        progressDao.getLastFiveSessions(studentId)

    suspend fun getTotalMemorizedInRange(fromDate: Long, toDate: Long): Int =
        progressDao.getTotalMemorizedInRange(fromDate, toDate) ?: 0

    suspend fun getProgressInRange(fromDate: Long, toDate: Long): List<ProgressEntity> =
        progressDao.getProgressInRange(fromDate, toDate)

    suspend fun getDistinctStudentCountInRange(fromDate: Long, toDate: Long): Int =
        progressDao.getDistinctStudentCountInRange(fromDate, toDate)

    suspend fun getAverageGrade(studentId: Long): Double =
        progressDao.getAverageGrade(studentId) ?: 0.0

    suspend fun getSuccessfulSessions(studentId: Long): Int =
        progressDao.getSuccessfulSessions(studentId)

    suspend fun getTotalSessions(studentId: Long): Int =
        progressDao.getTotalSessions(studentId)

    suspend fun insert(progress: ProgressEntity): Long = progressDao.insert(progress)

    suspend fun update(progress: ProgressEntity) = progressDao.update(progress)

    suspend fun delete(progress: ProgressEntity) = progressDao.delete(progress)

    suspend fun getMonthlyAyahsForStudent(studentId: Long, monthsBack: Int = 6): List<Pair<String, Int>> {
        val calendar = Calendar.getInstance()
        val result = mutableListOf<Pair<String, Int>>()
        for (i in monthsBack - 1 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val monthEnd = calendar.timeInMillis
            val total = progressDao.getTotalMemorizedInRange(monthStart, monthEnd) ?: 0
            val monthNames = arrayOf(
                "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
            )
            val cal = Calendar.getInstance()
            cal.timeInMillis = monthStart
            result.add(Pair(monthNames[cal.get(Calendar.MONTH)], total))
        }
        return result
    }
}
