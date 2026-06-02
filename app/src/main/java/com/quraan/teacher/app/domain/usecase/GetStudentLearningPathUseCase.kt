package com.quraan.teacher.app.domain.usecase

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quraan.teacher.app.data.local.entities.LearningPathEntity
import com.quraan.teacher.app.data.repository.LearningPathRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import com.quraan.teacher.app.domain.model.LearningMilestone
import com.quraan.teacher.app.domain.model.PathAdaptation
import com.quraan.teacher.app.domain.model.PathStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class StudentLearningPathData(
    val entity: LearningPathEntity,
    val milestones: List<LearningMilestone>,
    val adaptations: List<PathAdaptation>,
    val progressPercent: Float
)

class GetStudentLearningPathUseCase @Inject constructor(
    private val learningPathRepository: LearningPathRepository,
    private val studentRepository: StudentRepository
) {
    private val gson = Gson()

    fun getActivePath(studentId: Long): Flow<StudentLearningPathData?> {
        return learningPathRepository.getActivePathByStudent(studentId).map { entity ->
            entity?.let { mapToDomain(it) }
        }
    }

    fun getPaths(studentId: Long): Flow<List<StudentLearningPathData>> {
        return learningPathRepository.getPathsByStudent(studentId).map { entities ->
            entities.map { mapToDomain(it) }
        }
    }

    private suspend fun mapToDomain(entity: LearningPathEntity): StudentLearningPathData {
        val milestonesType = object : TypeToken<List<LearningMilestone>>() {}.type
        val milestones: List<LearningMilestone> = gson.fromJson(entity.milestones, milestonesType) ?: emptyList()

        val adaptationType = object : TypeToken<List<PathAdaptation>>() {}.type
        val adaptations: List<PathAdaptation> = gson.fromJson(entity.adaptationHistory, adaptationType) ?: emptyList()

        val completedMilestones = milestones.count { it.isCompleted }
        val progressPercent = if (entity.totalWeeks > 0) {
            (completedMilestones.toFloat() / entity.totalWeeks.toFloat()).coerceAtMost(1f)
        } else 0f

        return StudentLearningPathData(
            entity = entity,
            milestones = milestones,
            adaptations = adaptations,
            progressPercent = progressPercent
        )
    }
}
