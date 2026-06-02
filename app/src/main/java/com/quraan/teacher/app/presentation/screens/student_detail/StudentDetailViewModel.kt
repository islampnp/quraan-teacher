package com.quraan.teacher.app.presentation.screens.student_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quraan.teacher.app.data.local.entities.*
import com.quraan.teacher.app.data.repository.*
import com.quraan.teacher.app.domain.model.LearningMilestone
import com.quraan.teacher.app.domain.model.PathAdaptation
import com.quraan.teacher.app.domain.usecase.EvaluateStudentUseCase
import com.quraan.teacher.app.domain.usecase.UpdateProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDetailUiState(
    val isLoading: Boolean = true,
    val student: StudentEntity? = null,
    val progress: List<ProgressEntity> = emptyList(),
    val activePath: LearningPathEntity? = null,
    val milestones: List<LearningMilestone> = emptyList(),
    val adaptations: List<PathAdaptation> = emptyList(),
    val audioRecordings: List<AudioEntity> = emptyList(),
    val quizAttempts: List<QuizAttemptEntity> = emptyList(),
    val selectedTab: Int = 0,
    val error: String? = null
)

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val progressRepository: ProgressRepository,
    private val learningPathRepository: LearningPathRepository,
    private val audioRepository: AudioRepository,
    private val quizRepository: QuizRepository,
    private val evaluateStudentUseCase: EvaluateStudentUseCase,
    private val updateProgressUseCase: UpdateProgressUseCase
) : ViewModel() {

    private val gson = Gson()
    private val _uiState = MutableStateFlow(StudentDetailUiState())
    val uiState: StateFlow<StudentDetailUiState> = _uiState.asStateFlow()

    fun loadStudent(studentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                studentRepository.getStudentById(studentId).collect { student ->
                    _uiState.update { it.copy(student = student) }
                }
            } catch (_: Exception) {}

            try {
                progressRepository.getProgressByStudent(studentId).collect { progress ->
                    _uiState.update { it.copy(progress = progress) }
                }
            } catch (_: Exception) {}

            try {
                learningPathRepository.getActivePathByStudent(studentId).collect { path ->
                    if (path != null) {
                        val milestonesType = object : TypeToken<List<LearningMilestone>>() {}.type
                        val milestones: List<LearningMilestone> = gson.fromJson(path.milestones, milestonesType) ?: emptyList()
                        val adaptationType = object : TypeToken<List<PathAdaptation>>() {}.type
                        val adaptations: List<PathAdaptation> = gson.fromJson(path.adaptationHistory, adaptationType) ?: emptyList()
                        _uiState.update { it.copy(activePath = path, milestones = milestones, adaptations = adaptations) }
                    }
                }
            } catch (_: Exception) {}

            try {
                audioRepository.getAudioByStudent(studentId).collect { audio ->
                    _uiState.update { it.copy(audioRecordings = audio) }
                }
            } catch (_: Exception) {}

            try {
                quizRepository.getAttemptsByStudent(studentId).collect { attempts ->
                    _uiState.update { it.copy(quizAttempts = attempts) }
                }
            } catch (_: Exception) {}

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun addProgressSession(
        studentId: Long, date: Long, duration: Int, surah: String, ayahFrom: Int,
        ayahTo: Int, memorized: Int, reviewed: Int, mistakes: Int, mistakesDetail: String,
        notes: String, grade: Int, mood: String
    ) {
        viewModelScope.launch {
            try {
                updateProgressUseCase(
                    studentId = studentId,
                    progress = ProgressEntity(
                        studentId = studentId,
                        date = date,
                        sessionDuration = duration,
                        surahName = surah,
                        ayahFrom = ayahFrom,
                        ayahTo = ayahTo,
                        memorizedAyahs = memorized,
                        reviewedAyahs = reviewed,
                        mistakesCount = mistakes,
                        mistakeDetails = mistakesDetail,
                        teacherNotes = notes,
                        grade = grade,
                        mood = mood
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
