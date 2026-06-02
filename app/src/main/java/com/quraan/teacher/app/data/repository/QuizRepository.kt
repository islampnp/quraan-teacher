package com.quraan.teacher.app.data.repository

import com.quraan.teacher.app.data.local.dao.QuizDao
import com.quraan.teacher.app.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val quizDao: QuizDao
) {
    fun getAllQuizzes(): Flow<List<QuizEntity>> = quizDao.getAllQuizzes()

    suspend fun getQuizById(id: Long): QuizEntity? = quizDao.getQuizById(id)

    fun getQuestionsByQuiz(quizId: Long): Flow<List<QuestionEntity>> = quizDao.getQuestionsByQuiz(quizId)

    suspend fun getQuestionsByQuizOnce(quizId: Long): List<QuestionEntity> = quizDao.getQuestionsByQuizOnce(quizId)

    fun getAttemptsByStudent(studentId: Long): Flow<List<QuizAttemptEntity>> = quizDao.getAttemptsByStudent(studentId)

    fun getAttemptsByQuiz(quizId: Long): Flow<List<QuizAttemptEntity>> = quizDao.getAttemptsByQuiz(quizId)

    suspend fun getAttemptById(id: Long): QuizAttemptEntity? = quizDao.getAttemptById(id)

    suspend fun getAverageQuizScore(studentId: Long): Double = quizDao.getAverageQuizScore(studentId) ?: 0.0

    suspend fun getPassedQuizCount(studentId: Long): Int = quizDao.getPassedQuizCount(studentId)

    suspend fun getTotalQuizAttempts(studentId: Long): Int = quizDao.getTotalQuizAttempts(studentId)

    suspend fun insertQuiz(quiz: QuizEntity): Long = quizDao.insertQuiz(quiz)

    suspend fun insertQuestion(question: QuestionEntity): Long = quizDao.insertQuestion(question)

    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long = quizDao.insertAttempt(attempt)

    suspend fun updateQuiz(quiz: QuizEntity) = quizDao.updateQuiz(quiz)

    suspend fun updateAttempt(attempt: QuizAttemptEntity) = quizDao.updateAttempt(attempt)
}
