package com.quraan.teacher.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.quraan.teacher.app.data.local.AppDatabase
import com.quraan.teacher.app.data.local.entities.*
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.random.Random

object SeedData {

    private const val SEED_FLAG = "seed_data_complete"
    private val gson = Gson()

    private val arabicNames = listOf(
        "أحمد محمد", "محمد علي", "عمر حسن", "خالد عبدالله", "يوسف إبراهيم",
        "عبدالله عمر", "محمود سعيد", "حسن علي", "علي حسين", "إبراهيم أحمد",
        "سالم راشد", "ناصر عبدالرحمن", "عمرو خالد", "بدر ناصر", "سعيد أحمد",
        "كريم حسن", "طارق محمود", "ماجد عبدالله", "هاني محمد", "أيمن علي",
        "زياد عمر", "معاذ خالد", "رامي إبراهيم", "أحمد يوسف", "محمد خالد",
        "ياسر عبدالله", "وليد سعيد", "نور الدين حسن", "جمال علي", "بسام محمد",
        "عبدالرحمن عمر", "أسامة خالد", "أكرم محمود", "مهدي ناصر", "حاتم أحمد",
        "فهد عبدالله", "عبدالعزيز محمد", "صالح علي", "راشد عمر", "شريف إبراهيم"
    )

    private val surahNames30th = listOf(
        "النبأ", "النازعات", "عبس", "التكوير", "الانفطار", "المطففين",
        "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر",
        "البلد", "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق",
        "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر",
        "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر",
        "الكافرون", "النصر", "المسد", "الإخلاص", "الفلق", "الناس"
    )

    private val surahNamesMiddle = listOf(
        "الملك", "القلم", "الحاقة", "المعارج", "نوح", "الجن",
        "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات"
    )

    private val surahNamesLong = listOf(
        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام",
        "الأعراف", "الأنفال", "التوبة", "يونس", "هود", "يوسف",
        "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف",
        "مريم", "طه"
    )

    private val tajweedRules = listOf(
        "مخارج الحروف", "المد", "الغنة", "الإدغام", "الإخفاء", "القلقلة", "الوقف والابتداء"
    )

    private val strongPointsList = listOf(
        "النطق الصحيح للحروف", "الطلاقة في القراءة", "سرعة الحفظ",
        "قوة الذاكرة", "فهم المعاني", "الالتزام بالمراجعة",
        "حسن الصوت", "الإتقان في التجويد"
    )

    private val sessionMoods = listOf("EXCELLENT", "GOOD", "AVERAGE", "POOR")
    private val learningStyles = listOf("VISUAL", "AUDITORY", "REPETITIVE", "MIXED")
    private val sessionTimes = listOf("MORNING", "AFTERNOON", "EVENING")

    private val starterSurahs = surahNames30th + surahNamesMiddle.take(3)
    private val midSurahs = surahNamesMiddle + surahNamesLong.take(5)
    private val advancedSurahs = surahNamesLong

