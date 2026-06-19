package com.savage.anime.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var searchSequence = 0
    private var rawResults: List<Anime> = emptyList()
    private var allAnimeResults: List<Anime> = emptyList()

    init {
        loadAllAnime()
    }

    private fun loadAllAnime() {
        if (allAnimeResults.isNotEmpty()) return
        _uiState.value = _uiState.value.copy(isLoadingAll = true)
        viewModelScope.launch {
            try {
                val letters = ('0'..'9').map { it.toString() } + ('a'..'z').map { it.toString() }
                val all = mutableListOf<Anime>()
                val seenIds = mutableSetOf<Int>()

                for (letter in letters) {
                    try {
                        val list = animeRepository.getAzList(letter).first()
                        for (anime in list) {
                            if (seenIds.add(anime.id)) {
                                all.add(anime)
                            }
                        }
                    } catch (_: Exception) { }
                }

                allAnimeResults = all
            } catch (_: Exception) { } finally {
                _uiState.value = _uiState.value.copy(isLoadingAll = false)
                if (allAnimeResults.isNotEmpty() && _uiState.value.query.isEmpty()) {
                    rawResults = allAnimeResults
                    applyAndUpdate()
                }
            }
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        val currentSeq = ++searchSequence
        _uiState.value = _uiState.value.copy(query = query, displayLimit = 30)
        searchJob = viewModelScope.launch {
            delay(300)

            if (currentSeq != searchSequence) return@launch

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isRefreshing = false,
                error = null
            )

            try {
                animeRepository.search(query).collectLatest { results ->
                    if (currentSeq == searchSequence) {
                        rawResults = results
                        val filtered = applyFilters(results)
                        _uiState.value = _uiState.value.copy(
                            results = filtered.take(30),
                            allResultsCount = filtered.size,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                if (currentSeq == searchSequence) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Errore durante la ricerca"
                    )
                }
            }
        }
    }

    fun refresh() {
        searchJob?.cancel()
        val currentSeq = ++searchSequence
        val query = _uiState.value.query
        if (query.isBlank()) return

        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, displayLimit = 30)
            delay(300)

            if (currentSeq != searchSequence) return@launch

            try {
                animeRepository.search(query).collectLatest { results ->
                    if (currentSeq == searchSequence) {
                        rawResults = results
                        val filtered = applyFilters(results)
                        _uiState.value = _uiState.value.copy(
                            results = filtered.take(30),
                            allResultsCount = filtered.size,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                if (currentSeq == searchSequence) {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = "Errore durante la ricerca"
                    )
                }
            }
        }
    }

    fun showMore() {
        val current = _uiState.value
        val newLimit = current.displayLimit + 30
        val filtered = applyFilters(rawResults)
        _uiState.value = current.copy(
            displayLimit = newLimit,
            results = filtered.take(newLimit)
        )
    }

    fun setTypeFilter(type: String?) {
        _uiState.value = _uiState.value.copy(selectedType = type, displayLimit = 30)
        applyAndUpdate()
    }

    fun setStatusFilter(status: String?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status, displayLimit = 30)
        applyAndUpdate()
    }

    fun setYearFilter(year: String?) {
        _uiState.value = _uiState.value.copy(selectedYear = year, displayLimit = 30)
        applyAndUpdate()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedType = null,
            selectedStatus = null,
            selectedYear = null,
            displayLimit = 30
        )
        applyAndUpdate()
    }

    private fun applyAndUpdate() {
        val filtered = applyFilters(rawResults)
        _uiState.value = _uiState.value.copy(
            results = filtered.take(30),
            allResultsCount = filtered.size
        )
    }

    private fun applyFilters(results: List<Anime>): List<Anime> {
        var filtered = results
        val type = _uiState.value.selectedType
        if (!type.isNullOrBlank()) {
            filtered = filtered.filter { it.type.equals(type, ignoreCase = true) }
        }
        val status = _uiState.value.selectedStatus
        if (!status.isNullOrBlank()) {
            filtered = filtered.filter { it.status.equals(status, ignoreCase = true) }
        }
        val year = _uiState.value.selectedYear
        if (!year.isNullOrBlank()) {
            filtered = filtered.filter { anime ->
                anime.releaseDate?.startsWith(year) == true
            }
        }
        return filtered
    }

    fun clearResults() {
        searchSequence++
        searchJob?.cancel()
        val filters = _uiState.value.copy(
            query = "",
            results = emptyList(),
            isLoading = false,
            isRefreshing = false,
            error = null,
            displayLimit = 30,
            allResultsCount = 0
        )
        _uiState.value = filters
        if (allAnimeResults.isNotEmpty()) {
            rawResults = allAnimeResults
            applyAndUpdate()
        }
    }

}

data class SearchUiState(
    val query: String = "",
    val results: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingAll: Boolean = false,
    val error: String? = null,
    val selectedType: String? = null,
    val selectedStatus: String? = null,
    val selectedYear: String? = null,
    val displayLimit: Int = 30,
    val allResultsCount: Int = 0
)
