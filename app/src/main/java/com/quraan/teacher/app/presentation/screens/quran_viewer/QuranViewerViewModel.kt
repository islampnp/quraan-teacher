package com.quraan.teacher.app.presentation.screens.quran_viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quraan.teacher.app.data.repository.StudentRepository
import com.quraan.teacher.app.data.local.entities.StudentEntity
import com.quraan.teacher.app.domain.usecase.GenerateLearningPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SurahInfo(
    val name: String,
    val number: Int,
    val ayahCount: Int,
    val revelType: String = "مكية"
)

data class QuranViewerUiState(
    val isLoading: Boolean = true,
    val surahList: List<SurahInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedSurah: SurahInfo? = null,
    val students: List<StudentEntity> = emptyList(),
    val highlightedAyahs: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class QuranViewerViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranViewerUiState())
    val uiState: StateFlow<QuranViewerUiState> = _uiState.asStateFlow()

    private val allSurahs = listOf(
        SurahInfo("الفاتحة", 1, 7, "مكية"),
        SurahInfo("البقرة", 2, 286, "مدنية"),
        SurahInfo("آل عمران", 3, 200, "مدنية"),
        SurahInfo("النساء", 4, 176, "مدنية"),
        SurahInfo("المائدة", 5, 120, "مدنية"),
        SurahInfo("الأنعام", 6, 165, "مكية"),
        SurahInfo("الأعراف", 7, 206, "مكية"),
        SurahInfo("الأنفال", 8, 75, "مدنية"),
        SurahInfo("التوبة", 9, 129, "مدنية"),
        SurahInfo("يونس", 10, 109, "مكية"),
        SurahInfo("هود", 11, 123, "مكية"),
        SurahInfo("يوسف", 12, 111, "مكية"),
        SurahInfo("الرعد", 13, 43, "مدنية"),
        SurahInfo("إبراهيم", 14, 52, "مكية"),
        SurahInfo("الحجر", 15, 99, "مكية"),
        SurahInfo("النحل", 16, 128, "مكية"),
        SurahInfo("الإسراء", 17, 111, "مكية"),
        SurahInfo("الكهف", 18, 110, "مكية"),
        SurahInfo("مريم", 19, 98, "مكية"),
        SurahInfo("طه", 20, 135, "مكية"),
        SurahInfo("الأنبياء", 21, 112, "مكية"),
        SurahInfo("الحج", 22, 78, "مدنية"),
        SurahInfo("المؤمنون", 23, 118, "مكية"),
        SurahInfo("النور", 24, 64, "مدنية"),
        SurahInfo("الفرقان", 25, 77, "مكية"),
        SurahInfo("الشعراء", 26, 227, "مكية"),
        SurahInfo("النمل", 27, 93, "مكية"),
        SurahInfo("القصص", 28, 88, "مكية"),
        SurahInfo("العنكبوت", 29, 69, "مكية"),
        SurahInfo("الروم", 30, 60, "مكية"),
        SurahInfo("لقمان", 31, 34, "مكية"),
        SurahInfo("السجدة", 32, 30, "مكية"),
        SurahInfo("الأحزاب", 33, 73, "مدنية"),
        SurahInfo("سبأ", 34, 54, "مكية"),
        SurahInfo("فاطر", 35, 45, "مكية"),
        SurahInfo("يس", 36, 83, "مكية"),
        SurahInfo("الصافات", 37, 182, "مكية"),
        SurahInfo("ص", 38, 88, "مكية"),
        SurahInfo("الزمر", 39, 75, "مكية"),
        SurahInfo("غافر", 40, 85, "مكية"),
        SurahInfo("فصلت", 41, 54, "مكية"),
        SurahInfo("الشورى", 42, 53, "مكية"),
        SurahInfo("الزخرف", 43, 89, "مكية"),
        SurahInfo("الدخان", 44, 59, "مكية"),
        SurahInfo("الجاثية", 45, 37, "مكية"),
        SurahInfo("الأحقاف", 46, 35, "مكية"),
        SurahInfo("محمد", 47, 38, "مدنية"),
        SurahInfo("الفتح", 48, 29, "مدنية"),
        SurahInfo("الحجرات", 49, 18, "مدنية"),
        SurahInfo("ق", 50, 45, "مكية"),
        SurahInfo("الذاريات", 51, 60, "مكية"),
        SurahInfo("الطور", 52, 49, "مكية"),
        SurahInfo("النجم", 53, 62, "مكية"),
        SurahInfo("القمر", 54, 55, "مكية"),
        SurahInfo("الرحمن", 55, 78, "مدنية"),
        SurahInfo("الواقعة", 56, 96, "مكية"),
        SurahInfo("الحديد", 57, 29, "مدنية"),
        SurahInfo("المجادلة", 58, 22, "مدنية"),
        SurahInfo("الحشر", 59, 24, "مدنية"),
        SurahInfo("الممتحنة", 60, 13, "مدنية"),
        SurahInfo("الصف", 61, 14, "مدنية"),
        SurahInfo("الجمعة", 62, 11, "مدنية"),
        SurahInfo("المنافقون", 63, 11, "مدنية"),
        SurahInfo("التغابن", 64, 18, "مدنية"),
        SurahInfo("الطلاق", 65, 12, "مدنية"),
        SurahInfo("التحريم", 66, 12, "مدنية"),
        SurahInfo("الملك", 67, 30, "مكية"),
        SurahInfo("القلم", 68, 52, "مكية"),
        SurahInfo("الحاقة", 69, 52, "مكية"),
        SurahInfo("المعارج", 70, 44, "مكية"),
        SurahInfo("نوح", 71, 28, "مكية"),
        SurahInfo("الجن", 72, 28, "مكية"),
        SurahInfo("المزمل", 73, 20, "مكية"),
        SurahInfo("المدثر", 74, 56, "مكية"),
        SurahInfo("القيامة", 75, 40, "مكية"),
        SurahInfo("الإنسان", 76, 31, "مدنية"),
        SurahInfo("المرسلات", 77, 50, "مكية"),
        SurahInfo("النبأ", 78, 40, "مكية"),
        SurahInfo("النازعات", 79, 46, "مكية"),
        SurahInfo("عبس", 80, 42, "مكية"),
        SurahInfo("التكوير", 81, 29, "مكية"),
        SurahInfo("الانفطار", 82, 19, "مكية"),
        SurahInfo("المطففين", 83, 36, "مكية"),
        SurahInfo("الانشقاق", 84, 25, "مكية"),
        SurahInfo("البروج", 85, 22, "مكية"),
        SurahInfo("الطارق", 86, 17, "مكية"),
        SurahInfo("الأعلى", 87, 19, "مكية"),
        SurahInfo("الغاشية", 88, 26, "مكية"),
        SurahInfo("الفجر", 89, 30, "مكية"),
        SurahInfo("البلد", 90, 20, "مكية"),
        SurahInfo("الشمس", 91, 15, "مكية"),
        SurahInfo("الليل", 92, 21, "مكية"),
        SurahInfo("الضحى", 93, 11, "مكية"),
        SurahInfo("الشرح", 94, 8, "مكية"),
        SurahInfo("التين", 95, 8, "مكية"),
        SurahInfo("العلق", 96, 19, "مكية"),
        SurahInfo("القدر", 97, 5, "مكية"),
        SurahInfo("البينة", 98, 8, "مدنية"),
        SurahInfo("الزلزلة", 99, 8, "مدنية"),
        SurahInfo("العاديات", 100, 11, "مكية"),
        SurahInfo("القارعة", 101, 11, "مكية"),
        SurahInfo("التكاثر", 102, 8, "مكية"),
        SurahInfo("العصر", 103, 3, "مكية"),
        SurahInfo("الهمزة", 104, 9, "مكية"),
        SurahInfo("الفيل", 105, 5, "مكية"),
        SurahInfo("قريش", 106, 4, "مكية"),
        SurahInfo("الماعون", 107, 7, "مكية"),
        SurahInfo("الكوثر", 108, 3, "مكية"),
        SurahInfo("الكافرون", 109, 6, "مكية"),
        SurahInfo("النصر", 110, 3, "مدنية"),
        SurahInfo("المسد", 111, 5, "مكية"),
        SurahInfo("الإخلاص", 112, 4, "مكية"),
        SurahInfo("الفلق", 113, 5, "مكية"),
        SurahInfo("الناس", 114, 6, "مكية")
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    surahList = allSurahs
                )
            }

            studentRepository.getAllActiveStudents().collect { students ->
                val highlighted = mutableSetOf<String>()
                students.forEach { s ->
                    if (s.currentSurah.isNotBlank()) {
                        highlighted.add("${s.currentSurah}:${s.currentAyah}")
                    }
                }
                _uiState.update { it.copy(students = students, highlightedAyahs = highlighted) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        val filtered = if (query.isBlank()) allSurahs
        else allSurahs.filter { it.name.contains(query) }
        _uiState.update { it.copy(searchQuery = query, surahList = filtered) }
    }

    fun selectSurah(surah: SurahInfo) {
        _uiState.update { it.copy(selectedSurah = surah) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedSurah = null) }
    }
}
