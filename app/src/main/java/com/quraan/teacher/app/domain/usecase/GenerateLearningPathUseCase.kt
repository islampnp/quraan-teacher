package com.quraan.teacher.app.domain.usecase

import com.google.gson.Gson
import com.quraan.teacher.app.data.local.entities.LearningPathEntity
import com.quraan.teacher.app.data.repository.LearningPathRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import com.quraan.teacher.app.domain.model.LearningMilestone
import com.quraan.teacher.app.domain.model.SessionTime
import javax.inject.Inject

data class PathGeneratorInput(
    val studentId: Long,
    val weaknesses: List<String> = emptyList(),
    val learningPace: String = "متوسط",
    val availableDaysPerWeek: Int = 5,
    val sessionDurationMinutes: Int = 30,
    val priority: String = "متوازن",
    val targetSurah: String = "",
    val targetWeeks: Int = 12,
    val revisionRatio: Int = 30,
    val includeTajweed: Boolean = true,
    val includeWeeklyQuiz: Boolean = true
)

class GenerateLearningPathUseCase @Inject constructor(
    private val learningPathRepository: LearningPathRepository,
    private val studentRepository: StudentRepository
) {
    private val gson = Gson()

    suspend operator fun invoke(input: PathGeneratorInput): Result<LearningPathEntity> = runCatching {
        val student = studentRepository.getStudentByIdOnce(input.studentId)
            ?: throw IllegalArgumentException("الطالب غير موجود")

        val paceMultiplier = when (input.learningPace) {
            "بطيء" -> 0.7
            "سريع" -> 1.3
            else -> 1.0
        }

        val surahAyahs = getSurahAyahCount(input.targetSurah)
        val totalAyahs = surahAyahs
        var weeklyNewAyahs = (totalAyahs.toDouble() / input.targetWeeks.toDouble()) * paceMultiplier
        val dailyNewAyahs = weeklyNewAyahs / input.availableDaysPerWeek.coerceAtLeast(1)

        val revisionAyahsPerWeek = (weeklyNewAyahs * (input.revisionRatio / 100.0)).toInt()
        weeklyNewAyahs = weeklyNewAyahs - revisionAyahsPerWeek

        val milestones = mutableListOf<LearningMilestone>()
        val surahParts = splitSurahIntoParts(input.targetSurah, surahAyahs, input.targetWeeks)

        for (week in 1..input.targetWeeks) {
            val (ayahFrom, ayahTo) = if (week <= surahParts.size) surahParts[week - 1] else Pair(1, 1)
            val targetMemorized = if (week <= surahParts.size) {
                (surahParts[week - 1].second - surahParts[week - 1].first + 1).coerceAtMost(weeklyNewAyahs.toInt())
            } else {
                weeklyNewAyahs.toInt()
            }

            val hasTajweed = input.includeTajweed && week % 3 == 0
            val hasQuiz = input.includeWeeklyQuiz && week % 2 == 0

            val focusTopic = if (hasTajweed) {
                getTajweedTopic(week, input.weaknesses)
            } else ""

            milestones.add(
                LearningMilestone(
                    weekNumber = week,
                    targetSurah = input.targetSurah,
                    targetAyahFrom = ayahFrom,
                    targetAyahTo = ayahTo,
                    targetMemorizedAyahs = targetMemorized.coerceAtLeast(1),
                    targetRevisionAyahs = revisionAyahsPerWeek,
                    hasQuiz = hasQuiz,
                    hasTajweedExercise = hasTajweed,
                    focusTopic = focusTopic
                )
            )
        }

        if (input.weaknesses.isNotEmpty()) {
            for (i in 0 until minOf(2, milestones.size)) {
                milestones[i] = milestones[i].copy(
                    focusTopic = "تمرين تجويد: ${input.weaknesses.joinToString("، ")}",
                    hasTajweedExercise = true
                )
            }
        }

        val title = "مسار حفظ $input.targetSurah"
        val description = "خطة حفظ مخصصة للطالب $input.targetSurah - ${input.targetWeeks} أسبوع"
        val focusAreas = mutableListOf<String>()
        if (input.includeTajweed) focusAreas.add("التجويد")
        if (input.includeWeeklyQuiz) focusAreas.add("الاختبارات الأسبوعية")
        focusAreas.add("الحفظ الجديد")
        if (input.revisionRatio > 0) focusAreas.add("المراجعة")

        // Deactivate previous active paths
        val existingPaths = learningPathRepository.getActivePathByStudentOnce(input.studentId)
        existingPaths?.let {
            learningPathRepository.updateStatus(it.id, "PAUSED")
        }

        val pathEntity = LearningPathEntity(
            studentId = input.studentId,
            generatedDate = System.currentTimeMillis(),
            pathTitle = title,
            description = description,
            totalWeeks = input.targetWeeks,
            currentWeek = 1,
            weeklyGoalAyahs = weeklyNewAyahs.toInt().coerceAtLeast(1),
            dailyGoalMinutes = input.sessionDurationMinutes,
            status = "ACTIVE",
            milestones = gson.toJson(milestones),
            nextRecommendedSurah = input.targetSurah,
            focusAreas = gson.toJson(focusAreas),
            adaptationHistory = "[]"
        )

        val pathId = learningPathRepository.insert(pathEntity)
        pathEntity.copy(id = pathId)
    }

    private fun getSurahAyahCount(surahName: String): Int {
        return surahAyahMap[surahName] ?: 20
    }

    private fun splitSurahIntoParts(surah: String, totalAyahs: Int, weeks: Int): List<Pair<Int, Int>> {
        if (weeks <= 0) return listOf(Pair(1, totalAyahs))
        val ayahsPerWeek = totalAyahs / weeks
        val parts = mutableListOf<Pair<Int, Int>>()
        var start = 1
        for (i in 1..weeks) {
            var end = start + ayahsPerWeek - 1
            if (i == weeks) end = totalAyahs
            if (start <= totalAyahs) {
                parts.add(Pair(start, minOf(end, totalAyahs)))
            }
            start = end + 1
        }
        return parts
    }

    private fun getTajweedTopic(week: Int, weaknesses: List<String>): String {
        val topics = listOf(
            "أحكام النون الساكنة والتنوين", "المدود", "الغنّة",
            "الإدغام", "الإخفاء", "القلقلة", "الوقف والابتداء", "مخارج الحروف"
        )
        if (weaknesses.isNotEmpty()) {
            return weaknesses[week % weaknesses.size]
        }
        return topics[week % topics.size]
    }

    companion object {
        val surahAyahMap = mapOf(
            "الفاتحة" to 7, "البقرة" to 286, "آل عمران" to 200, "النساء" to 176,
            "المائدة" to 120, "الأنعام" to 165, "الأعراف" to 206, "الأنفال" to 75,
            "التوبة" to 129, "يونس" to 109, "هود" to 123, "يوسف" to 111,
            "الرعد" to 43, "إبراهيم" to 52, "الحجر" to 99, "النحل" to 128,
            "الإسراء" to 111, "الكهف" to 110, "مريم" to 98, "طه" to 135,
            "الأنبياء" to 112, "الحج" to 78, "المؤمنون" to 118, "النور" to 64,
            "الفرقان" to 77, "الشعراء" to 227, "النمل" to 93, "القصص" to 88,
            "العنكبوت" to 69, "الروم" to 60, "لقمان" to 34, "السجدة" to 30,
            "الأحزاب" to 73, "سبأ" to 54, "فاطر" to 45, "يس" to 83,
            "الصافات" to 182, "ص" to 88, "الزمر" to 75, "غافر" to 85,
            "فصلت" to 54, "الشورى" to 53, "الزخرف" to 89, "الدخان" to 59,
            "الجاثية" to 37, "الأحقاف" to 35, "محمد" to 38, "الفتح" to 29,
            "الحجرات" to 18, "ق" to 45, "الذاريات" to 60, "الطور" to 49,
            "النجم" to 62, "القمر" to 55, "الرحمن" to 78, "الواقعة" to 96,
            "الحديد" to 29, "المجادلة" to 22, "الحشر" to 24, "الممتحنة" to 13,
            "الصف" to 14, "الجمعة" to 11, "المنافقون" to 11, "التغابن" to 18,
            "الطلاق" to 12, "التحريم" to 12, "الملك" to 30, "القلم" to 52,
            "الحاقة" to 52, "المعارج" to 44, "نوح" to 28, "الجن" to 28,
            "المزمل" to 20, "المدثر" to 56, "القيامة" to 40, "الإنسان" to 31,
            "المرسلات" to 50, "النبأ" to 40, "النازعات" to 46, "عبس" to 42,
            "التكوير" to 29, "الانفطار" to 19, "المطففين" to 36, "الانشقاق" to 25,
            "البروج" to 22, "الطارق" to 17, "الأعلى" to 19, "الغاشية" to 26,
            "الفجر" to 30, "البلد" to 20, "الشمس" to 15, "الليل" to 21,
            "الضحى" to 11, "الشرح" to 8, "التين" to 8, "العلق" to 19,
            "القدر" to 5, "البينة" to 8, "الزلزلة" to 8, "العاديات" to 11,
            "القارعة" to 11, "التكاثر" to 8, "العصر" to 3, "الهمزة" to 9,
            "الفيل" to 5, "قريش" to 4, "الماعون" to 7, "الكوثر" to 3,
            "الكافرون" to 6, "النصر" to 3, "المسد" to 5, "الإخلاص" to 4,
            "الفلق" to 5, "الناس" to 6
        )
    }
}
