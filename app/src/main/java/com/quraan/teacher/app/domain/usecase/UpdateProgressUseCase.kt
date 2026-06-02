package com.quraan.teacher.app.domain.usecase

import com.google.gson.Gson
import com.quraan.teacher.app.data.local.entities.LearningPathEntity
import com.quraan.teacher.app.data.local.entities.ProgressEntity
import com.quraan.teacher.app.data.repository.LearningPathRepository
import com.quraan.teacher.app.data.repository.ProgressRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import com.quraan.teacher.app.domain.model.LearningMilestone
import com.quraan.teacher.app.domain.model.PathAdaptation
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class UpdateProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val studentRepository: StudentRepository,
    private val learningPathRepository: LearningPathRepository
) {
    private val gson = Gson()

    suspend operator fun invoke(
        studentId: Long,
        progress: ProgressEntity
    ): Result<Unit> = runCatching {
        progressRepository.insert(progress)

        val student = studentRepository.getStudentByIdOnce(studentId) ?: return@runCatching
        val newTotal = student.totalMemorizedAyahs + progress.memorizedAyahs
        studentRepository.update(
            student.copy(
                totalMemorizedAyahs = newTotal,
                currentSurah = progress.surahName,
                currentAyah = progress.ayahTo
            )
        )

        checkAndAdaptPath(studentId, progress)
    }

    private suspend fun checkAndAdaptPath(studentId: Long, progress: ProgressEntity) {
        val pathEntity = learningPathRepository.getActivePathByStudentOnce(studentId) ?: return

        val milestonesType = object : TypeToken<List<LearningMilestone>>() {}.type
        val milestones = (gson.fromJson<List<LearningMilestone>>(pathEntity.milestones, milestonesType) ?: emptyList()).toMutableList()

        val adaptationType = object : TypeToken<List<PathAdaptation>>() {}.type
        val adaptations = (gson.fromJson<List<PathAdaptation>>(pathEntity.adaptationHistory, adaptationType) ?: emptyList()).toMutableList()

        val currentWeekIndex = pathEntity.currentWeek - 1
        if (currentWeekIndex < milestones.size) {
            val milestone = milestones[currentWeekIndex]
            val updatedMilestone = milestone.copy(
                actualAyahsAchieved = milestone.actualAyahsAchieved + progress.memorizedAyahs,
                isCompleted = milestone.actualAyahsAchieved + progress.memorizedAyahs >= milestone.targetMemorizedAyahs,
                completedDate = if (milestone.actualAyahsAchieved + progress.memorizedAyahs >= milestone.targetMemorizedAyahs)
                    System.currentTimeMillis() else null
            )
            milestones[currentWeekIndex] = updatedMilestone
        }

        val lastTwoWeeks = milestones.takeLast(2).filter { it.isCompleted }
        if (lastTwoWeeks.size >= 2) {
            val avgAchieved = lastTwoWeeks.map { it.actualAyahsAchieved.toFloat() / it.targetMemorizedAyahs.toFloat() }.average()

            when {
                avgAchieved < 0.7 -> {
                    val reductionFactor = 0.8f
                    for (i in currentWeekIndex + 1 until (currentWeekIndex + 4).coerceAtMost(milestones.size)) {
                        val m = milestones[i]
                        milestones[i] = m.copy(
                            targetMemorizedAyahs = (m.targetMemorizedAyahs * reductionFactor).toInt()
                        )
                    }
                    adaptations.add(
                        PathAdaptation(
                            date = System.currentTimeMillis(),
                            reason = "تعديل تلقائي",
                            description = "المسار تم تعديله تلقائياً لتناسب تقدمك",
                            changes = "تم تخفيض أهداف الأسابيع القادمة بنسبة 20%"
                        )
                    )
                }
                avgAchieved > 1.2 -> {
                    val increaseFactor = 1.15f
                    for (i in currentWeekIndex + 1 until (currentWeekIndex + 4).coerceAtMost(milestones.size)) {
                        val m = milestones[i]
                        milestones[i] = m.copy(
                            targetMemorizedAyahs = (m.targetMemorizedAyahs * increaseFactor).toInt()
                        )
                    }
                    adaptations.add(
                        PathAdaptation(
                            date = System.currentTimeMillis(),
                            reason = "تسريع المسار",
                            description = "ممتاز! تم تسريع المسار",
                            changes = "تم زيادة أهداف الأسابيع القادمة بنسبة 15%"
                        )
                    )
                }
            }
        }

        val updatedPath = pathEntity.copy(
            milestones = gson.toJson(milestones),
            adaptationHistory = gson.toJson(adaptations),
            currentWeek = if (milestones[currentWeekIndex.coerceAtMost(milestones.size - 1)].isCompleted)
                (pathEntity.currentWeek + 1).coerceAtMost(pathEntity.totalWeeks) else pathEntity.currentWeek
        )
        learningPathRepository.update(updatedPath)
    }
}
