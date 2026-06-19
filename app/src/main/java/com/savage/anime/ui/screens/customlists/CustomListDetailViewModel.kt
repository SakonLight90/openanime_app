package com.savage.anime.ui.screens.customlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.CustomList
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomListDetailUiState(
    val listName: String = "",
    val animeIds: List<Int> = emptyList(),
    val otherLists: List<CustomList> = emptyList(),
    val showMoveDialog: Boolean = false,
    val moveAnimeId: Int = 0,
    val currentListId: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CustomListDetailViewModel @Inject constructor(
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomListDetailUiState())
    val uiState: StateFlow<CustomListDetailUiState> = _uiState.asStateFlow()

    fun loadList(listId: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, currentListId = listId)
        viewModelScope.launch {
            try {
                val list = localUserDataRepository.getCustomListItems(listId)
                _uiState.value = _uiState.value.copy(
                    animeIds = list,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        loadOtherLists(listId)
    }

    private fun loadOtherLists(currentListId: Int) {
        viewModelScope.launch {
            try {
                localUserDataRepository.getCustomLists().collect { lists ->
                    _uiState.value = _uiState.value.copy(
                        otherLists = lists.filter { it.id != currentListId }
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun showMoveDialog(animeId: Int) {
        _uiState.value = _uiState.value.copy(showMoveDialog = true, moveAnimeId = animeId)
    }

    fun hideMoveDialog() {
        _uiState.value = _uiState.value.copy(showMoveDialog = false, moveAnimeId = 0)
    }

    fun moveItem(targetListId: Int) {
        val animeId = _uiState.value.moveAnimeId
        val currentListId = _uiState.value.currentListId
        if (animeId == 0) return
        viewModelScope.launch {
            val size = localUserDataRepository.getCustomListItems(targetListId).size
            localUserDataRepository.addToCustomList(targetListId, animeId, size)
            localUserDataRepository.removeFromCustomList(currentListId, animeId)
            _uiState.value = _uiState.value.copy(
                animeIds = _uiState.value.animeIds - animeId,
                showMoveDialog = false,
                moveAnimeId = 0
            )
        }
    }

    fun removeItem(listId: Int, animeId: Int) {
        viewModelScope.launch {
            localUserDataRepository.removeFromCustomList(listId, animeId)
            _uiState.value = _uiState.value.copy(
                animeIds = _uiState.value.animeIds - animeId
            )
        }
    }
}
