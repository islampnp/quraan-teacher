package com.quraan.teacher.app.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quraan.teacher.app.data.local.entities.ProgressEntity
import com.quraan.teacher.app.data.local.entities.StudentEntity
import com.quraan.teacher.app.data.repository.*
import com.quraan.teacher.app.presentation.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalStudents: Int = 0,
    val averageProgress: Float = 0f,
    val weeklySessions: Int = 0,
    val quizPassRate: Float = 0f,
    val recentSessions: List<ProgressEntity> = emptyList(),
    val topStudents: List<StudentEntity> = emptyList(),
    val beginnerCount: Int = 0,
    val intermediateCount: Int = 0,
    val advancedCount: Int = 0,
    val monthlyAyahs: List<Pair<String, Int>> = emptyList(),
    val error: String? = null
)

sealed class DashboardEvent {
    data class NavigateToStudent(val studentId: Long) : DashboardEvent()
    data object NavigateToAddSession : DashboardEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val progressRepository: ProgressRepository,
    private val quizRepository: QuizRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    init {
        loadDashboard()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                studentRepository.getStudentCount()
                    .combine(studentRepository.getAllActiveStudents()) { count, students ->
                        Pair(count, students)
                    }
                    .combine(progressRepository.getRecentProgressAll()) { (count, students), sessions ->
                        Triple(count, students, sessions)
                    }
                    .combine(studentRepository.getTopStudentsByMemorization()) { (count, students, sessions), top ->
                        listOf(count, students, sessions, top)
                    }
                    .collectLatest { data ->
                        val count = data[0] as Int
                        val students = data[1] as List<StudentEntity>
                        val sessions = data[2] as List<ProgressEntity>
                        val top = data[3] as List<StudentEntity>

                        val now = Calendar.getInstance()
                        val weekStart = now.clone() as Calendar
                        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                        weekStart.set(Calendar.HOUR_OF_DAY, 0)
                        weekStart.set(Calendar.MINUTE, 0)
                        weekStart.set(Calendar.SECOND, 0)

                        val weeklyCount = progressRepository.getDistinctStudentCountInRange(
                            weekStart.timeInMillis, System.currentTimeMillis()
                        )

                        val allStudents = students
                        val avgProg = if (allStudents.isNotEmpty()) {
                            allStudents.map { it.totalMemorizedAyahs }.average().toFloat() / 800f
                        } else 0f

                        // Quiz pass rate
                        var totalAttempts = 0
                        var totalPassed = 0
                        for (s in allStudents) {
                            totalAttempts += quizRepository.getTotalQuizAttempts(s.id)
                            totalPassed += quizRepository.getPassedQuizCount(s.id)
                        }
                        val passRate = if (totalAttempts > 0) totalPassed.toFloat() / totalAttempts else 0f

                        val beginnerCount = studentRepository.getStudentCountByLevel("BEGINNER")
                        val intermediateCount = studentRepository.getStudentCountByLevel("INTERMEDIATE")
                        val advancedCount = studentRepository.getStudentCountByLevel("ADVANCED")

                        val monthlyData = if (allStudents.isNotEmpty()) {
                            val cal = Calendar.getInstance()
                            val months = mutableListOf<Pair<String, Int>>()
                            for (i in 5 downTo 0) {
                                cal.time = Date()
                                cal.add(Calendar.MONTH, -i)
                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                val start = cal.timeInMillis
                                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                cal.set(Calendar.HOUR_OF_DAY, 23)
                                val end = cal.timeInMillis
                                val total = progressRepository.getTotalMemorizedInRange(start, end)
                                val monthNames = arrayOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                                    "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
                                months.add(Pair(monthNames[cal.get(Calendar.MONTH)], total))
                            }
                            months
                        } else emptyList()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalStudents = count,
                                averageProgress = avgProg,
                                weeklySessions = weeklyCount,
                                quizPassRate = passRate,
                                recentSessions = sessions,
                                topStudents = top,
                                beginnerCount = beginnerCount,
                                intermediateCount = intermediateCount,
                                advancedCount = advancedCount,
                                monthlyAyahs = monthlyData
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNavigateToStudent(studentId: Long) {
        viewModelScope.launch { _events.emit(DashboardEvent.NavigateToStudent(studentId)) }
    }

    fun onNavigateToAddSession() {
        viewModelScope.launch { _events.emit(DashboardEvent.NavigateToAddSession) }
    }
}
