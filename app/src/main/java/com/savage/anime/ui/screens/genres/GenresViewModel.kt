package com.savage.anime.ui.screens.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.Genre
import com.savage.anime.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenresUiState())
    val uiState: StateFlow<GenresUiState> = _uiState.asStateFlow()

    fun loadGenres() {
        viewModelScope.launch {
            try {
                val genres = animeRepository.getGenres()
                _uiState.value = _uiState.value.copy(genres = genres, isLoading = false, isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = "Errore caricamento generi")
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadGenres()
    }
}

data class GenresUiState(
    val genres: List<Genre> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)