package com.quraan.teacher.app.presentation.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.quraan.teacher.app.data.local.entities.ScheduleEntity
import com.quraan.teacher.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onStudentClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الجدول الأسبوعي") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = Gold,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة موعد")
            }
        },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Day Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth().background(Surface),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.dayNames.size) { index ->
                    val isSelected = uiState.selectedDay == index
                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Primary else SurfaceVariant)
                            .clickable { viewModel.selectDay(index) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.dayNames[index], style = MaterialTheme.typography.labelMedium, color = if (isSelected) OnPrimary else TextPrimary)
                    }
                }
            }

            Divider()

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.schedules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(48.dp), tint = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("لا توجد مواعيد لهذا اليوم", color = TextMuted)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.schedules.sortedBy { it.startTime }) { schedule ->
                        val studentName = uiState.students[schedule.studentId] ?: "طالب #${schedule.studentId}"
                        val sessionColor = when (schedule.sessionType) {
                            "NEW_MEMORIZATION" -> SessionGreen
                            "REVISION" -> SessionBlue
                            "TAJWEED" -> SessionAmber
                            "QUIZ" -> SessionRed
                            else -> TextMuted
                        }
                        val sessionTypeName = when (schedule.sessionType) {
                            "NEW_MEMORIZATION" -> "حفظ"
                            "REVISION" -> "مراجعة"
                            "TAJWEED" -> "تجويد"
                            "QUIZ" -> "اختبار"
                            else -> schedule.sessionType
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            onClick = { onStudentClick(schedule.studentId) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(48.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(sessionColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(studentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("${schedule.startTime} - ${schedule.endTime}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    if (schedule.subject.isNotBlank()) {
                                        Text(schedule.subject, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = sessionColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        sessionTypeName,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = sessionColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        if (uiState.showAddDialog) {
            AddScheduleBottomSheet(
                onDismiss = { viewModel.hideAddDialog() },
                students = uiState.students,
                selectedDay = uiState.selectedDay,
                onSave = { studentId, start, end, type, subject, notes, notif ->
                    viewModel.addSchedule(studentId, uiState.selectedDay, start, end, type, subject, notes, notif)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleBottomSheet(
    onDismiss: () -> Unit,
    students: Map<Long, String>,
    selectedDay: Int,
    onSave: (Long, String, String, String, String, String, Boolean) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.keys.firstOrNull() ?: 0L) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var sessionType by remember { mutableStateOf("NEW_MEMORIZATION") }
    var subject by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var notificationEnabled by remember { mutableStateOf(true) }
    var studentExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()
        ) {
            Text("إضافة موعد", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = studentExpanded, onExpandedChange = { studentExpanded = it }) {
                OutlinedTextField(
                    value = students[selectedStudentId] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                    label = { Text("الطالب") }
                )
                ExposedDropdownMenu(expanded = studentExpanded, onDismissRequest = { studentExpanded = false }) {
                    students.forEach { (id, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { selectedStudentId = id; studentExpanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("من") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("إلى") }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("نوع الحصة", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("NEW_MEMORIZATION" to "حفظ", "REVISION" to "مراجعة", "TAJWEED" to "تجويد", "QUIZ" to "اختبار").forEach { (value, label) ->
                    FilterChip(selected = sessionType == value, onClick = { sessionType = value }, label = { Text(label) })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("الموضوع") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("إشعار قبل 15 دقيقة", modifier = Modifier.weight(1f))
                Switch(checked = notificationEnabled, onCheckedChange = { notificationEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryLight))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(selectedStudentId, startTime, endTime, sessionType, subject, notes, notificationEnabled) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("حفظ") }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
