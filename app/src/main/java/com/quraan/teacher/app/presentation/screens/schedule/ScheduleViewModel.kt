package com.quraan.teacher.app.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quraan.teacher.app.data.local.entities.ScheduleEntity
import com.quraan.teacher.app.data.local.entities.StudentEntity
import com.quraan.teacher.app.data.repository.ScheduleRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val selectedDay: Int = 0,
    val schedules: List<ScheduleEntity> = emptyList(),
    val allSchedules: List<ScheduleEntity> = emptyList(),
    val students: Map<Long, String> = emptyMap(),
    val dayNames: List<String> = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"),
    val error: String? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _selectedDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Get all students for name mapping
                studentRepository.getAllActiveStudents().collect { students ->
                    val nameMap = students.associate { it.id to it.fullName }
                    _uiState.update { it.copy(students = nameMap) }
                }
            } catch (_: Exception) {}

            try {
                combine(
                    scheduleRepository.getAllSchedules(),
                    _selectedDay
                ) { schedules, day ->
                    Pair(schedules, day)
                }.collect { (all, day) ->
                    val filtered = all.filter { it.dayOfWeek == day }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allSchedules = all,
                            schedules = filtered,
                            selectedDay = day
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectDay(day: Int) { _selectedDay.value = day }

    fun showAddDialog() { _uiState.update { it.copy(showAddDialog = true) } }
    fun hideAddDialog() { _uiState.update { it.copy(showAddDialog = false) } }

    fun addSchedule(
        studentId: Long, dayOfWeek: Int, startTime: String, endTime: String,
        sessionType: String, subject: String, notes: String, notificationEnabled: Boolean
    ) {
        viewModelScope.launch {
            try {
                scheduleRepository.insert(
                    ScheduleEntity(
                        studentId = studentId,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        sessionType = sessionType,
                        subject = subject,
                        notes = notes,
                        notificationEnabled = notificationEnabled
                    )
                )
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            try { scheduleRepository.delete(schedule) } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
