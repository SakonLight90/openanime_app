package com.savage.anime.ui.screens.azlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AzListViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AzListUiState())
    val uiState: StateFlow<AzListUiState> = _uiState.asStateFlow()

    private val alphabet = ('A'..'Z').map { it.toString() }
    private var loadJob: Job? = null

    init {
        loadLetter("A")
    }

    fun getAlphabet(): List<String> = alphabet

    fun loadLetter(letter: String) {
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(selectedLetter = letter, isLoading = true, error = null)
        loadJob = viewModelScope.launch {
            try {
                animeRepository.getAzList(letter).collect { results ->
                    if (_uiState.value.selectedLetter == letter) {
                        _uiState.value = _uiState.value.copy(
                            results = results,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
            } catch (e: Exception) {
                if (_uiState.value.selectedLetter == letter) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Errore caricamento"
                    )
                }
            }
        }
    }

    fun refresh() {
        val letter = _uiState.value.selectedLetter
        if (letter.isBlank()) return
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadLetter(letter)
    }
}

data class AzListUiState(
    val selectedLetter: String = "A",
    val results: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)