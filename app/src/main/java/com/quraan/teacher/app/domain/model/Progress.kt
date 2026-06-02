package com.quraan.teacher.app.domain.model

data class Progress(
    val id: Long = 0,
    val studentId: Long,
    val date: Long,
    val sessionDuration: Int,
    val surahName: String,
    val ayahFrom: Int,
    val ayahTo: Int,
    val memorizedAyahs: Int,
    val reviewedAyahs: Int = 0,
    val mistakesCount: Int = 0,
    val mistakeDetails: List<String> = emptyList(),
    val teacherNotes: String = "",
    val grade: Int = 5,
    val mood: SessionMood = SessionMood.GOOD,
    val revisionNeeded: Boolean = false
)

enum class SessionMood {
    EXCELLENT, GOOD, AVERAGE, POOR;

    fun arabicName(): String = when (this) {
        EXCELLENT -> "ممتاز"
        GOOD -> "جيد"
        AVERAGE -> "متوسط"
        POOR -> "ضعيف"
    }

    fun emoji(): String = when (this) {
        EXCELLENT -> "🌟"
        GOOD -> "👍"
        AVERAGE -> "😐"
        POOR -> "😞"
    }

    companion object {
        fun fromString(value: String): SessionMood = when (value.uppercase()) {
            "EXCELLENT" -> EXCELLENT
            "GOOD" -> GOOD
            "AVERAGE" -> AVERAGE
            "POOR" -> POOR
            else -> GOOD
        }
    }
}
