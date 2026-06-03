package com.quraan.teacher.app.presentation.screens.student_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quraan.teacher.app.data.local.entities.ProgressEntity
import com.quraan.teacher.app.domain.model.LearningMilestone
import com.quraan.teacher.app.domain.model.PathAdaptation
import com.quraan.teacher.app.presentation.components.*
import com.quraan.teacher.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    viewModel: StudentDetailViewModel,
    studentId: Long,
    onNavigateToPathGenerator: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(studentId) { viewModel.loadStudent(studentId) }

    val uiState by viewModel.uiState.collectAsState()
    val gson = remember { Gson() }
    val tabs = listOf("الملف الشخصي", "مسار التعلم", "سجل الحصص", "الاختبارات", "التسجيلات")
    val tabTitles = listOf("profile", "learning_path", "sessions", "quizzes", "audio")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.student?.fullName ?: "", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary, navigationIconContentColor = OnPrimary)
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.student == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("الطالب غير موجود", color = TextMuted)
            }
        } else {
            val student = uiState.student!!

            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Avatar + Name Header
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(student.fullName.take(2), color = OnPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, style = MaterialTheme.typography.titleLarge)
                            Text("السورة: ${student.currentSurah}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            Text("المحفوظ: ${student.totalMemorizedAyahs} آية", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = Surface,
                    contentColor = Primary,
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.onTabSelected(index) },
                            text = { Text(title) }
                        )
                    }
                }

                // Tab Content
                when (uiState.selectedTab) {
                    0 -> ProfileTab(student = student, gson = gson)
                    1 -> LearningPathTab(
                        activePath = uiState.activePath,
                        milestones = uiState.milestones,
                        adaptations = uiState.adaptations,
                        gson = gson,
                        onGeneratePath = onNavigateToPathGenerator
                    )
                    2 -> SessionsTab(
                        progressList = uiState.progress,
                        onAddSession = { /* TODO: show bottom sheet */ }
                    )
                    3 -> QuizzesTab(attempts = uiState.quizAttempts, gson = gson)
                    4 -> AudioTab(recordings = uiState.audioRecordings)
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(student: com.quraan.teacher.app.data.local.entities.StudentEntity, gson: Gson) {
    val type = object : TypeToken<List<String>>() {}.type
    val weakPoints: List<String> = gson.fromJson(student.weakPoints, type) ?: emptyList()
    val strongPoints: List<String> = gson.fromJson(student.strongPoints, type) ?: emptyList()
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("معلومات الطالب", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("العمر", "${student.age} سنة")
                    InfoRow("رقم الهاتف", student.phone)
                    InfoRow("ولي الأمر", student.guardianName)
                    InfoRow("هاتف ولي الأمر", student.guardianPhone)
                    InfoRow("تاريخ التسجيل", dateFormat.format(Date(student.enrollmentDate)))
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("أسلوب التعلم والوقت المفضل", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text(when(student.learningStyle) { "VISUAL" -> "بصري"; "AUDITORY" -> "سمعي"; "REPETITIVE" -> "تكرار"; else -> "مختلط" }) })
                        AssistChip(onClick = {}, label = { Text(when(student.preferredSessionTime) { "MORNING" -> "صباحية" "AFTERNOON" -> "بعد الظهر" else -> "مسائية" }) })
                    }
                }
            }
        }

        if (strongPoints.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("نقاط القوة", style = MaterialTheme.typography.titleMedium, color = Success)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            strongPoints.forEach { point ->
                                Surface(shape = MaterialTheme.shapes.small, color = Success.copy(alpha = 0.1f)) {
                                    Text(point, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = Success)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (weakPoints.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("نقاط الضعف", style = MaterialTheme.typography.titleMedium, color = Danger)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            weakPoints.forEach { point ->
                                Surface(shape = MaterialTheme.shapes.small, color = Danger.copy(alpha = 0.1f)) {
                                    Text(point, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = Danger)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun LearningPathTab(
    activePath: com.quraan.teacher.app.data.local.entities.LearningPathEntity?,
    milestones: List<LearningMilestone>,
    adaptations: List<PathAdaptation>,
    gson: Gson,
    onGeneratePath: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (activePath != null) {
            val currentWeek = activePath.currentWeek
            val totalWeeks = activePath.totalWeeks
            val completedCount = milestones.count { it.isCompleted }
            val progressPct = if (totalWeeks > 0) completedCount.toFloat() / totalWeeks else 0f

            item {
                LearningPathCard(
                    title = activePath.pathTitle,
                    progressPercent = progressPct,
                    currentWeek = currentWeek,
                    totalWeeks = totalWeeks,
                    weeklyGoal = activePath.weeklyGoalAyahs,
                    description = activePath.description
                )
            }

            item {
                Text("المراحل الأسبوعية", style = MaterialTheme.typography.titleMedium)
            }

            items(milestones) { milestone ->
                MilestoneRow(milestone = milestone)
            }

            if (adaptations.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("سجل التعديلات", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            adaptations.forEach { adaptation ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.Info, null, Modifier.size(16.dp), tint = Warning)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(adaptation.description, style = MaterialTheme.typography.bodySmall)
                                        Text(adaptation.changes, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onGeneratePath,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تعديل المسار")
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Route, null, Modifier.size(48.dp), tint = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("لا يوجد مسار تعلم نشط", color = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        item {
            Button(
                onClick = onGeneratePath,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء مسار جديد")
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SessionsTab(progressList: List<ProgressEntity>, onAddSession: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (progressList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد حصص مسجلة", color = TextMuted)
                }
            }
        } else {
            items(progressList) { session ->
                SessionDetailCard(session = session)
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SessionDetailCard(session: ProgressEntity) {
    var expanded by remember { mutableStateOf(false) }
    val gradeColor = when { session.grade >= 8 -> Success; session.grade >= 5 -> Warning; else -> Danger }
    val moodEmoji = when (session.mood) { "EXCELLENT" -> "🌟"; "GOOD" -> "👍"; "AVERAGE" -> "😐"; else -> "😞" }
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(moodEmoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("سورة ${session.surahName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(dateFormat.format(Date(session.date)), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                Surface(shape = MaterialTheme.shapes.small, color = gradeColor.copy(alpha = 0.15f)) {
                    Text("${session.grade}/10", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = gradeColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("الآيات المحفوظة", "${session.memorizedAyahs}")
                InfoRow("المراجعة", "${session.reviewedAyahs}")
                InfoRow("مدة الحصة", "${session.sessionDuration} دقيقة")
                InfoRow("الأخطاء", "${session.mistakesCount}")
                if (session.teacherNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ملاحظات المعلّم:", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    Text(session.teacherNotes, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun QuizzesTab(attempts: List<com.quraan.teacher.app.data.local.entities.QuizAttemptEntity>, gson: Gson) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (attempts.isEmpty()) {
            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("لا توجد اختبارات", color = TextMuted) } }
        } else {
            items(attempts) { attempt ->
                val pct = if (attempt.totalPoints > 0) attempt.score.toFloat() / attempt.totalPoints else 0f
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(progress = pct, size = 48.dp, strokeWidth = 5.dp, progressColor = if (attempt.passed) Success else Danger)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("النتيجة: ${attempt.score}/${attempt.totalPoints}", style = MaterialTheme.typography.bodyMedium)
                            Text(if (attempt.passed) "ناجح" else "راسب", color = if (attempt.passed) Success else Danger, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioTab(recordings: List<com.quraan.teacher.app.data.local.entities.AudioEntity>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (recordings.isEmpty()) {
            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("لا توجد تسجيلات", color = TextMuted) } }
        } else {
            items(recordings) { recording ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(recording.surahName, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        AudioPlayer(filePath = recording.filePath, durationSeconds = recording.durationSeconds)
                        if (recording.teacherRating > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("★".repeat(recording.teacherRating), color = DifficultyStar)
                        }
                        if (recording.feedback.isNotBlank()) {
                            Text(recording.feedback, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
