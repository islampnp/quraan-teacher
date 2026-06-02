package com.quraan.teacher.app.presentation.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.quraan.teacher.app.data.local.entities.ProgressEntity
import com.quraan.teacher.app.data.local.entities.StudentEntity
import com.quraan.teacher.app.presentation.components.KPICard
import com.quraan.teacher.app.presentation.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToStudent: (Long) -> Unit,
    onNavigateToAddSession: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("معلِّم القرآن", style = MaterialTheme.typography.headlineMedium)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddSession,
                containerColor = Gold,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة حصة جديدة")
            }
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // KPI Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KPICard(
                            label = "إجمالي الطلاب",
                            value = uiState.totalStudents,
                            icon = { Icon(Icons.Default.People, null, tint = Primary) },
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        KPICard(
                            label = "متوسط التقدم",
                            value = (uiState.averageProgress * 100).toInt(),
                            icon = { Icon(Icons.Default.TrendingUp, null, tint = Success) },
                            color = Success,
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KPICard(
                            label = "حصص هذا الأسبوع",
                            value = uiState.weeklySessions,
                            icon = { Icon(Icons.Default.CalendarMonth, null, tint = Info) },
                            color = Info,
                            modifier = Modifier.weight(1f)
                        )
                        KPICard(
                            label = "معدل النجاح",
                            value = (uiState.quizPassRate * 100).toInt(),
                            icon = { Icon(Icons.Default.Quiz, null, tint = Gold) },
                            color = Gold,
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Students by Level Donut
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("توزيع الطلاب حسب المستوى", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            DonutChart(
                                data = listOf(
                                    Triple("مبتدئ", uiState.beginnerCount, ChipBeginnerText),
                                    Triple("متوسط", uiState.intermediateCount, ChipIntermediateText),
                                    Triple("متقدم", uiState.advancedCount, ChipAdvancedText)
                                )
                            )
                        }
                    }
                }

                // Monthly Ayahs Chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("الآيات المحفوظة شهرياً", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (uiState.monthlyAyahs.isNotEmpty()) {
                                MonthlyAyahsChart(data = uiState.monthlyAyahs)
                            }
                        }
                    }
                }

                // Top Students
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("أفضل 10 طلاب", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.topStudents.take(5).forEach { student ->
                                TopStudentRow(student = student) {
                                    onNavigateToStudent(student.id)
                                }
                            }
                        }
                    }
                }

                // Recent Sessions
                item {
                    Text("آخر الحصص", style = MaterialTheme.typography.titleMedium)
                }

                items(uiState.recentSessions.take(10)) { session ->
                    SessionCard(session = session, onClick = { onNavigateToStudent(session.studentId) })
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DonutChart(data: List<Triple<String, Int, Color>>) {
    val total = data.sumOf { it.second }.toFloat()
    if (total == 0f) {
        Text("لا توجد بيانات", color = TextMuted)
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            var startAngle = -90f
            data.forEach { (_, count, color) ->
                val sweepAngle = (count / total) * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Column {
            data.forEach { (label, count, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$label: $count", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun MonthlyAyahsChart(data: List<Pair<String, Int>>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries { series(data.map { it.second.toFloat() }) }
        }
    }

    CartesianChartHost(
        chart = com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis()
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun TopStudentRow(student: StudentEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Text(student.fullName.take(1), color = OnPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(student.fullName, style = MaterialTheme.typography.bodyMedium)
            Text(student.currentSurah, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        Text("${student.totalMemorizedAyahs} آية", style = MaterialTheme.typography.titleSmall, color = Primary)
    }
    HorizontalDivider()
}

@Composable
private fun SessionCard(session: ProgressEntity, onClick: () -> Unit) {
    val gradeColor = when {
        session.grade >= 8 -> Success
        session.grade >= 5 -> Warning
        else -> Danger
    }
    val moodEmoji = when (session.mood) {
        "EXCELLENT" -> "🌟"
        "GOOD" -> "👍"
        "AVERAGE" -> "😐"
        else -> "😞"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(moodEmoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("سورة ${session.surahName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("آيات ${session.ayahFrom}-${session.ayahTo} | ${session.memorizedAyahs} آية جديدة", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = gradeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${session.grade}/10",
                    style = MaterialTheme.typography.labelMedium,
                    color = gradeColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
