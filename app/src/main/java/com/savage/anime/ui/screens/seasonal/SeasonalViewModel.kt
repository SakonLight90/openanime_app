package com.savage.anime.ui.screens.seasonal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SeasonalUiState(
    val seasons: Map<String, List<Anime>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SeasonalViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeasonalUiState())
    val uiState: StateFlow<SeasonalUiState> = _uiState.asStateFlow()

    init {
        loadSeasonal()
    }

    fun loadSeasonal() {
        viewModelScope.launch {
            try {
                val popular = animeRepository.getPopular().first()
                val ongoing = animeRepository.getOngoing().first()
                val newest = animeRepository.getNewest().first()
                val upcoming = animeRepository.getUpcoming().first()

                val allAnime = (popular + ongoing + newest + upcoming).distinctBy { it.id }
                val seasons = groupBySeason(allAnime)
                _uiState.value = SeasonalUiState(seasons = seasons, isLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Errore caricamento stagionali")
            }
        }
    }

    private fun groupBySeason(animeList: List<Anime>): Map<String, List<Anime>> {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1

        val currentSeason = getSeasonName(currentMonth)
        val labels = listOf(
            "${currentSeason} $currentYear" to animeList.filter {
                val season = parseSeason(it.releaseDate)
                season == "${currentSeason} $currentYear"
            },
            "Prossimamente" to animeList.filter {
                val season = parseSeason(it.releaseDate)
                season.isNotEmpty() && season != "${currentSeason} $currentYear"
            },
            "Altri" to animeList.filter {
                val season = parseSeason(it.releaseDate)
                season.isEmpty()
            }
        )

        return labels
            .filter { it.second.isNotEmpty() }
            .toMap()
    }

    private fun parseSeason(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return try {
            val parts = dateStr.split("-")
            if (parts.size >= 2) {
                val year = parts[0].toIntOrNull() ?: return ""
                val month = parts[1].toIntOrNull() ?: return ""
                "${getSeasonName(month)} $year"
            } else {
                ""
            }
        } catch (_: Exception) { "" }
    }

    private fun getSeasonName(month: Int): String = when {
        month in 1..3 -> "Inverno"
        month in 4..6 -> "Primavera"
        month in 7..9 -> "Estate"
        else -> "Autunno"
    }
}
