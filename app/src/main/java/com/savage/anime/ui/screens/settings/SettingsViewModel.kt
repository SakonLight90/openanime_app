package com.savage.anime.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val cacheSize: String = "0 MB",
    val autoPlayNext: Boolean = true,
    val creator: String = "SavageGhost",
    val githubUrl: String = "https://github.com/sakonlight90/openanime_app",
    val selectedAccentColor: Long = 0xFFE50914
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun getCacheSize() {
        viewModelScope.launch {
            try {
                val count = animeRepository.getCacheCount()
                _uiState.value = _uiState.value.copy(cacheSize = "$count anime")
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(cacheSize = "0 MB")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            animeRepository.clearCache()
            getCacheSize()
        }
    }

    fun loadAutoPlayNext() {
        viewModelScope.launch {
            val value = localUserDataRepository.getAutoPlayNext()
            _uiState.value = _uiState.value.copy(autoPlayNext = value)
        }
    }

    fun toggleAutoPlayNext() {
        viewModelScope.launch {
            val newValue = !_uiState.value.autoPlayNext
            localUserDataRepository.setAutoPlayNext(newValue)
            _uiState.value = _uiState.value.copy(autoPlayNext = newValue)
        }
    }

    fun setAccentColor(color: Long) {
        _uiState.value = _uiState.value.copy(selectedAccentColor = color)
    }

    fun applyAccentColor() {
        viewModelScope.launch {
            localUserDataRepository.setAccentColor(_uiState.value.selectedAccentColor)
        }
    }

    fun loadAccentColor() {
        viewModelScope.launch {
            val color = localUserDataRepository.getAccentColor()
            if (color != null) {
                _uiState.value = _uiState.value.copy(selectedAccentColor = color)
            }
        }
    }
}
