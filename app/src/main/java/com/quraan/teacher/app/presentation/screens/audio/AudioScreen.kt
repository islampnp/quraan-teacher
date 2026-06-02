package com.quraan.teacher.app.presentation.screens.audio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quraan.teacher.app.presentation.components.AudioPlayer
import com.quraan.teacher.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioScreen(viewModel: AudioViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التسجيلات الصوتية") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary)
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.audioGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MicOff, null, Modifier.size(64.dp), tint = TextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("لا توجد تسجيلات", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.audioGroups) { group ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        onClick = { expanded = !expanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null, tint = Primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(group.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${group.recordings.size} تسجيل", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Icon(
                                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    null, tint = TextMuted
                                )
                            }

                            if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                group.recordings.forEach { recording ->
                                    var rating by remember(recording.id) { mutableIntStateOf(recording.teacherRating) }
                                    var feedback by remember(recording.id) { mutableStateOf(recording.feedback) }
                                    var showFeedbackField by remember { mutableStateOf(false) }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("سورة ${recording.surahName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

                                            Spacer(modifier = Modifier.height(8.dp))

                                            AudioPlayer(
                                                filePath = recording.filePath,
                                                durationSeconds = recording.durationSeconds
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Rating stars
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("التقييم: ", style = MaterialTheme.typography.labelSmall)
                                                for (i in 1..5) {
                                                    IconButton(
                                                        onClick = {
                                                            rating = i
                                                            viewModel.updateRating(recording.id, i)
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                            null,
                                                            tint = DifficultyStar,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            TextButton(onClick = { showFeedbackField = !showFeedbackField }) {
                                                Text(if (showFeedbackField) "إخفاء" else "إضافة ملاحظات", style = MaterialTheme.typography.labelSmall)
                                            }

                                            if (showFeedbackField) {
                                                OutlinedTextField(
                                                    value = feedback,
                                                    onValueChange = {
                                                        feedback = it
                                                        viewModel.updateFeedback(recording.id, it)
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    label = { Text("ملاحظات المعلّم") },
                                                    minLines = 2
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
