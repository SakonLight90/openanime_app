package com.savage.anime.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.WatchHistoryEntry
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val entries: List<WatchHistoryEntry> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            localUserDataRepository.getWatchHistory().collect { entries ->
                _uiState.value = HistoryUiState(entries = entries, isLoading = false)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            localUserDataRepository.clearWatchHistory()
        }
    }
}