    suspend fun seedDatabase(context: Context, database: AppDatabase) {
        val prefs: SharedPreferences = context.getSharedPreferences("quraan_seed", Context.MODE_PRIVATE)
        if (prefs.getBoolean(SEED_FLAG, false)) return

        val studentDao = database.studentDao()
        val progressDao = database.progressDao()
        val scheduleDao = database.scheduleDao()
        val audioDao = database.audioDao()
        val quizDao = database.quizDao()
        val pathDao = database.learningPathDao()

        val allStudents = mutableListOf<StudentEntity>()
        val allProgress = mutableListOf<ProgressEntity>()
        val allPaths = mutableListOf<LearningPathEntity>()
        val allSchedules = mutableListOf<ScheduleEntity>()
        val allAudio = mutableListOf<AudioEntity>()
        val allQuizzes = mutableListOf<QuizEntity>()
        val allQuestions = mutableListOf<QuestionEntity>()
        val allAttempts = mutableListOf<QuizAttemptEntity>()

        val calendar = Calendar.getInstance()

        // Create 5 quizzes
        val quizIdMap = mutableListOf<Long>()
        for (i in 1..5) {
            val quizSurah = surahNames30th.random(Random)
            val q = QuizEntity(
                title = "اختبار سورة $quizSurah",
                surahName = quizSurah,
                ayahRange = "1-${Random.nextInt(5, 20)}",
                totalQuestions = 5,
                passScore = 70,
                createdAt = System.currentTimeMillis() - (i * 86400000L * 7)
            )
            val qId = quizDao.insertQuiz(q)
            quizIdMap.add(qId)

            for (qIdx in 1..5) {
                val options = listOf(
                    "الإجابة الصحيحة $qIdx",
                    "إجابة خاطئة $qIdx أ",
                    "إجابة خاطئة $qIdx ب",
                    "إجابة خاطئة $qIdx ج"
                )
                quizDao.insertQuestion(
                    QuestionEntity(
                        quizId = qId,
                        questionText = "سؤال $qIdx حول سورة ${quizSurah}?",
                        options = gson.toJson(options),
                        correctAnswer = 0,
                        ayahReference = "الآية $qIdx",
                        points = 2
                    )
                )
            }
        }

        // Generate 40 students
        val studentIds = mutableListOf<Long>()
        for (i in arabicNames.indices) {
            val name = arabicNames[i]
            val level = when {
                i < 10 -> "BEGINNER"
                i < 30 -> "INTERMEDIATE"
                else -> "ADVANCED"
            }

            val totalAyahs = when (level) {
                "BEGINNER" -> Random.nextInt(1, 51)
                "INTERMEDIATE" -> Random.nextInt(51, 301)
                else -> Random.nextInt(301, 801)
            }

            val currentSurah = when (level) {
                "BEGINNER" -> starterSurahs.random(Random)
                "INTERMEDIATE" -> midSurahs.random(Random)
                else -> advancedSurahs.random(Random)
            }

            val weakPoints = tajweedRules.shuffled(Random).take(Random.nextInt(1, 4))
            val strongPts = strongPointsList.shuffled(Random).take(Random.nextInt(2, 4))

            val studentId = studentDao.insert(
                StudentEntity(
                    fullName = name,
                    age = Random.nextInt(8, 25),
                    phone = "05${Random.nextInt(10000000, 99999999)}",
                    guardianName = "والد $name",
                    guardianPhone = "05${Random.nextInt(10000000, 99999999)}",
                    level = level,
                    enrollmentDate = System.currentTimeMillis() - Random.nextLong(86400000L * 180, 86400000L * 365),
                    totalMemorizedAyahs = totalAyahs,
                    currentSurah = currentSurah,
                    currentAyah = Random.nextInt(1, 20),
                    learningStyle = learningStyles.random(Random),
                    weakPoints = gson.toJson(weakPoints),
                    strongPoints = gson.toJson(strongPts),
                    averageSessionDuration = Random.nextInt(20, 60),
                    preferredSessionTime = sessionTimes.random(Random)
                )
            )
            studentIds.add(studentId)
        }

        // Generate progress, paths, schedules, audio per student
        studentIds.forEach { sid ->
            val idx = studentIds.indexOf(sid)
            val studentLevel = when {
                idx < 10 -> "BEGINNER"
                idx < 30 -> "INTERMEDIATE"
                else -> "ADVANCED"
            }

            val numSessions = Random.nextInt(8, 16)
            val surahPool = when (studentLevel) {
                "BEGINNER" -> starterSurahs
                "INTERMEDIATE" -> midSurahs
                else -> advancedSurahs
            }

            for (s in 0 until numSessions) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -(s * Random.nextInt(7, 14)))
                calendar.set(Calendar.HOUR_OF_DAY, Random.nextInt(8, 18))
                calendar.set(Calendar.MINUTE, 0)

                val ayahMemorized = Random.nextInt(1, 8)
                val surah = surahPool.random(Random)
                val from = Random.nextInt(1, 20)
                val to = from + ayahMemorized

                progressDao.insert(
                    ProgressEntity(
                        studentId = sid,
                        date = calendar.timeInMillis,
                        sessionDuration = Random.nextInt(20, 60),
                        surahName = surah,
                        ayahFrom = from,
                        ayahTo = to,
                        memorizedAyahs = ayahMemorized,
                        reviewedAyahs = Random.nextInt(5, 20),
                        mistakesCount = Random.nextInt(0, 5),
                        mistakeDetails = gson.toJson(listOf("خطأ في المد", "خطأ في الغنة")),
                        teacherNotes = if (Random.nextBoolean()) "تحسن ملحوظ في الأداء" else "يحتاج لمراجعة مخارج الحروف",
                        grade = Random.nextInt(4, 11),
                        mood = sessionMoods.random(Random),
                        revisionNeeded = Random.nextBoolean()
                    )
                )
            }

            // Learning Path
            val pathSurah = surahPool.random(Random)
            val totalWeeks = Random.nextInt(8, 13)
            val milestones = mutableListOf<Map<String, Any>>()

