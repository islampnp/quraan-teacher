package com.quraan.teacher.app.domain.model

data class QuizResult(
    val attemptId: Long,
    val quizId: Long,
    val studentId: Long,
    val score: Int,
    val totalPoints: Int,
    val percentage: Float,
    val passed: Boolean,
    val attemptDate: Long,
    val answers: List<AnswerResult> = emptyList(),
    val timeTakenSeconds: Int = 0
)

data class AnswerResult(
    val questionId: Long,
    val questionText: String,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val pointsPossible: Int
)
