package com.quraan.teacher.app.presentation.screens.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quraan.teacher.app.data.local.entities.AudioEntity
import com.quraan.teacher.app.data.repository.AudioRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioRecordingGroup(
    val studentId: Long,
    val studentName: String,
    val recordings: List<AudioEntity>
)

data class AudioUiState(
    val isLoading: Boolean = true,
    val audioGroups: List<AudioRecordingGroup> = emptyList(),
    val filterStudentId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    init { loadAudio() }

    private fun loadAudio() {
        viewModelScope.launch {
            try {
                combine(
                    audioRepository.getAllAudio(),
                    studentRepository.getAllActiveStudents()
                ) { audio, students ->
                    Pair(audio, students)
                }.collect { (audio, students) ->
                    val nameMap = students.associate { it.id to it.fullName }
                    val groups = audio.groupBy { it.studentId }.map { (studentId, recordings) ->
                        AudioRecordingGroup(
                            studentId = studentId,
                            studentName = nameMap[studentId] ?: "طالب #$studentId",
                            recordings = recordings.sortedByDescending { it.uploadDate }
                        )
                    }.sortedBy { it.studentName }

                    _uiState.update { it.copy(isLoading = false, audioGroups = groups) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateRating(audioId: Long, rating: Int) {
        viewModelScope.launch {
            try {
                val audio = audioRepository.getAudioById(audioId) ?: return@launch
                audioRepository.update(audio.copy(teacherRating = rating))
            } catch (_: Exception) {}
        }
    }

    fun updateFeedback(audioId: Long, feedback: String) {
        viewModelScope.launch {
            try {
                val audio = audioRepository.getAudioById(audioId) ?: return@launch
                audioRepository.update(audio.copy(feedback = feedback))
            } catch (_: Exception) {}
        }
    }
}
