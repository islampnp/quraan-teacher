package com.quraan.teacher.app.presentation.screens.quizzes

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quraan.teacher.app.presentation.components.ProgressRing
import com.quraan.teacher.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizzesScreen(viewModel: QuizzesViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.quizResult != null) {
        QuizResultScreen(
            result = uiState.quizResult!!,
            onDismiss = { viewModel.closeResult() }
        )
    } else if (uiState.isQuizActive && uiState.selectedQuiz != null) {
        QuizAttemptScreen(
            quiz = uiState.selectedQuiz!!,
            questions = uiState.questions,
            currentIndex = uiState.currentQuestionIndex,
            answers = uiState.answers,
            onSelectAnswer = { qId, ans -> viewModel.selectAnswer(qId, ans) },
            onNext = { viewModel.nextQuestion() },
            onPrevious = { viewModel.previousQuestion() },
            onSubmit = { viewModel.submitQuiz() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("الاختبارات") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary)
                )
            },
            containerColor = Background
        ) { padding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.quizzes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Quiz, null, Modifier.size(64.dp), tint = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("لا توجد اختبارات", color = TextMuted)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.quizzes) { quiz ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            onClick = { viewModel.startQuiz(quiz) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Quiz, null, Modifier.size(32.dp), tint = Primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(quiz.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("سورة ${quiz.surahName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${quiz.totalQuestions} سؤال", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("ابدأ الاختبار", style = MaterialTheme.typography.labelMedium, color = Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizAttemptScreen(
    quiz: com.quraan.teacher.app.data.local.entities.QuizEntity,
    questions: List<com.quraan.teacher.app.data.local.entities.QuestionEntity>,
    currentIndex: Int,
    answers: Map<Long, Int>,
    onSelectAnswer: (Long, Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSubmit: () -> Unit
) {
    val currentQuestion = questions.getOrNull(currentIndex)
    val progress = if (questions.isNotEmpty()) currentIndex.toFloat() / questions.size else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quiz.title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Primary,
                trackColor = SurfaceVariant
            )

            if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "السؤال ${currentIndex + 1} من ${questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                currentQuestion.questionText,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (currentQuestion.ayahReference.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "(${currentQuestion.ayahReference})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val options = com.google.gson.Gson().fromJson(
                        currentQuestion.options,
                        Array<String>::class.java
                    ).toList()

                    options.forEachIndexed { index, option ->
                        val isSelected = answers[currentQuestion.id] == index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryLight.copy(alpha = 0.15f) else Surface
                            ),
                            onClick = { onSelectAnswer(currentQuestion.id, index) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectAnswer(currentQuestion.id, index) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentIndex > 0) {
                            OutlinedButton(onClick = onPrevious) { Text("السابق") }
                        } else { Spacer(modifier = Modifier.width(1.dp)) }

                        if (currentIndex < questions.size - 1) {
                            Button(
                                onClick = onNext,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) { Text("التالي") }
                        } else {
                            Button(
                                onClick = onSubmit,
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) { Text("إنهاء الاختبار") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultScreen(
    result: QuizResultState,
    onDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نتيجة الاختبار") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "إغلاق") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary, navigationIconContentColor = OnPrimary)
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProgressRing(
                            progress = result.percentage,
                            size = 120.dp,
                            strokeWidth = 12.dp,
                            progressColor = if (result.passed) Success else Danger
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "${result.score}/${result.totalPoints}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (result.passed) Success.copy(alpha = 0.1f) else Danger.copy(alpha = 0.1f)
                        ) {
                            Text(
                                if (result.passed) "ناجح ✓" else "راسب ✗",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = if (result.passed) Success else Danger,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text("مراجعة الإجابات", style = MaterialTheme.typography.titleMedium)
            }

            items(result.answers) { answer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (answer.isCorrect) Success.copy(alpha = 0.05f) else Danger.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (answer.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            null,
                            tint = if (answer.isCorrect) Success else Danger,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(answer.questionText, style = MaterialTheme.typography.bodySmall)
                            Text("+${answer.pointsEarned} نقطة", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
