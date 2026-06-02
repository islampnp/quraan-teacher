package com.quraan.teacher.app.presentation.screens.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quraan.teacher.app.data.local.entities.StudentEntity
import com.quraan.teacher.app.data.repository.LearningPathRepository
import com.quraan.teacher.app.data.repository.ProgressRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quraan.teacher.app.domain.model.LearningMilestone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentListItem(
    val student: StudentEntity,
    val progressPercent: Float,
    val lastSessionDaysAgo: Int?,
    val lastFiveAyahs: List<Int>
)

data class StudentsUiState(
    val isLoading: Boolean = true,
    val students: List<StudentListItem> = emptyList(),
    val searchQuery: String = "",
    val filterLevel: String = "ALL",
    val error: String? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val progressRepository: ProgressRepository,
    private val learningPathRepository: LearningPathRepository
) : ViewModel() {

    private val gson = Gson()
    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterLevel = MutableStateFlow("ALL")

    init {
        loadStudents()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadStudents() {
        viewModelScope.launch {
            combine(_searchQuery, _filterLevel) { query, level -> Pair(query, level) }
                .flatMapLatest { (query, level) ->
                    val flow = when {
                        query.isNotBlank() -> studentRepository.searchStudents(query)
                        level != "ALL" -> studentRepository.getStudentsByLevel(level)
                        else -> studentRepository.getAllActiveStudents()
                    }
                    flow
                }
                .map { students ->
                    coroutineScope {
                        students.map { student ->
                            async {
                                val path = learningPathRepository.getActivePathByStudentOnce(student.id)
                                val milestones = if (path != null) {
                                    val type = object : TypeToken<List<LearningMilestone>>() {}.type
                                    gson.fromJson<List<LearningMilestone>>(path.milestones, type) ?: emptyList()
                                } else emptyList()
                                val completedMilestones = milestones.count { it.isCompleted }
                                val progressPct = if (milestones.isNotEmpty()) completedMilestones.toFloat() / milestones.size else 0f

                                StudentListItem(
                                    student = student,
                                    progressPercent = progressPct,
                                    lastSessionDaysAgo = null,
                                    lastFiveAyahs = listOf(
                                        student.totalMemorizedAyahs,
                                        (student.totalMemorizedAyahs * 0.9).toInt(),
                                        (student.totalMemorizedAyahs * 0.85).toInt(),
                                        (student.totalMemorizedAyahs * 0.8).toInt(),
                                        (student.totalMemorizedAyahs * 0.75).toInt()
                                    )
                                )
                            }
                        }.awaitAll()
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(isLoading = false, students = list, error = null) }
                }
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onFilterChange(level: String) { _filterLevel.value = level }
    fun showAddDialog() { _uiState.update { it.copy(showAddDialog = true) } }
    fun hideAddDialog() { _uiState.update { it.copy(showAddDialog = false) } }

    fun addStudent(
        name: String, age: Int, phone: String, guardianName: String,
        guardianPhone: String, level: String, learningStyle: String, preferredTime: String
    ) {
        viewModelScope.launch {
            try {
                studentRepository.insert(
                    StudentEntity(
                        fullName = name,
                        age = age,
                        phone = phone,
                        guardianName = guardianName,
                        guardianPhone = guardianPhone,
                        level = level,
                        enrollmentDate = System.currentTimeMillis(),
                        learningStyle = learningStyle,
                        preferredSessionTime = preferredTime
                    )
                )
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            try {
                studentRepository.softDelete(student.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
