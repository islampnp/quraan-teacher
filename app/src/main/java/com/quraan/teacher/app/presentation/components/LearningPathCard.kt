package com.quraan.teacher.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quraan.teacher.app.domain.model.LearningMilestone
import com.quraan.teacher.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LearningPathCard(
    title: String,
    progressPercent: Float,
    currentWeek: Int,
    totalWeeks: Int,
    weeklyGoal: Int,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary
                )
                ProgressRing(
                    progress = progressPercent,
                    size = 48.dp,
                    strokeWidth = 5.dp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip(label = "الأسبوع", value = "$currentWeek / $totalWeeks", icon = { Icon(Icons.Default.Schedule, null, Modifier.size(16.dp)) })
                InfoChip(label = "الهدف الأسبوعي", value = "$weeklyGoal آية", icon = { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = Success) })
            }
        }
    }
}

@Composable
fun MilestoneRow(
    milestone: LearningMilestone,
    modifier: Modifier = Modifier
) {
    val statusIcon: @Composable () -> Unit = {
        when {
            milestone.isCompleted -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(24.dp)
            )
            else -> Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    val statusColor = when {
        milestone.isCompleted -> Success
        else -> TextMuted
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isCompleted) Success.copy(alpha = 0.05f) else Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            statusIcon()

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "الأسبوع ${milestone.weekNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    color = statusColor
                )
                Text(
                    text = "${milestone.targetSurah} (${milestone.targetAyahFrom}-${milestone.targetAyahTo})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Row {
                    Text(
                        text = "حفظ: ${milestone.targetMemorizedAyahs} آية",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    if (milestone.targetRevisionAyahs > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مراجعة: ${milestone.targetRevisionAyahs}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (milestone.isCompleted) {
                    Text(
                        text = "${milestone.actualAyahsAchieved}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Success
                    )
                    Text(
                        text = "آية",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                if (milestone.difficultyRating > 0) {
                    Text(
                        text = "★".repeat(milestone.difficultyRating),
                        style = MaterialTheme.typography.labelSmall,
                        color = DifficultyStar
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    icon: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.titleSmall)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}
