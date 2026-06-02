package com.quraan.teacher.app.presentation.screens.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quraan.teacher.app.data.local.entities.*
import com.quraan.teacher.app.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizzesUiState(
    val isLoading: Boolean = true,
    val quizzes: List<QuizEntity> = emptyList(),
    val selectedQuiz: QuizEntity? = null,
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: MutableMap<Long, Int> = mutableMapOf(),
    val isQuizActive: Boolean = false,
    val quizResult: QuizResultState? = null,
    val error: String? = null
)

data class QuizResultState(
    val score: Int,
    val totalPoints: Int,
    val percentage: Float,
    val passed: Boolean,
    val answers: List<AnswerReviewItem>
)

data class AnswerReviewItem(
    val questionText: String,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean,
    val pointsEarned: Int
)

@HiltViewModel
class QuizzesViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizzesUiState())
    val uiState: StateFlow<QuizzesUiState> = _uiState.asStateFlow()

    private var _answers = MutableStateFlow<Map<Long, Int>>(emptyMap())

    init { loadQuizzes() }

    private fun loadQuizzes() {
        viewModelScope.launch {
            try {
                quizRepository.getAllQuizzes().collect { quizzes ->
                    _uiState.update { it.copy(isLoading = false, quizzes = quizzes) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun startQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            try {
                val questions = quizRepository.getQuestionsByQuizOnce(quiz.id)
                _uiState.update {
                    it.copy(
                        selectedQuiz = quiz,
                        questions = questions,
                        currentQuestionIndex = 0,
                        answers = mutableMapOf(),
                        isQuizActive = true,
                        quizResult = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun selectAnswer(questionId: Long, answerIndex: Int) {
        val newAnswers = _uiState.value.answers.toMutableMap()
        newAnswers[questionId] = answerIndex
        _uiState.update { it.copy(answers = newAnswers) }
    }

    fun nextQuestion() {
        val current = _uiState.value.currentQuestionIndex
        if (current < _uiState.value.questions.size - 1) {
            _uiState.update { it.copy(currentQuestionIndex = current + 1) }
        }
    }

    fun previousQuestion() {
        val current = _uiState.value.currentQuestionIndex
        if (current > 0) {
            _uiState.update { it.copy(currentQuestionIndex = current - 1) }
        }
    }

    fun submitQuiz() {
        val state = _uiState.value
        val questions = state.questions
        val answers = state.answers

        var totalScore = 0
        var totalPoints = 0
        val reviewItems = mutableListOf<AnswerReviewItem>()

        questions.forEach { q ->
            val selected = answers[q.id]
            val isCorrect = selected == q.correctAnswer
            val points = if (isCorrect) q.points else 0
            totalScore += points
            totalPoints += q.points

            reviewItems.add(
                AnswerReviewItem(
                    questionText = q.questionText,
                    selectedAnswer = selected ?: -1,
                    correctAnswer = q.correctAnswer,
                    isCorrect = isCorrect,
                    pointsEarned = points
                )
            )
        }

        val percentage = if (totalPoints > 0) totalScore.toFloat() / totalPoints else 0f
        val passed = percentage >= 0.7f

        _uiState.update {
            it.copy(
                isQuizActive = false,
                quizResult = QuizResultState(
                    score = totalScore,
                    totalPoints = totalPoints,
                    percentage = percentage,
                    passed = passed,
                    answers = reviewItems
                )
            )
        }
    }

    fun closeResult() {
        _uiState.update {
            it.copy(
                selectedQuiz = null,
                questions = emptyList(),
                currentQuestionIndex = 0,
                answers = mutableMapOf(),
                quizResult = null
            )
        }
    }
}
