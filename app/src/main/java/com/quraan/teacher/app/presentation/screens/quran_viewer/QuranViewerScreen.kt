package com.quraan.teacher.app.presentation.screens.quran_viewer

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
import com.quraan.teacher.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranViewerScreen(
    viewModel: QuranViewerViewModel,
    onStudentClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.selectedSurah == null) "المصحف" else uiState.selectedSurah!!.name) },
                navigationIcon = {
                    if (uiState.selectedSurah != null) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = OnPrimary, navigationIconContentColor = OnPrimary)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.selectedSurah == null) {
                // Surah List
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("بحث عن سورة...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.surahList) { surah ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.selectSurah(surah) },
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = PrimaryLight.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${surah.number}", color = Primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(surah.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text("${surah.ayahCount} آية - ${surah.revelType}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Icon(Icons.Default.ChevronLeft, null, tint = TextMuted)
                            }
                        }
                    }
                }
            } else {
                // Surah Detail View (simplified - showing ayah placeholders)
                val surah = uiState.selectedSurah!!
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PrimaryLight.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", style = MaterialTheme.typography.headlineMedium, color = Primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("سورة ${surah.name}", style = MaterialTheme.typography.titleMedium, color = Primary)
                                Text("${surah.ayahCount} آية", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }

                    items(1..surah.ayahCount) { ayahNumber ->
                        val isHighlighted = uiState.highlightedAyahs.contains("${surah.name}:$ayahNumber")
                        com.quraan.teacher.app.presentation.components.QuranAyahCard(
                            ayahNumber = ayahNumber,
                            ayahText = "آية $ayahNumber من سورة ${surah.name}",
                            isHighlighted = isHighlighted
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}
