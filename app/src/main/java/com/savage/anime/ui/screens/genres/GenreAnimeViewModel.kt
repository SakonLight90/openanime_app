package com.savage.anime.ui.screens.genres

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
import javax.inject.Inject

data class GenreAnimeUiState(
    val items: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val total: Int = 0
)

@HiltViewModel
class GenreAnimeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenreAnimeUiState())
    val uiState: StateFlow<GenreAnimeUiState> = _uiState.asStateFlow()

    private var currentGenreId: Int = 0

    fun loadGenreAnime(genreId: Int) {
        currentGenreId = genreId
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val response = animeRepository.getGenreAnime(genreId, 1).first()
                _uiState.value = _uiState.value.copy(
                    items = response.items,
                    page = response.page,
                    hasMore = response.hasMore,
                    total = response.total,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Errore caricamento"
                )
            }
        }
    }

    fun showMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoadingMore || currentGenreId == 0) return

        _uiState.value = state.copy(isLoadingMore = true)
        viewModelScope.launch {
            try {
                val response = animeRepository.getGenreAnime(currentGenreId, state.page + 1).first()
                _uiState.value = _uiState.value.copy(
                    items = _uiState.value.items + response.items,
                    page = response.page,
                    hasMore = response.hasMore,
                    total = response.total,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }

    fun refresh() {
        if (currentGenreId == 0) return
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadGenreAnime(currentGenreId)
    }
}
