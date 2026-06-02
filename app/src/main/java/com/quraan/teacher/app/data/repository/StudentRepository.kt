package com.quraan.teacher.app.data.repository

import com.quraan.teacher.app.data.local.dao.StudentDao
import com.quraan.teacher.app.data.local.entities.StudentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val studentDao: StudentDao
) {
    fun getAllActiveStudents(): Flow<List<StudentEntity>> = studentDao.getAllActiveStudents()

    fun getStudentsByLevel(level: String): Flow<List<StudentEntity>> = studentDao.getStudentsByLevel(level)

    fun getStudentById(id: Long): Flow<StudentEntity?> = studentDao.getStudentById(id)

    suspend fun getStudentByIdOnce(id: Long): StudentEntity? = studentDao.getStudentByIdOnce(id)

    fun searchStudents(query: String): Flow<List<StudentEntity>> = studentDao.searchStudents(query)

    fun getStudentCount(): Flow<Int> = studentDao.getStudentCount()

    suspend fun getStudentCountByLevel(level: String): Int = studentDao.getStudentCountByLevel(level)

    fun getTopStudentsByMemorization(): Flow<List<StudentEntity>> = studentDao.getTopStudentsByMemorization()

    suspend fun insert(student: StudentEntity): Long = studentDao.insert(student)

    suspend fun update(student: StudentEntity) = studentDao.update(student)

    suspend fun delete(student: StudentEntity) = studentDao.delete(student)

    suspend fun softDelete(id: Long) = studentDao.softDelete(id)
}
