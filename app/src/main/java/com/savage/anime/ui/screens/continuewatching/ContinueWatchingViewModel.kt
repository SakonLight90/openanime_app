package com.savage.anime.ui.screens.continuewatching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContinueWatchingViewModel @Inject constructor(
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ContinueWatchingItem>>(emptyList())
    val items: StateFlow<List<ContinueWatchingItem>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            localUserDataRepository.getContinueWatching().collect { list ->
                _items.value = list
            }
        }
    }

    fun removeFromContinueWatching(item: ContinueWatchingItem) {
        viewModelScope.launch {
            localUserDataRepository.clearAnimePosition(item.animeId)
        }
    }
}
