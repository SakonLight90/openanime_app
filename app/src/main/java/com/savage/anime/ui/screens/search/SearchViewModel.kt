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

data class SearchUiState(
    val query: String = "",
    val results: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingAll: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedType: String? = null,
    val selectedStatus: String? = null,
    val selectedYear: String? = null,
    val selectedLanguage: String? = null,
    val displayLimit: Int = PAGE_SIZE,
    val allResultsCount: Int = 0,
    val page: Int = 1,
    val hasMore: Boolean = false
) {
    val hasActiveFilters: Boolean
        get() = selectedType != null || selectedStatus != null || selectedYear != null || selectedLanguage != null

    companion object {
        const val PAGE_SIZE = 30
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var searchSequence = 0
    private var browseResults: List<Anime> = emptyList()

    init {
        loadAllAnime()
    }

    private fun loadAllAnime() {
        if (browseResults.isNotEmpty()) return
        _uiState.value = _uiState.value.copy(isLoadingAll = true)
        viewModelScope.launch {
            val all = mutableListOf<Anime>()
            val seen = mutableSetOf<Int>()
            val letters = listOf("0-9") + ('a'..'z').map { it.toString() }

            for (letter in letters) {
                try {
                    animeRepository.getAzList(letter).first().forEach { anime ->
                        if (seen.add(anime.id)) all.add(anime)
                    }
                } catch (_: Exception) {
                }
            }

            browseResults = all
            _uiState.value = _uiState.value.copy(isLoadingAll = false)
            if (_uiState.value.query.isEmpty()) applyBrowseResults()
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        val currentSeq = ++searchSequence
        _uiState.value = _uiState.value.copy(
            query = query,
            displayLimit = SearchUiState.PAGE_SIZE,
            results = emptyList(),
            allResultsCount = 0,
            page = 1,
            hasMore = false,
            error = null
        )

        if (query.isBlank()) {
            applyBrowseResults()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            if (currentSeq != searchSequence) return@launch

            _uiState.value = _uiState.value.copy(isLoading = true, isRefreshing = false, error = null)

            try {
                animeRepository.search(
                    query = query,
                    type = _uiState.value.selectedType,
                    status = _uiState.value.selectedStatus,
                    year = _uiState.value.selectedYear,
                    language = _uiState.value.selectedLanguage
                ).collectLatest { response ->
                    if (currentSeq != searchSequence) return@collectLatest
                    _uiState.value = _uiState.value.copy(
                        results = response.items,
                        allResultsCount = response.total,
                        page = response.page,
                        hasMore = response.hasMore,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
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
        val query = _uiState.value.query
        if (query.isBlank()) {
            loadAllAnime()
            return
        }
        search(query)
    }

    fun setTypeFilter(type: String?) = applyFilter { copy(selectedType = type) }

    fun setStatusFilter(status: String?) = applyFilter { copy(selectedStatus = status) }

    fun setYearFilter(year: String?) = applyFilter { copy(selectedYear = year) }

    fun setLanguageFilter(language: String?) = applyFilter { copy(selectedLanguage = language) }

    fun clearFilters() {
        applyFilter {
            copy(
                selectedType = null,
                selectedStatus = null,
                selectedYear = null,
                selectedLanguage = null
            )
        }
    }

    /**
     * I filtri non filtrano piu' la lista gia' caricata: rilanciano la
     * ricerca sul server, altrimenti un filtro non riflesso nei dati
     * (per esempio l'anno, nato quando release_date era sempre null)
     * continuerebbe a non produrre risultati.
     */
    private fun applyFilter(transform: SearchUiState.() -> SearchUiState) {
        val updated = _uiState.value.transform().copy(
            displayLimit = SearchUiState.PAGE_SIZE,
            page = 1,
            hasMore = false
        )
        _uiState.value = updated

        if (updated.query.isBlank()) {
            applyBrowseResults()
        } else {
            search(updated.query)
        }
    }

    private fun applyBrowseResults() {
        val state = _uiState.value
        val filtered = browseResults.filterLocal(state)
        _uiState.value = state.copy(
            results = filtered.take(state.displayLimit),
            allResultsCount = filtered.size,
            hasMore = filtered.size > state.displayLimit,
            isLoading = false,
            isRefreshing = false,
            error = null
        )
    }

    private fun List<Anime>.filterLocal(state: SearchUiState): List<Anime> {
        var filtered = this
        state.selectedType?.takeIf { it.isNotBlank() }?.let { type ->
            filtered = filtered.filter { it.type.equals(type, ignoreCase = true) }
        }
        state.selectedStatus?.takeIf { it.isNotBlank() }?.let { status ->
            filtered = filtered.filter { it.status.equals(status, ignoreCase = true) }
        }
        state.selectedYear?.takeIf { it.isNotBlank() }?.let { year ->
            filtered = filtered.filter { it.releaseYear?.toString() == year }
        }
        state.selectedLanguage?.takeIf { it.isNotBlank() }?.let { lang ->
            filtered = filtered.filter { it.language == lang }
        }
        return filtered
    }

    fun showMore() {
        val state = _uiState.value

        if (state.query.isBlank()) {
            _uiState.value = state.copy(displayLimit = state.displayLimit + SearchUiState.PAGE_SIZE)
            applyBrowseResults()
            return
        }

        if (!state.hasMore || state.isLoadingMore) return

        val currentSeq = searchSequence
        _uiState.value = state.copy(isLoadingMore = true)

        searchJob = viewModelScope.launch {
            try {
                val nextPage = state.page + 1
                val response = animeRepository.search(
                    query = state.query,
                    type = state.selectedType,
                    status = state.selectedStatus,
                    year = state.selectedYear,
                    language = state.selectedLanguage,
                    page = nextPage
                ).first()

                if (currentSeq != searchSequence) return@launch

                _uiState.value = _uiState.value.copy(
                    results = (_uiState.value.results + response.items).distinctBy { it.id },
                    allResultsCount = response.total,
                    page = response.page,
                    hasMore = response.hasMore,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                if (currentSeq == searchSequence) {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun clearResults() {
        searchSequence++
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            query = "",
            results = emptyList(),
            isLoading = false,
            isRefreshing = false,
            isLoadingMore = false,
            error = null,
            displayLimit = SearchUiState.PAGE_SIZE,
            allResultsCount = 0,
            page = 1,
            hasMore = false
        )
        applyBrowseResults()
    }
}
