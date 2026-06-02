package com.quraan.teacher.app.domain.usecase

import com.google.gson.Gson
import com.quraan.teacher.app.data.local.entities.LearningPathEntity
import com.quraan.teacher.app.data.repository.LearningPathRepository
import com.quraan.teacher.app.data.repository.ProgressRepository
import com.quraan.teacher.app.data.repository.QuizRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import com.quraan.teacher.app.domain.model.LearningMilestone
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

data class StudentEvaluation(
    val currentLevel: String,
    val memorizedAyahs: Int,
    val averageGrade: Double,
    val sessionFrequency: Int,
    val averageQuizScore: Double,
    val quizPassRate: Double,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendedPace: String,
    val recommendedSurah: String,
    val totalSessions: Int,
    val trend: String
)

class EvaluateStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository,
    private val progressRepository: ProgressRepository,
    private val learningPathRepository: LearningPathRepository,
    private val quizRepository: QuizRepository
) {
    private val gson = Gson()

    suspend operator fun invoke(studentId: Long): StudentEvaluation {
        val student = studentRepository.getStudentByIdOnce(studentId)
            ?: throw IllegalArgumentException("الطالب غير موجود")

        val totalSessions = progressRepository.getTotalSessions(studentId)
        val avgGrade = progressRepository.getAverageGrade(studentId)
        val successfulSessions = progressRepository.getSuccessfulSessions(studentId)
        val avgQuizScore = quizRepository.getAverageQuizScore(studentId)
        val passedQuizzes = quizRepository.getPassedQuizCount(studentId)
        val totalQuizzes = quizRepository.getTotalQuizAttempts(studentId)

        val weakPointsType = object : TypeToken<List<String>>() {}.type
        val weakPoints: List<String> = gson.fromJson(student.weakPoints, weakPointsType) ?: emptyList()
        val strongPoints: List<String> = gson.fromJson(student.strongPoints, weakPointsType) ?: emptyList()

        val sessionFrequency = if (totalSessions > 0) totalSessions / 4 else 0

        val trend = when {
            avgGrade >= 8.0 && successfulSessions.toFloat() / totalSessions.coerceAtLeast(1) > 0.7f -> "ممتاز"
            avgGrade >= 6.0 -> "جيد"
            avgGrade >= 4.0 -> "بحاجة لتحسين"
            else -> "ضعيف"
        }

        val recommendedPace = when {
            avgGrade >= 8.0 && avgQuizScore >= 80 -> "سريع"
            avgGrade >= 6.0 && avgQuizScore >= 60 -> "متوسط"
            else -> "بطيء"
        }

        return StudentEvaluation(
            currentLevel = student.level,
            memorizedAyahs = student.totalMemorizedAyahs,
            averageGrade = avgGrade,
            sessionFrequency = sessionFrequency,
            averageQuizScore = avgQuizScore,
            quizPassRate = if (totalQuizzes > 0) passedQuizzes.toDouble() / totalQuizzes else 0.0,
            strengths = strongPoints,
            weaknesses = weakPoints,
            recommendedPace = recommendedPace,
            recommendedSurah = getNextRecommendedSurah(student.totalMemorizedAyahs),
            totalSessions = totalSessions,
            trend = trend
        )
    }

    private fun getNextRecommendedSurah(totalAyahs: Int): String {
        var cumulative = 0
        for ((surah, ayahs) in GenerateLearningPathUseCase.surahAyahMap) {
            cumulative += ayahs
            if (cumulative > totalAyahs) return surah
        }
        return "الناس"
    }
}
