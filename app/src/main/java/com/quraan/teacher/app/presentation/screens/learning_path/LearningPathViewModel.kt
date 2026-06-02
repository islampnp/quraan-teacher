package com.quraan.teacher.app.presentation.screens.learning_path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quraan.teacher.app.data.repository.StudentRepository
import com.quraan.teacher.app.domain.usecase.EvaluateStudentUseCase
import com.quraan.teacher.app.domain.usecase.GenerateLearningPathUseCase
import com.quraan.teacher.app.domain.usecase.PathGeneratorInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearningPathUiState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val studentName: String = "",
    val studentLevel: String = "",
    val memorizedAyahs: Int = 0,
    val averageGrade: Double = 0.0,
    val sessionFrequency: Int = 0,
    val evaluation: String = "",
    // Step 1 fields
    val weaknesses: List<String> = emptyList(),
    val learningPace: String = "متوسط",
    val availableDays: Int = 5,
    val sessionDuration: Int = 30,
    val priority: String = "متوازن",
    // Step 2 fields
    val targetSurah: String = "",
    val targetWeeks: Int = 12,
    val revisionRatio: Int = 30,
    val includeTajweed: Boolean = true,
    val includeWeeklyQuiz: Boolean = true,
    // Step 3 - Generated path
    val generatedPathTitle: String = "",
    val generatedWeeks: List<GeneratedWeek> = emptyList(),
    val error: String? = null,
    val success: Boolean = false
)

data class GeneratedWeek(
    val weekNumber: Int,
    val surah: String,
    val ayahRange: String,
    val newAyahs: Int,
    val revisionAyahs: Int,
    val tajweedTopic: String = "",
    val hasQuiz: Boolean = false
)

@HiltViewModel
class LearningPathViewModel @Inject constructor(
    private val generateLearningPathUseCase: GenerateLearningPathUseCase,
    private val evaluateStudentUseCase: EvaluateStudentUseCase,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningPathUiState())
    val uiState: StateFlow<LearningPathUiState> = _uiState.asStateFlow()

    private var studentId: Long = 0

    fun initialize(studentId: Long) {
        this.studentId = studentId
        viewModelScope.launch {
            try {
                val student = studentRepository.getStudentByIdOnce(studentId) ?: return@launch
                val evaluation = evaluateStudentUseCase(studentId)

                _uiState.update {
                    it.copy(
                        studentName = student.fullName,
                        studentLevel = student.level,
                        memorizedAyahs = student.totalMemorizedAyahs,
                        averageGrade = evaluation.averageGrade,
                        sessionFrequency = evaluation.sessionFrequency,
                        evaluation = evaluation.trend,
                        targetSurah = evaluation.recommendedSurah,
                        learningPace = evaluation.recommendedPace
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun setStep(step: Int) { _uiState.update { it.copy(currentStep = step) } }

    fun setWeaknesses(weaknesses: List<String>) { _uiState.update { it.copy(weaknesses = weaknesses) } }
    fun toggleWeakness(weakness: String) {
        _uiState.update { state ->
            val current = state.weaknesses.toMutableList()
            if (current.contains(weakness)) current.remove(weakness) else current.add(weakness)
            state.copy(weaknesses = current)
        }
    }
    fun setLearningPace(pace: String) { _uiState.update { it.copy(learningPace = pace) } }
    fun setAvailableDays(days: Int) { _uiState.update { it.copy(availableDays = days) } }
    fun setSessionDuration(minutes: Int) { _uiState.update { it.copy(sessionDuration = minutes) } }
    fun setPriority(priority: String) { _uiState.update { it.copy(priority = priority) } }
    fun setTargetSurah(surah: String) { _uiState.update { it.copy(targetSurah = surah) } }
    fun setTargetWeeks(weeks: Int) { _uiState.update { it.copy(targetWeeks = weeks) } }
    fun setRevisionRatio(ratio: Int) { _uiState.update { it.copy(revisionRatio = ratio) } }
    fun setIncludeTajweed(include: Boolean) { _uiState.update { it.copy(includeTajweed = include) } }
    fun setIncludeWeeklyQuiz(include: Boolean) { _uiState.update { it.copy(includeWeeklyQuiz = include) } }

    fun generateAndPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val input = PathGeneratorInput(
                studentId = studentId,
                weaknesses = _uiState.value.weaknesses,
                learningPace = _uiState.value.learningPace,
                availableDaysPerWeek = _uiState.value.availableDays,
                sessionDurationMinutes = _uiState.value.sessionDuration,
                priority = _uiState.value.priority,
                targetSurah = _uiState.value.targetSurah,
                targetWeeks = _uiState.value.targetWeeks,
                revisionRatio = _uiState.value.revisionRatio,
                includeTajweed = _uiState.value.includeTajweed,
                includeWeeklyQuiz = _uiState.value.includeWeeklyQuiz
            )

            try {
                val result = generateLearningPathUseCase(input)
                result.onSuccess { path ->
                    val weeks = mutableListOf<GeneratedWeek>()
                    val milestones = com.google.gson.Gson().fromJson(path.milestones, Array<com.quraan.teacher.app.domain.model.LearningMilestone>::class.java).toList()
                    milestones.forEach { m ->
                        weeks.add(
                            GeneratedWeek(
                                weekNumber = m.weekNumber,
                                surah = m.targetSurah,
                                ayahRange = "${m.targetAyahFrom}-${m.targetAyahTo}",
                                newAyahs = m.targetMemorizedAyahs,
                                revisionAyahs = m.targetRevisionAyahs,
                                tajweedTopic = m.focusTopic,
                                hasQuiz = m.hasQuiz
                            )
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generatedPathTitle = path.pathTitle,
                            generatedWeeks = weeks,
                            currentStep = 2
                        )
                    }
                }
                result.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun savePath() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val input = PathGeneratorInput(
                studentId = studentId,
                weaknesses = _uiState.value.weaknesses,
                learningPace = _uiState.value.learningPace,
                availableDaysPerWeek = _uiState.value.availableDays,
                sessionDurationMinutes = _uiState.value.sessionDuration,
                priority = _uiState.value.priority,
                targetSurah = _uiState.value.targetSurah,
                targetWeeks = _uiState.value.targetWeeks,
                revisionRatio = _uiState.value.revisionRatio,
                includeTajweed = _uiState.value.includeTajweed,
                includeWeeklyQuiz = _uiState.value.includeWeeklyQuiz
            )
            try {
                generateLearningPathUseCase(input)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
