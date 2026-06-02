package com.quraan.teacher.app.data.local.dao

import androidx.room.*
import com.quraan.teacher.app.data.local.entities.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY fullName ASC")
    fun getAllActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE isActive = 1 AND level = :level ORDER BY fullName ASC")
    fun getStudentsByLevel(level: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdOnce(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE isActive = 1 AND fullName LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1 AND level = :level")
    suspend fun getStudentCountByLevel(level: String): Int

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY totalMemorizedAyahs DESC LIMIT 10")
    fun getTopStudentsByMemorization(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)

    @Query("UPDATE students SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)
}
