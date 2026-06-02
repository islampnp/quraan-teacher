package com.quraan.teacher.app.data.repository

import com.quraan.teacher.app.data.local.dao.LearningPathDao
import com.quraan.teacher.app.data.local.entities.LearningPathEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningPathRepository @Inject constructor(
    private val learningPathDao: LearningPathDao
) {
    fun getActivePathByStudent(studentId: Long): Flow<LearningPathEntity?> =
        learningPathDao.getActivePathByStudent(studentId)

    fun getPathsByStudent(studentId: Long): Flow<List<LearningPathEntity>> =
        learningPathDao.getPathsByStudent(studentId)

    suspend fun getPathById(id: Long): LearningPathEntity? = learningPathDao.getPathById(id)

    fun getPathByIdFlow(id: Long): Flow<LearningPathEntity?> = learningPathDao.getPathByIdFlow(id)

    suspend fun getActivePathByStudentOnce(studentId: Long): LearningPathEntity? =
        learningPathDao.getActivePathByStudentOnce(studentId)

    suspend fun updateStatus(id: Long, status: String) = learningPathDao.updateStatus(id, status)

    suspend fun updateCurrentWeek(id: Long, week: Int) = learningPathDao.updateCurrentWeek(id, week)

    suspend fun insert(path: LearningPathEntity): Long = learningPathDao.insert(path)

    suspend fun update(path: LearningPathEntity) = learningPathDao.update(path)

    suspend fun delete(path: LearningPathEntity) = learningPathDao.delete(path)
}
