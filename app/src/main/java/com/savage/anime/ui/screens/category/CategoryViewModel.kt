package com.savage.anime.ui.screens.category

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

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private var currentCategory: String = ""

    fun loadCategory(category: String) {
        currentCategory = category
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val items = when (category) {
                    "Popolari" -> animeRepository.getPopular().first()
                    "In Corso" -> animeRepository.getOngoing().first()
                    "Imminenti" -> animeRepository.getUpcoming().first()
                    "Nuovi" -> animeRepository.getNewest().first()
                    "Aggiornati" -> animeRepository.getUpdatedFeed().first()
                    else -> emptyList()
                }
                val limit = _uiState.value.displayLimit
                _uiState.value = _uiState.value.copy(
                    allItems = items,
                    items = items.take(limit),
                    isLoading = false,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = "Errore caricamento")
            }
        }
    }

    fun showMore() {
        val newLimit = _uiState.value.displayLimit + 30
        _uiState.value = _uiState.value.copy(
            displayLimit = newLimit,
            items = _uiState.value.allItems.take(newLimit)
        )
    }

    fun refresh() {
        if (currentCategory.isBlank()) return
        _uiState.value = _uiState.value.copy(isRefreshing = true, displayLimit = 30)
        loadCategory(currentCategory)
    }
}

data class CategoryUiState(
    val items: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val displayLimit: Int = 30,
    val allItems: List<Anime> = emptyList()
)