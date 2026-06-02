package com.quraan.teacher.app.presentation.screens.students

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quraan.teacher.app.presentation.components.StudentCard
import com.quraan.teacher.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: com.quraan.teacher.app.presentation.screens.students.StudentsViewModel,
    onStudentClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الطلاب") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary, titleContentColor = OnPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = Gold,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل طالب جديد")
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("بحث عن طالب...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterLevel == "ALL",
                    onClick = { viewModel.onFilterChange("ALL") },
                    label = { Text("الكل") }
                )
                FilterChip(
                    selected = uiState.filterLevel == "BEGINNER",
                    onClick = { viewModel.onFilterChange("BEGINNER") },
                    label = { Text("مبتدئ") }
                )
                FilterChip(
                    selected = uiState.filterLevel == "INTERMEDIATE",
                    onClick = { viewModel.onFilterChange("INTERMEDIATE") },
                    label = { Text("متوسط") }
                )
                FilterChip(
                    selected = uiState.filterLevel == "ADVANCED",
                    onClick = { viewModel.onFilterChange("ADVANCED") },
                    label = { Text("متقدم") }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("لا يوجد طلاب", style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.students, key = { it.student.id }) { item ->
                        StudentCard(
                            student = item.student,
                            progressPercent = item.progressPercent,
                            lastSessionDaysAgo = item.lastSessionDaysAgo,
                            onClick = { onStudentClick(item.student.id) },
                            onDelete = { viewModel.deleteStudent(item.student) },
                            onViewPath = { onStudentClick(item.student.id) },
                            onEdit = { viewModel.showAddDialog() }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Add Student Bottom Sheet
        if (uiState.showAddDialog) {
            AddStudentBottomSheet(
                onDismiss = { viewModel.hideAddDialog() },
                onSave = { name, age, phone, guardian, guardianPhone, level, style, time ->
                    viewModel.addStudent(name, age, phone, guardian, guardianPhone, level, style, time)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStudentBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, Int, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("BEGINNER") }
    var selectedStyle by remember { mutableStateOf("MIXED") }
    var selectedTime by remember { mutableStateOf("MORNING") }

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Text("تسجيل طالب جديد", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم الكامل") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("العمر") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = guardianName, onValueChange = { guardianName = it }, label = { Text("اسم ولي الأمر") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = guardianPhone, onValueChange = { guardianPhone = it }, label = { Text("هاتف ولي الأمر") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))
            Text("المستوى", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BEGINNER" to "مبتدئ", "INTERMEDIATE" to "متوسط", "ADVANCED" to "متقدم").forEach { (value, label) ->
                    FilterChip(selected = selectedLevel == value, onClick = { selectedLevel = value }, label = { Text(label) })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("أسلوب التعلم", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("VISUAL" to "بصري", "AUDITORY" to "سمعي", "REPETITIVE" to "تكرار", "MIXED" to "مختلط").forEach { (value, label) ->
                    FilterChip(selected = selectedStyle == value, onClick = { selectedStyle = value }, label = { Text(label) })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("الوقت المفضل", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MORNING" to "صباحية", "AFTERNOON" to "بعد الظهر", "EVENING" to "مسائية").forEach { (value, label) ->
                    FilterChip(selected = selectedTime == value, onClick = { selectedTime = value }, label = { Text(label) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val ageInt = age.toIntOrNull() ?: 0
                    onSave(name, ageInt, phone, guardianName, guardianPhone, selectedLevel, selectedStyle, selectedTime)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = name.isNotBlank()
            ) {
                Text("حفظ")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
