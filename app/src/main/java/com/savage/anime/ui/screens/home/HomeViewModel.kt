package com.savage.anime.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val localUserDataRepository: LocalUserDataRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError.asStateFlow()

    private val _genreSections = MutableStateFlow<List<GenreSection>>(emptyList())
    val genreSections: StateFlow<List<GenreSection>> = _genreSections.asStateFlow()

    private var hasLoadedOnce = false

    val uiState: StateFlow<HomeUiState> =
        combine(
            animeRepository.getTrending(),
            animeRepository.getPopular(),
            animeRepository.getOngoing(),
            animeRepository.getUpcoming(),
            animeRepository.getNewest()
        ) { trending, popular, ongoing, upcoming, newest ->
            Combine5(trending, popular, ongoing, upcoming, newest)
        }
            .combine(
                combine(
                    animeRepository.getUpdatedFeed(),
                    animeRepository.getLatestEpisodes(),
                    localUserDataRepository.getWatchlist(),
                    localUserDataRepository.getContinueWatching()
                ) { updated, latestEpisodes, watchlist, continueWatching ->
                    Combine4(updated, latestEpisodes, watchlist, continueWatching)
                }
            ) { (trending, popular, ongoing, upcoming, newest), (updated, latestEpisodes, watchlist, continueWatching) ->

                val combinedHero = (trending + popular)
                    .distinctBy { it.id }
                    .take(20)

                HomeUiState(
                    hero = combinedHero,
                    popular = popular,
                    ongoing = ongoing,
                    upcoming = upcoming,
                    newest = newest,
                    updated = updated,
                    latestEpisodes = latestEpisodes,
                    watchlist = watchlist,
                    continueWatching = continueWatching,
                    isLoading = false,
                    error = null
                )
            }
            .catch {
                emit(HomeUiState(error = "Errore caricamento dati"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState()
            )

    init {
        refresh()
        loadGenreSections()
        watchDataState()
    }

    private fun watchDataState() {
        viewModelScope.launch {
            uiState.collect { state ->
                val isEmpty = state.hero.isEmpty() && state.popular.isEmpty()
                    && state.ongoing.isEmpty() && state.upcoming.isEmpty()
                    && state.newest.isEmpty() && state.updated.isEmpty()

                if (!isEmpty) {
                    hasLoadedOnce = true
                } else if (hasLoadedOnce && !_isLoading.value && _fetchError.value == null) {
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                animeRepository.fetchHome()
                _fetchError.value = null
            } catch (e: Exception) {
                _fetchError.value = "Impossibile aggiornare i dati: ${e.message ?: "errore sconosciuto"}"
            } finally {
                _isLoading.value = false
            }
        }
        loadGenreSections()
    }

    private fun loadGenreSections() {
        viewModelScope.launch {
            try {
                val genres = animeRepository.getGenres()
                val sections = genres.map { genre ->
                    val items = try {
                        animeRepository.getGenreAnime(genre.id).first()
                            .distinctBy { it.id }
                            .take(20)
                    } catch (_: Exception) { emptyList() }
                    GenreSection(genre = genre, items = items)
                }
                _genreSections.value = sections
            } catch (_: Exception) { }
        }
    }

    fun removeFromContinueWatching(item: ContinueWatchingItem) {
        viewModelScope.launch {
            localUserDataRepository.clearAnimePosition(item.animeId)
        }
    }
}

private data class Combine5(
    val trending: List<Anime>,
    val popular: List<Anime>,
    val ongoing: List<Anime>,
    val upcoming: List<Anime>,
    val newest: List<Anime>
)

private data class Combine4(
    val updated: List<Anime>,
    val latestEpisodes: List<Episode>,
    val watchlist: List<Anime>,
    val continueWatching: List<ContinueWatchingItem>
)
