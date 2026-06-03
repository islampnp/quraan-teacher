package com.quraan.teacher.app.presentation.screens.learning_path

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quraan.teacher.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathGeneratorScreen(
    viewModel: LearningPathViewModel,
    studentId: Long,
    onBack: () -> Unit,
    onPathSaved: () -> Unit
) {
    LaunchedEffect(studentId) { viewModel.initialize(studentId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إنشاء مسار تعلم", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary, navigationIconContentColor = OnPrimary)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("التقييم", "الأهداف", "المراجعة").forEachIndexed { index, label ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = if (uiState.currentStep >= index) Primary else SurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${index + 1}", color = if (uiState.currentStep >= index) OnPrimary else TextMuted, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(label, style = MaterialTheme.typography.labelSmall, color = if (uiState.currentStep >= index) Primary else TextMuted)
                    }
                }
            }

            Divider()

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("جاري إنشاء المسار...", color = TextMuted)
                    }
                }
            } else if (uiState.success) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(64.dp), tint = Success)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("تم حفظ المسار بنجاح!", style = MaterialTheme.typography.titleLarge, color = Success)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onPathSaved, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                            Text("العودة")
                        }
                    }
                }
            } else {
                when (uiState.currentStep) {
                    0 -> Step1Evaluation(
                        studentName = uiState.studentName,
                        memorizedAyahs = uiState.memorizedAyahs,
                        averageGrade = uiState.averageGrade,
                        sessionFrequency = uiState.sessionFrequency,
                        weaknesses = uiState.weaknesses,
                        learningPace = uiState.learningPace,
                        availableDays = uiState.availableDays,
                        sessionDuration = uiState.sessionDuration,
                        priority = uiState.priority,
                        onToggleWeakness = { viewModel.toggleWeakness(it) },
                        onSetPace = { viewModel.setLearningPace(it) },
                        onSetDays = { viewModel.setAvailableDays(it) },
                        onSetDuration = { viewModel.setSessionDuration(it) },
                        onSetPriority = { viewModel.setPriority(it) },
                        onNext = { viewModel.setStep(1) }
                    )
                    1 -> Step2Goals(
                        targetSurah = uiState.targetSurah,
                        targetWeeks = uiState.targetWeeks,
                        revisionRatio = uiState.revisionRatio,
                        includeTajweed = uiState.includeTajweed,
                        includeWeeklyQuiz = uiState.includeWeeklyQuiz,
                        availableDays = uiState.availableDays,
                        onSetSurah = { viewModel.setTargetSurah(it) },
                        onSetWeeks = { viewModel.setTargetWeeks(it) },
                        onSetRevisionRatio = { viewModel.setRevisionRatio(it) },
                        onToggleTajweed = { viewModel.setIncludeTajweed(it) },
                        onToggleQuiz = { viewModel.setIncludeWeeklyQuiz(it) },
                        onGenerate = { viewModel.generateAndPreview() },
                        onBack = { viewModel.setStep(0) }
                    )
                    2 -> Step3Review(
                        pathTitle = uiState.generatedPathTitle,
                        weeks = uiState.generatedWeeks,
                        onSave = { viewModel.savePath() },
                        onBack = { viewModel.setStep(1) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step1Evaluation(
    studentName: String,
    memorizedAyahs: Int,
    averageGrade: Double,
    sessionFrequency: Int,
    weaknesses: List<String>,
    learningPace: String,
    availableDays: Int,
    sessionDuration: Int,
    priority: String,
    onToggleWeakness: (String) -> Unit,
    onSetPace: (String) -> Unit,
    onSetDays: (Int) -> Unit,
    onSetDuration: (Int) -> Unit,
    onSetPriority: (String) -> Unit,
    onNext: () -> Unit
) {
    val tajweedRules = listOf("مخارج الحروف", "المد", "الغنة", "الإدغام", "الإخفاء", "القلقلة", "الوقف والابتداء")
    val paces = listOf("بطيء", "متوسط", "سريع")
    val priorities = listOf("حفظ جديد", "مراجعة", "تجويد", "متوازن")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تقييم الطالب", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("الطالب", studentName)
                    InfoRow("الآيات المحفوظة", "$memorizedAyahs")
                    InfoRow("المعدل", "%.1f".format(averageGrade))
                    InfoRow("عدد الحصص شهرياً", "$sessionFrequency")
                }
            }
        }

        item {
            Text("نقاط الضعف في التجويد", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tajweedRules.forEach { rule ->
                    FilterChip(
                        selected = weaknesses.contains(rule),
                        onClick = { onToggleWeakness(rule) },
                        label = { Text(rule) }
                    )
                }
            }
        }

        item {
            Text("سرعة التعلم", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                paces.forEach { pace ->
                    FilterChip(selected = learningPace == pace, onClick = { onSetPace(pace) }, label = { Text(pace) })
                }
            }
        }

        item {
            Text("الأيام المتاحة في الأسبوع: $availableDays", style = MaterialTheme.typography.titleSmall)
            Slider(value = availableDays.toFloat(), onValueChange = { onSetDays(it.toInt()) }, valueRange = 1f..7f, steps = 5)
        }

        item {
            Text("مدة الحصة: $sessionDuration دقيقة", style = MaterialTheme.typography.titleSmall)
            Slider(value = sessionDuration.toFloat(), onValueChange = { onSetDuration(it.toInt()) }, valueRange = 15f..90f, steps = 14)
        }

        item {
            Text("الأولوية", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                priorities.forEach { p ->
                    FilterChip(selected = priority == p, onClick = { onSetPriority(p) }, label = { Text(p) })
                }
            }
        }

        item {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("التالي: تخصيص الأهداف")
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2Goals(
    targetSurah: String,
    targetWeeks: Int,
    revisionRatio: Int,
    includeTajweed: Boolean,
    includeWeeklyQuiz: Boolean,
    availableDays: Int,
    onSetSurah: (String) -> Unit,
    onSetWeeks: (Int) -> Unit,
    onSetRevisionRatio: (Int) -> Unit,
    onToggleTajweed: (Boolean) -> Unit,
    onToggleQuiz: (Boolean) -> Unit,
    onGenerate: () -> Unit,
    onBack: () -> Unit
) {
    val surahList = listOf(
        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف",
        "الأنفال", "التوبة", "يونس", "هود", "يوسف", "الرعد", "إبراهيم",
        "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه", "الأنبياء",
        "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص",
        "العنكبوت", "الروم", "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر",
        "يس", "الصافات", "ص", "الزمر", "غافر", "فصلت", "الشورى",
        "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات",
        "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة",
        "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف", "الجمعة", "المنافقون",
        "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
        "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات",
        "النبأ", "النازعات", "عبس", "التكوير", "الانفطار", "المطففين", "الانشقاق",
        "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس",
        "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة",
        "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل",
        "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص",
        "الفلق", "الناس"
    )
    var surahExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("السورة المستهدفة", style = MaterialTheme.typography.titleSmall)
            ExposedDropdownMenuBox(expanded = surahExpanded, onExpandedChange = { surahExpanded = it }) {
                OutlinedTextField(
                    value = targetSurah,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = surahExpanded) },
                    label = { Text("اختر السورة") }
                )
                ExposedDropdownMenu(expanded = surahExpanded, onDismissRequest = { surahExpanded = false }) {
                    surahList.forEach { surah ->
                        DropdownMenuItem(
                            text = { Text(surah) },
                            onClick = { onSetSurah(surah); surahExpanded = false }
                        )
                    }
                }
            }
        }

        item {
            Text("عدد الأسابيع: $targetWeeks", style = MaterialTheme.typography.titleSmall)
            Slider(value = targetWeeks.toFloat(), onValueChange = { onSetWeeks(it.toInt()) }, valueRange = 4f..52f, steps = 47)
        }

        item {
            val totalAyahs = com.quraan.teacher.app.domain.usecase.GenerateLearningPathUseCase.surahAyahMap[targetSurah] ?: 0
            val dailyGoal = if (targetWeeks > 0 && availableDays > 0) {
                (totalAyahs.toFloat() / targetWeeks / availableDays)
            } else 0f
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PrimaryLight.copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("الهدف اليومي المقترح: %.1f آية".format(dailyGoal), style = MaterialTheme.typography.titleSmall, color = Primary)
                    Text("إجمالي آيات السورة: $totalAyahs", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        }

        item {
            Text("نسبة المراجعة: $revisionRatio%", style = MaterialTheme.typography.titleSmall)
            Slider(value = revisionRatio.toFloat(), onValueChange = { onSetRevisionRatio(it.toInt()) }, valueRange = 0f..100f, steps = 19)
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("تمارين تجويد", modifier = Modifier.weight(1f))
                Switch(checked = includeTajweed, onCheckedChange = onToggleTajweed, colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryLight))
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("اختبار أسبوعي", modifier = Modifier.weight(1f))
                Switch(checked = includeWeeklyQuiz, onCheckedChange = onToggleQuiz, colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryLight))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("السابق") }
                Button(
                    onClick = onGenerate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = targetSurah.isNotBlank()
                ) { Text("معاينة المسار") }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun Step3Review(
    pathTitle: String,
    weeks: List<GeneratedWeek>,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(pathTitle, style = MaterialTheme.typography.titleLarge, color = Primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${weeks.size} أسبوع", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }

        items(weeks) { week ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الأسبوع ${week.weekNumber}", style = MaterialTheme.typography.titleSmall)
                        Text(week.surah, style = MaterialTheme.typography.bodySmall, color = Primary)
                    }
                    Text("الآيات: ${week.ayahRange}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Row {
                        Text("حفظ: ${week.newAyahs} آية", style = MaterialTheme.typography.labelSmall, color = Success)
                        if (week.revisionAyahs > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مراجعة: ${week.revisionAyahs}", style = MaterialTheme.typography.labelSmall, color = Info)
                        }
                    }
                    if (week.tajweedTopic.isNotBlank()) {
                        Text("تجويد: ${week.tajweedTopic}", style = MaterialTheme.typography.labelSmall, color = Warning)
                    }
                    if (week.hasQuiz) {
                        Text("📝 اختبار أسبوعي", style = MaterialTheme.typography.labelSmall, color = Gold)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("تعديل") }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("حفظ المسار") }
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
