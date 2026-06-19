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

data class CustomListsUiState(
    val lists: List<CustomList> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false
)

@HiltViewModel
class CustomListsViewModel @Inject constructor(
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomListsUiState())
    val uiState: StateFlow<CustomListsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            localUserDataRepository.getCustomLists().collect { lists ->
                _uiState.value = _uiState.value.copy(lists = lists, isLoading = false)
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun createList(name: String) {
        viewModelScope.launch {
            localUserDataRepository.createCustomList(name)
            hideCreateDialog()
        }
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            localUserDataRepository.deleteCustomList(listId)
        }
    }
}
