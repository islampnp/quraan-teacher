package com.quraan.teacher.app.data.repository

import com.quraan.teacher.app.data.local.dao.AudioDao
import com.quraan.teacher.app.data.local.entities.AudioEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor(
    private val audioDao: AudioDao
) {
    fun getAudioByStudent(studentId: Long): Flow<List<AudioEntity>> = audioDao.getAudioByStudent(studentId)
    fun getAllAudio(): Flow<List<AudioEntity>> = audioDao.getAllAudio()
    suspend fun getAudioById(id: Long): AudioEntity? = audioDao.getAudioById(id)
    suspend fun insert(audio: AudioEntity): Long = audioDao.insert(audio)
    suspend fun update(audio: AudioEntity) = audioDao.update(audio)
    suspend fun delete(audio: AudioEntity) = audioDao.delete(audio)
}
