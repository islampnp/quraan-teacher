package com.quraan.teacher.app.data.local.dao

import androidx.room.*
import com.quraan.teacher.app.data.local.entities.QuizEntity
import com.quraan.teacher.app.data.local.entities.QuestionEntity
import com.quraan.teacher.app.data.local.entities.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    suspend fun getQuizById(id: Long): QuizEntity?

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    fun getQuestionsByQuiz(quizId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestionsByQuizOnce(quizId: Long): List<QuestionEntity>

    @Query("SELECT * FROM quiz_attempts WHERE studentId = :studentId ORDER BY attemptDate DESC")
    fun getAttemptsByStudent(studentId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId ORDER BY attemptDate DESC")
    fun getAttemptsByQuiz(quizId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    suspend fun getAttemptById(id: Long): QuizAttemptEntity?

    @Query("SELECT AVG(CAST(score AS FLOAT) / CAST(totalPoints AS FLOAT)) * 100 FROM quiz_attempts WHERE studentId = :studentId")
    suspend fun getAverageQuizScore(studentId: Long): Double?

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE studentId = :studentId AND passed = 1")
    suspend fun getPassedQuizCount(studentId: Long): Int

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE studentId = :studentId")
    suspend fun getTotalQuizAttempts(studentId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuizzes(quizzes: List<QuizEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttempts(attempts: List<QuizAttemptEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Update
    suspend fun updateAttempt(attempt: QuizAttemptEntity)
}
