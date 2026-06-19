package com.savage.anime.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.CustomList
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadCustomLists()
    }

    fun loadWatchlist() {
        viewModelScope.launch {
            localUserDataRepository.getWatchlist().collect { watchlist ->
                _uiState.value = _uiState.value.copy(
                    watchlist = watchlist,
                    isRefreshing = false
                )
            }
        }
    }

    private fun loadCustomLists() {
        viewModelScope.launch {
            localUserDataRepository.getCustomLists().collect { lists ->
                _uiState.value = _uiState.value.copy(customLists = lists)
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadWatchlist()
    }
}

data class WatchlistUiState(
    val watchlist: List<Anime> = emptyList(),
    val customLists: List<CustomList> = emptyList(),
    val isRefreshing: Boolean = false
)
