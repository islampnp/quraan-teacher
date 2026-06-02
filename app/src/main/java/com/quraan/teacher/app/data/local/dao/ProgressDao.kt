package com.quraan.teacher.app.data.local.dao

import androidx.room.*
import com.quraan.teacher.app.data.local.entities.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE studentId = :studentId ORDER BY date DESC")
    fun getProgressByStudent(studentId: Long): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE studentId = :studentId ORDER BY date DESC LIMIT 10")
    fun getRecentProgressByStudent(studentId: Long): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress ORDER BY date DESC LIMIT 10")
    fun getRecentProgressAll(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE studentId = :studentId ORDER BY date DESC LIMIT 5")
    fun getLastFiveSessions(studentId: Long): Flow<List<ProgressEntity>>

    @Query("SELECT SUM(memorizedAyahs) FROM progress WHERE date >= :fromDate AND date <= :toDate")
    suspend fun getTotalMemorizedInRange(fromDate: Long, toDate: Long): Int?

    @Query("SELECT * FROM progress WHERE date >= :fromDate AND date <= :toDate")
    suspend fun getProgressInRange(fromDate: Long, toDate: Long): List<ProgressEntity>

    @Query("SELECT COUNT(DISTINCT studentId) FROM progress WHERE date >= :fromDate AND date <= :toDate")
    suspend fun getDistinctStudentCountInRange(fromDate: Long, toDate: Long): Int

    @Query("SELECT AVG(grade) FROM progress WHERE studentId = :studentId")
    suspend fun getAverageGrade(studentId: Long): Double?

    @Query("SELECT COUNT(*) FROM progress WHERE studentId = :studentId AND grade >= 7")
    suspend fun getSuccessfulSessions(studentId: Long): Int

    @Query("SELECT COUNT(*) FROM progress WHERE studentId = :studentId")
    suspend fun getTotalSessions(studentId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: ProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progress: List<ProgressEntity>)

    @Update
    suspend fun update(progress: ProgressEntity)

    @Delete
    suspend fun delete(progress: ProgressEntity)
}
