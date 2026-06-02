package com.quraan.teacher.app.data.local.dao

import androidx.room.*
import com.quraan.teacher.app.data.local.entities.AudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {
    @Query("SELECT * FROM audio_recordings WHERE studentId = :studentId ORDER BY uploadDate DESC")
    fun getAudioByStudent(studentId: Long): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio_recordings ORDER BY uploadDate DESC")
    fun getAllAudio(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio_recordings WHERE id = :id")
    suspend fun getAudioById(id: Long): AudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audio: AudioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audio: List<AudioEntity>)

    @Update
    suspend fun update(audio: AudioEntity)

    @Delete
    suspend fun delete(audio: AudioEntity)
}
