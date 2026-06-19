package com.savage.anime.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.AnimeDetail
import com.savage.anime.domain.models.CustomList
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = false,
    val anime: AnimeDetail? = null,
    val error: String? = null,
    val isInWatchlist: Boolean = false,
    val selectedLanguage: String = "sub",
    val selectedVersionId: Int? = null,
    val currentAnimeId: Int = 0,
    val currentDisplayId: Int = 0,
    val watchedEpisodeIds: Set<Int> = emptySet(),
    val lastWatchedEpisode: Episode? = null,
    val episodeProgress: Map<Int, Float> = emptyMap(),
    val customLists: List<CustomList> = emptyList(),
    val showListPicker: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    fun loadDetail(id: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, currentDisplayId = id)
        if (_uiState.value.currentAnimeId == 0) {
            _uiState.value = _uiState.value.copy(currentAnimeId = id)
        }
        viewModelScope.launch {
            try {
                animeRepository.getDetail(id).collect { detail ->
                    val lang = detail.language.ifEmpty { "sub" }
                    val watchedIds = try {
                        localUserDataRepository.getWatchedEpisodeIds(id).toSet()
                    } catch (_: Exception) { emptySet() }
                    val lastWatched = try {
                        localUserDataRepository.getLastWatchedEpisode(id)
                    } catch (_: Exception) { null }
                    val progress = try {
                        watchedIds.associateWith { eid ->
                            val pos = localUserDataRepository.getPosition(id, eid) ?: 0L
                            val dur = localUserDataRepository.getDuration(id, eid) ?: 0L
                            if (dur > 0 && pos < dur) (pos.toFloat() / dur).coerceIn(0f, 1f) else if (pos > 0) 1f else 0f
                        }
                    } catch (_: Exception) { emptyMap() }

                    val currentSelectedId = _uiState.value.selectedVersionId
                    val resolvedVersionId = if (currentSelectedId != null) {
                        currentSelectedId
                    } else {
                        val savedPref = try {
                            localUserDataRepository.getLanguagePreference(_uiState.value.currentAnimeId)
                        } catch (_: Exception) { null }
                        if (savedPref != null && savedPref > 0) savedPref else null
                    }

                    if (resolvedVersionId != null && resolvedVersionId != id && currentSelectedId == null) {
                        _uiState.value = _uiState.value.copy(
                            selectedVersionId = resolvedVersionId,
                            selectedLanguage = lang
                        )
                        loadDetail(resolvedVersionId)
                        return@collect
                    }

                    _uiState.value = _uiState.value.copy(
                        anime = detail,
                        selectedLanguage = lang,
                        selectedVersionId = resolvedVersionId,
                        watchedEpisodeIds = watchedIds,
                        lastWatchedEpisode = lastWatched,
                        episodeProgress = progress,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Errore caricamento dettaglio"
                )
            }
        }
    }

    fun loadWatchlistStatus(id: Int) {
        viewModelScope.launch {
            localUserDataRepository.isInWatchlist(id).collectLatest { inList ->
                _uiState.value = _uiState.value.copy(isInWatchlist = inList)
            }
        }
    }

    fun loadCustomLists() {
        viewModelScope.launch {
            localUserDataRepository.getCustomLists().collect { lists ->
                _uiState.value = _uiState.value.copy(customLists = lists)
            }
        }
    }

    fun addToCustomList() {
        val animeId = _uiState.value.currentAnimeId
        if (animeId == 0) return
        val lists = _uiState.value.customLists
        viewModelScope.launch {
            if (lists.isEmpty()) {
                val newId = localUserDataRepository.createCustomList("Preferiti")
                localUserDataRepository.addToCustomList(newId, animeId, 0)
                loadCustomLists()
            } else {
                _uiState.value = _uiState.value.copy(showListPicker = true)
            }
        }
    }

    fun showCustomListPicker() {
        _uiState.value = _uiState.value.copy(showListPicker = true)
    }

    fun hideCustomListPicker() {
        _uiState.value = _uiState.value.copy(showListPicker = false)
    }

    fun addToCustomList(listId: Int) {
        val animeId = _uiState.value.currentAnimeId
        if (animeId == 0) return
        viewModelScope.launch {
            val size = localUserDataRepository.getCustomListItems(listId).size
            localUserDataRepository.addToCustomList(listId, animeId, size)
            _uiState.value = _uiState.value.copy(showListPicker = false)
        }
    }

    fun toggleWatchlist() {
        val animeId = _uiState.value.currentAnimeId
        if (animeId == 0) return
        viewModelScope.launch {
            val inList = _uiState.value.isInWatchlist
            if (inList) {
                localUserDataRepository.removeFromWatchlist(animeId)
            } else {
                localUserDataRepository.addToWatchlist(animeId)
            }
            _uiState.value = _uiState.value.copy(isInWatchlist = !inList)
        }
    }

    fun toggleEpisodeWatched(episodeId: Int) {
        val id = _uiState.value.currentAnimeId
        if (id == 0) return
        viewModelScope.launch {
            val isWatched = episodeId in _uiState.value.watchedEpisodeIds
            if (isWatched) {
                localUserDataRepository.markEpisodeAsUnwatched(id, episodeId)
            } else {
                localUserDataRepository.markEpisodeAsWatched(id, episodeId)
            }
            val watchedIds = try {
                localUserDataRepository.getWatchedEpisodeIds(id).toSet()
            } catch (_: Exception) { emptySet() }
            _uiState.value = _uiState.value.copy(watchedEpisodeIds = watchedIds)
        }
    }

    fun selectLanguage(versionId: Int?) {
        val anime = _uiState.value.anime ?: return
        if (versionId == null) {
            val needsReload = _uiState.value.currentDisplayId != _uiState.value.currentAnimeId
            _uiState.value = _uiState.value.copy(
                selectedVersionId = null,
                selectedLanguage = if (anime.isDub) "dub" else "sub",
                currentDisplayId = _uiState.value.currentAnimeId
            )
            viewModelScope.launch {
                localUserDataRepository.saveLanguagePreference(anime.id, -1)
            }
            if (needsReload) {
                loadDetail(_uiState.value.currentAnimeId)
            }
        } else {
            val version = anime.relatedVersions.find { it.id == versionId } ?: return
            _uiState.value = _uiState.value.copy(
                selectedVersionId = versionId,
                selectedLanguage = if (version.isDub) "dub" else "sub",
                currentDisplayId = versionId
            )
            viewModelScope.launch {
                localUserDataRepository.saveLanguagePreference(anime.id, versionId)
            }
            loadDetail(versionId)
        }
    }
}
