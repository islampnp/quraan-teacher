package com.quraan.teacher.app.domain.model

data class Student(
    val id: Long = 0,
    val fullName: String,
    val age: Int,
    val phone: String,
    val guardianName: String,
    val guardianPhone: String,
    val level: StudentLevel,
    val enrollmentDate: Long,
    val isActive: Boolean = true,
    val totalMemorizedAyahs: Int = 0,
    val currentSurah: String = "",
    val currentAyah: Int = 0,
    val learningStyle: LearningStyle = LearningStyle.MIXED,
    val weakPoints: List<String> = emptyList(),
    val strongPoints: List<String> = emptyList(),
    val averageSessionDuration: Int = 30,
    val preferredSessionTime: SessionTime = SessionTime.MORNING
)

enum class StudentLevel {
    BEGINNER, INTERMEDIATE, ADVANCED;

    fun arabicName(): String = when (this) {
        BEGINNER -> "مبتدئ"
        INTERMEDIATE -> "متوسط"
        ADVANCED -> "متقدم"
    }

    companion object {
        fun fromString(value: String): StudentLevel = when (value.uppercase()) {
            "BEGINNER" -> BEGINNER
            "INTERMEDIATE" -> INTERMEDIATE
            "ADVANCED" -> ADVANCED
            else -> BEGINNER
        }
    }
}

enum class LearningStyle {
    VISUAL, AUDITORY, REPETITIVE, MIXED;

    fun arabicName(): String = when (this) {
        VISUAL -> "بصري"
        AUDITORY -> "سمعي"
        REPETITIVE -> "تكرار"
        MIXED -> "مختلط"
    }

    companion object {
        fun fromString(value: String): LearningStyle = when (value.uppercase()) {
            "VISUAL" -> VISUAL
            "AUDITORY" -> AUDITORY
            "REPETITIVE" -> REPETITIVE
            "MIXED" -> MIXED
            else -> MIXED
        }
    }
}

enum class SessionTime {
    MORNING, AFTERNOON, EVENING;

    fun arabicName(): String = when (this) {
        MORNING -> "صباحية"
        AFTERNOON -> "بعد الظهر"
        EVENING -> "مسائية"
    }

    companion object {
        fun fromString(value: String): SessionTime = when (value.uppercase()) {
            "MORNING" -> MORNING
            "AFTERNOON" -> AFTERNOON
            "EVENING" -> EVENING
            else -> MORNING
        }
    }
}
