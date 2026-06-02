package com.quraan.teacher.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quraan.teacher.app.data.local.entities.StudentEntity
import com.quraan.teacher.app.presentation.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentCard(
    student: StudentEntity,
    progressPercent: Float,
    lastSessionDaysAgo: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onViewPath: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val initials = student.fullName.take(2)

    val levelColor = when (student.level) {
        "BEGINNER" -> ChipBeginner to ChipBeginnerText
        "INTERMEDIATE" -> ChipIntermediate to ChipIntermediateText
        "ADVANCED" -> ChipAdvanced to ChipAdvancedText
        else -> ChipBeginner to ChipBeginnerText
    }

    val daysColor = when {
        lastSessionDaysAgo == null -> Color.Gray
        lastSessionDaysAgo <= 3 -> Success
        lastSessionDaysAgo <= 7 -> Warning
        else -> Danger
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = OnPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = levelColor.first
                    ) {
                        Text(
                            text = when (student.level) {
                                "BEGINNER" -> "مبتدئ"
                                "INTERMEDIATE" -> "متوسط"
                                "ADVANCED" -> "متقدم"
                                else -> student.level
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = levelColor.second,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "السورة: ${student.currentSurah}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = progressPercent,
                        size = 32.dp,
                        strokeWidth = 3.dp,
                        showPercentage = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(progressPercent * 100).toInt()}% من المسار",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (lastSessionDaysAgo != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = daysColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = when {
                                    lastSessionDaysAgo == 0 -> "اليوم"
                                    lastSessionDaysAgo == 1 -> "أمس"
                                    else -> "منذ $lastSessionDaysAgo أيام"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = daysColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("عرض المسار") },
                onClick = { showMenu = false; onViewPath() },
                leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("تعديل") },
                onClick = { showMenu = false; onEdit() },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("حذف", color = Danger) },
                onClick = { showMenu = false; onDelete() },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Danger) }
            )
        }
    }
}