            for (w in 1..totalWeeks) {
                val targetAyahs = Random.nextInt(5, 15)
                val actualAchieved = if (Random.nextFloat() > 0.3f) targetAyahs + Random.nextInt(-2, 5) else 0
                milestones.add(
                    mapOf(
                        "weekNumber" to w,
                        "targetSurah" to pathSurah,
                        "targetAyahFrom" to ((w - 1) * targetAyahs + 1),
                        "targetAyahTo" to (w * targetAyahs),
                        "targetMemorizedAyahs" to targetAyahs,
                        "targetRevisionAyahs" to Random.nextInt(3, 10),
                        "isCompleted" to (actualAchieved >= targetAyahs * 0.7f),
                        "completedDate" to (if (actualAchieved >= targetAyahs * 0.7f) System.currentTimeMillis() - Random.nextLong(86400000L * 30) else null),
                        "actualAyahsAchieved" to actualAchieved.coerceAtLeast(0),
                        "difficultyRating" to Random.nextInt(1, 6),
                        "focusTopic" to if (Random.nextBoolean()) tajweedRules.random(Random) else "",
                        "hasQuiz" to (w % 2 == 0),
                        "hasTajweedExercise" to (w % 3 == 0)
                    )
                )
            }

            val focusAreas = mutableListOf("الحفظ الجديد")
            if (Random.nextBoolean()) focusAreas.add("التجويد")
            if (Random.nextBoolean()) focusAreas.add("المراجعة")

            pathDao.insert(
                LearningPathEntity(
                    studentId = sid,
                    generatedDate = System.currentTimeMillis() - Random.nextLong(86400000L * 90),
                    pathTitle = "مسار حفظ $pathSurah",
                    description = "خطة حفظ مخصصة - $totalWeeks أسبوع",
                    totalWeeks = totalWeeks,
                    currentWeek = Random.nextInt(1, totalWeeks + 1),
                    weeklyGoalAyahs = Random.nextInt(5, 15),
                    dailyGoalMinutes = Random.nextInt(20, 45),
                    status = "ACTIVE",
                    milestones = gson.toJson(milestones),
                    nextRecommendedSurah = surahPool.random(Random),
                    focusAreas = gson.toJson(focusAreas),
                    adaptationHistory = "[]"
                )
            )

            // Schedule (2-4 per week)
            val daysCount = Random.nextInt(2, 5)
            val usedDays = mutableSetOf<Int>()
            for (d in 0 until daysCount) {
                var day = Random.nextInt(0, 6)
                while (day in usedDays) day = Random.nextInt(0, 6)
                usedDays.add(day)

                val sessionTypes = listOf("NEW_MEMORIZATION", "REVISION", "TAJWEED", "QUIZ")
                val st = sessionTypes.random(Random)
                val hour = Random.nextInt(8, 18)

                scheduleDao.insert(
                    ScheduleEntity(
                        studentId = sid,
                        dayOfWeek = day,
                        startTime = "$hour:00",
                        endTime = "${hour + 1}:00",
                        subject = if (st == "TAJWEED") "أحكام التجويد" else "حفظ القرآن",
                        sessionType = st,
                        isRecurring = true,
                        notificationEnabled = true,
                        notes = ""
                    )
                )
            }

            // Audio recordings (1-3)
            val audioCount = Random.nextInt(1, 4)
            for (a in 0 until audioCount) {
                audioDao.insert(
                    AudioEntity(
                        studentId = sid,
                        filePath = "/data/data/com.quraan.teacher.app/files/audio/recording_${sid}_$a.mp3",
                        uploadDate = System.currentTimeMillis() - Random.nextLong(86400000L * 30),
                        surahName = surahPool.random(Random),
                        durationSeconds = Random.nextInt(60, 600),
                        teacherRating = Random.nextInt(1, 6),
                        feedback = if (Random.nextBoolean()) "أداء جيد يحتاج لتحسين المدود" else "",
                        analysisNotes = if (Random.nextBoolean()) "لاحظ تحسن في نطق الحروف" else ""
                    )
                )
            }

            // Quiz attempts (2-5)
            val attemptCount = Random.nextInt(2, 6)
            for (a in 0 until attemptCount) {
                val qId = quizIdMap.random(Random)
                val score = Random.nextInt(3, 11)
                quizDao.insertAttempt(
                    QuizAttemptEntity(
                        studentId = sid,
                        quizId = qId,
                        attemptDate = System.currentTimeMillis() - Random.nextLong(86400000L * 60),
                        score = score,
                        totalPoints = 10,
                        answers = gson.toJson(listOf(0, 1, 2, 0, 1)),
                        passed = score >= 7,
                        timeTakenSeconds = Random.nextInt(180, 600)
                    )
                )
            }
        }

        prefs.edit().putBoolean(SEED_FLAG, true).apply()
    }
}
