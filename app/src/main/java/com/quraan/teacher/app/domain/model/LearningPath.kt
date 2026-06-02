package com.quraan.teacher.app.domain.model

data class LearningPath(
    val id: Long = 0,
    val studentId: Long,
    val generatedDate: Long,
    val pathTitle: String,
    val description: String = "",
    val totalWeeks: Int,
    val currentWeek: Int = 1,
    val weeklyGoalAyahs: Int,
    val dailyGoalMinutes: Int = 30,
    val status: PathStatus = PathStatus.ACTIVE,
    val milestones: List<LearningMilestone> = emptyList(),
    val nextRecommendedSurah: String = "",
    val focusAreas: List<String> = emptyList(),
    val adaptationHistory: List<PathAdaptation> = emptyList()
)

enum class PathStatus {
    ACTIVE, COMPLETED, PAUSED;

    fun arabicName(): String = when (this) {
        ACTIVE -> "نشط"
        COMPLETED -> "مكتمل"
        PAUSED -> "متوقف"
    }

    companion object {
        fun fromString(value: String): PathStatus = when (value.uppercase()) {
            "ACTIVE" -> ACTIVE
            "COMPLETED" -> COMPLETED
            "PAUSED" -> PAUSED
            else -> ACTIVE
        }
    }
}

data class LearningMilestone(
    val weekNumber: Int,
    val targetSurah: String,
    val targetAyahFrom: Int,
    val targetAyahTo: Int,
    val targetMemorizedAyahs: Int,
    val targetRevisionAyahs: Int = 0,
    val isCompleted: Boolean = false,
    val completedDate: Long? = null,
    val actualAyahsAchieved: Int = 0,
    val difficultyRating: Int = 0,
    val focusTopic: String = "",
    val hasQuiz: Boolean = false,
    val hasTajweedExercise: Boolean = false
)

data class PathAdaptation(
    val date: Long,
    val reason: String,
    val description: String,
    val changes: String
)
