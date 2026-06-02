package com.quraan.teacher.app.data.local.dao

import androidx.room.*
import com.quraan.teacher.app.data.local.entities.LearningPathEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningPathDao {
    @Query("SELECT * FROM learning_paths WHERE studentId = :studentId AND status = 'ACTIVE' LIMIT 1")
    fun getActivePathByStudent(studentId: Long): Flow<LearningPathEntity?>

    @Query("SELECT * FROM learning_paths WHERE studentId = :studentId ORDER BY generatedDate DESC")
    fun getPathsByStudent(studentId: Long): Flow<List<LearningPathEntity>>

    @Query("SELECT * FROM learning_paths WHERE id = :id")
    suspend fun getPathById(id: Long): LearningPathEntity?

    @Query("SELECT * FROM learning_paths WHERE id = :id")
    fun getPathByIdFlow(id: Long): Flow<LearningPathEntity?>

    @Query("SELECT * FROM learning_paths WHERE studentId = :studentId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActivePathByStudentOnce(studentId: Long): LearningPathEntity?

    @Query("UPDATE learning_paths SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE learning_paths SET currentWeek = :week WHERE id = :id")
    suspend fun updateCurrentWeek(id: Long, week: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(path: LearningPathEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(paths: List<LearningPathEntity>)

    @Update
    suspend fun update(path: LearningPathEntity)

    @Delete
    suspend fun delete(path: LearningPathEntity)
}
