package com.savage.anime.ui.screens.player

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val localUserDataRepository: LocalUserDataRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _sleepTimer = MutableStateFlow(SleepTimerState())
    val sleepTimer: StateFlow<SleepTimerState> = _sleepTimer.asStateFlow()

    private var player: ExoPlayer? = null

    private var retryCount = 0
    private var currentAnimeId = 0
    private var currentEpisodeId = 0
    private var currentToken = ""

    private var saveJob: Job? = null
    private var autoPlayNext: Boolean = true
    private var userPaused: Boolean = false
    private var sleepTimerJob: Job? = null
    private var currentAnimeTitle: String = ""
    private var currentAnimeImage: String = ""

    fun getExoPlayer(): ExoPlayer? = player

    fun loadAutoPlaySetting() {
        viewModelScope.launch {
            autoPlayNext = localUserDataRepository.getAutoPlayNext()
        }
    }

    fun isAutoPlayEnabled(): Boolean = autoPlayNext

    fun releasePlayer() {
        saveJob?.cancel()
        sleepTimerJob?.cancel()
        player?.release()
        player = null
    }

    fun skipForward(seconds: Int = 10) {
        player?.let { it.seekTo(it.currentPosition + seconds * 1000L) }
    }

    fun skipBackward(seconds: Int = 10) {
        player?.let { it.seekTo(maxOf(0L, it.currentPosition - seconds * 1000L)) }
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun togglePlayPause() {
        val exo = player ?: return
        if (exo.isPlaying) {
            exo.pause()
            userPaused = true
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            exo.play()
            userPaused = false
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    fun getNextEpisode(): com.savage.anime.domain.models.Episode? {
        val episodes = _uiState.value.anime?.episodes ?: return null
        val currentIndex = episodes.indexOfFirst { it.id == currentEpisodeId }
        if (currentIndex < 0 || currentIndex >= episodes.size - 1) return null
        return episodes[currentIndex + 1]
    }

    fun getPreviousEpisode(): com.savage.anime.domain.models.Episode? {
        val episodes = _uiState.value.anime?.episodes ?: return null
        val currentIndex = episodes.indexOfFirst { it.id == currentEpisodeId }
        if (currentIndex <= 0) return null
        return episodes[currentIndex - 1]
    }

    fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun loadPlayer(animeId: Int, episodeId: Int) {
        currentAnimeId = animeId
        currentEpisodeId = episodeId
        retryCount = 0

        viewModelScope.launch {
            try {
                val detail = animeRepository.getDetail(animeId).first { d ->
                    d.episodes.any { it.id == episodeId }
                }
                val episode = detail.episodes.first { it.id == episodeId }

                currentToken = episode.token
                currentAnimeTitle = detail.title
                currentAnimeImage = detail.coverImage
                val position = localUserDataRepository.getPosition(animeId, episodeId) ?: 0L

                _uiState.value = _uiState.value.copy(
                    anime = detail,
                    episode = episode,
                    position = position,
                    isLoading = true,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Episodio non trovato",
                    isLoading = false
                )
            }
        }
    }

    fun loadStream() {
        if (currentToken.isNotEmpty()) {
            loadStream(currentToken)
        } else {
            refreshTokenAndRetry()
        }
    }

    private fun loadStream(token: String) {
        viewModelScope.launch {
            try {
                val response = animeRepository.getStreamUrl(token)

                val url = response.url

                if (response.success && !url.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        streamUrl = url,
                        isLoading = false,
                        error = null
                    )

                    initializePlayer(url)
                    retryCount = 0
                } else {
                    handleStreamError()
                }

            } catch (e: Exception) {
                handleStreamError()
            }
        }
    }

    fun retryStream() {
        if (retryCount >= 3) {
            _uiState.value = _uiState.value.copy(
                error = "Impossibile caricare lo streaming",
                isLoading = false
            )
            return
        }

        retryCount++
        _uiState.value = _uiState.value.copy(
            error = null,
            isLoading = true
        )

        player?.release()
        player = null

        if (currentToken.isNotEmpty()) {
            loadStream(currentToken)
        } else {
            refreshTokenAndRetry()
        }
    }

    private fun refreshTokenAndRetry() {
        viewModelScope.launch {
            try {
                val detail = animeRepository.getDetail(currentAnimeId).first { d ->
                    d.episodes.any { it.id == currentEpisodeId }
                }
                val episode = detail.episodes.first { it.id == currentEpisodeId }
                currentToken = episode.token
                if (currentToken.isNotEmpty()) {
                    loadStream(currentToken)
                } else {
                    retryStream()
                }
            } catch (_: Exception) {
                retryStream()
            }
        }
    }

    private fun handleStreamError() {
        retryStream()
    }

    private fun initializePlayer(url: String) {
        if (player == null) {
            player = ExoPlayer.Builder(application).build()
        }

        val exo = player ?: return
        exo.clearMediaItems()
        exo.setMediaItem(MediaItem.fromUri(url))
        exo.prepare()

        val position = _uiState.value.position
        if (position > 0) exo.seekTo(position)

        val shouldPlay = !userPaused
        exo.playWhenReady = shouldPlay
        _uiState.value = _uiState.value.copy(isPlaying = shouldPlay)

        exo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    startProgressSaving(exo)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                handleStreamError()
            }
        })
    }

    private fun startProgressSaving(player: ExoPlayer) {
        saveJob?.cancel()

        saveJob = viewModelScope.launch {
            while (true) {
                delay(1000)

                val pos = player.currentPosition
                if (pos <= 0) continue

                val duration = player.duration

                localUserDataRepository.savePosition(
                    currentAnimeId,
                    currentEpisodeId,
                    pos,
                    duration
                )

                val nearEnd = duration > 0 && (duration - pos) <= 15_000L
                _uiState.value = _uiState.value.copy(
                    isNearEnd = nearEnd,
                    playerPosition = pos,
                    playerDuration = duration
                )
            }
        }
    }

    fun onEnterBackground() {
        val exo = player ?: return
        if (exo.isPlaying) {
            userPaused = true
            exo.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        }
        saveJob?.cancel()
        viewModelScope.launch {
            val pos = exo.currentPosition
            val dur = exo.duration
            if (pos > 0) {
                localUserDataRepository.savePosition(currentAnimeId, currentEpisodeId, pos, dur)
                addToWatchHistory()
            }
        }
    }

    fun reloadPlayerForPip() {
        val lastPos = _uiState.value.position
        saveJob?.cancel()
        sleepTimerJob?.cancel()
        player?.stop()
        player?.release()
        player = null
        _uiState.value = _uiState.value.copy(
            streamUrl = null,
            error = null,
            isLoading = true,
            isNearEnd = false,
            position = lastPos
        )
        retryCount = 0
        if (currentAnimeId > 0 && currentEpisodeId > 0) {
            refreshTokenAndRetry()
        }
    }

    fun savePositionNow() {
        val pos = player?.currentPosition ?: return
        if (pos > 0) {
            val dur = player?.duration ?: 0L
            viewModelScope.launch {
                localUserDataRepository.savePosition(currentAnimeId, currentEpisodeId, pos, dur)
                addToWatchHistory()
            }
        }
    }

    fun isPlayerAlive(): Boolean {
        val p = player ?: return false
        return p.playbackState != Player.STATE_IDLE && p.playbackState != Player.STATE_ENDED
    }

    fun setResizeMode(mode: Int) {
        _uiState.value = _uiState.value.copy(resizeMode = mode)
    }

    fun savePosition() {
        saveJob?.cancel()
        saveJob = null

        val pos = player?.currentPosition ?: 0L

        if (pos > 0) {
            viewModelScope.launch {
                localUserDataRepository.savePosition(
                    currentAnimeId,
                    currentEpisodeId,
                    pos
                )
            }
        }

        releasePlayer()
    }

    fun savePositionOnly() {
        val pos = player?.currentPosition ?: 0L
        if (pos > 0) {
            viewModelScope.launch {
                localUserDataRepository.savePosition(
                    currentAnimeId,
                    currentEpisodeId,
                    pos,
                    player?.duration ?: 0L
                )
                addToWatchHistory()
            }
        }
    }

    private suspend fun addToWatchHistory() {
        try {
            localUserDataRepository.addToWatchHistory(
                currentAnimeId,
                currentEpisodeId,
                currentAnimeTitle,
                currentAnimeImage,
                _uiState.value.episode?.number ?: 0.0
            )
        } catch (_: Exception) { }
    }

    fun isUserPaused(): Boolean = userPaused

    fun hasLoaded(): Boolean = currentAnimeId > 0

    fun resumePlayback() {
        val exo = player
        if (exo == null) {
            loadPlayer(currentAnimeId, currentEpisodeId)
            return
        }
        if (exo.playerError != null || exo.playbackState == Player.STATE_IDLE) {
            retryCount = 0
            loadPlayer(currentAnimeId, currentEpisodeId)
        } else {
            if (!userPaused && !exo.isPlaying) {
                exo.play()
            }
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSec = minutes * 60
        _sleepTimer.value = SleepTimerState(isActive = true, remainingSeconds = totalSec, totalSeconds = totalSec)
        sleepTimerJob = viewModelScope.launch {
            for (i in totalSec downTo 1) {
                delay(1000)
                _sleepTimer.value = _sleepTimer.value.copy(remainingSeconds = i - 1)
            }
            _sleepTimer.value = SleepTimerState()
            player?.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimer.value = SleepTimerState()
    }

    fun setAutoPlayNextEnabled(enabled: Boolean) {
        autoPlayNext = enabled
        viewModelScope.launch {
            localUserDataRepository.setAutoPlayNext(enabled)
        }
    }

    override fun onCleared() {
        savePosition()
        super.onCleared()
    }
}

data class PlayerUiState(
    val episode: com.savage.anime.domain.models.Episode? = null,
    val anime: com.savage.anime.domain.models.AnimeDetail? = null,
    val position: Long = 0L,
    val streamUrl: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val retryCount: Int = 0,
    val isNearEnd: Boolean = false,
    val isPlaying: Boolean = false,
    val playerPosition: Long = 0L,
    val playerDuration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val resizeMode: Int = 0
)
