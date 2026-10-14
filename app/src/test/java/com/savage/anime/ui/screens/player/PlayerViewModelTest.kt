package com.savage.anime.ui.screens.player

import android.app.Application
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.AnimeDetail
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.models.StreamResponse
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.repository.LocalUserDataRepository
import com.savage.anime.domain.util.filterItaDubDuplicates
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val animeRepository: AnimeRepository = mockk(relaxed = true)
    private val localUserDataRepository: LocalUserDataRepository = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)

    private lateinit var viewModel: PlayerViewModel

    private val detail = AnimeDetail(
        id = 1,
        title = "Test Anime",
        synopsis = "S",
        genres = emptyList(),
        rating = 8f,
        episodeCount = 3,
        type = "anime",
        status = "ongoing",
        isDub = false,
        language = "sub",
        coverImage = "cover.png",
        bannerImage = "",
        relatedVersions = emptyList(),
        episodes = listOf(
            Episode(id = 11, number = 1.0, token = "t1"),
            Episode(id = 12, number = 2.0, token = "t2"),
            Episode(id = 13, number = 3.0, token = "t3")
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { animeRepository.getDetail(1) } returns flowOf(detail)
        coEvery { animeRepository.getStreamUrl(any()) } returns StreamResponse(success = true, url = "https://stream/x.m3u8")
        coEvery { localUserDataRepository.getPosition(any(), any()) } returns 0L
        coEvery { localUserDataRepository.getAutoPlayNext() } returns true
        coEvery { localUserDataRepository.getDuration(any(), any()) } returns 0L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPlayer resolves stream url for requested episode`() = runTest(testDispatcher) {
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 11)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("https://stream/x.m3u8", state.streamUrl)
        assertNull(state.error)
        assertEquals(11, state.episode?.id)
    }

    @Test
    fun `loadPlayer surfaces error when episode is missing`() = runTest(testDispatcher) {
        coEvery { animeRepository.getEpisodeById(any()) } returns null
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 999)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Episodio non trovato", state.error)
    }

    @Test
    fun `getNextEpisode returns following episode`() = runTest(testDispatcher) {
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 11)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(12, viewModel.getNextEpisode()?.id)
    }

    @Test
    fun `getNextEpisode returns null on last episode`() = runTest(testDispatcher) {
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 13)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.getNextEpisode())
    }

    @Test
    fun `getPreviousEpisode returns preceding episode`() = runTest(testDispatcher) {
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 12)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(11, viewModel.getPreviousEpisode()?.id)
    }

    @Test
    fun `getPreviousEpisode returns null on first episode`() = runTest(testDispatcher) {
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 11)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.getPreviousEpisode())
    }

    @Test
    fun `retryStream gives up after three attempts`() = runTest(testDispatcher) {
        coEvery { animeRepository.getStreamUrl(any()) } returns StreamResponse(success = false, url = null)
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 11)
        testDispatcher.scheduler.advanceUntilIdle()

        repeat(4) {
            viewModel.retryStream()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        assertEquals("Impossibile caricare lo streaming", viewModel.uiState.value.error)
    }

    @Test
    fun `setAutoPlayNextEnabled persists the setting`() = runTest(testDispatcher) {
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.setAutoPlayNextEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { localUserDataRepository.setAutoPlayNext(false) }
    }

    @Test
    fun `releasePlayer clears the player instance`() = runTest(testDispatcher) {
        viewModel = PlayerViewModel(animeRepository, localUserDataRepository, application)
        viewModel.loadPlayer(1, 11)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.releasePlayer()

        assertNull(viewModel.getExoPlayer())
        assert(!viewModel.isPlayerAlive())
    }

    @Test
    fun `anime filters strip ita dub duplicates`() {
        val list = listOf(
            Anime(1, "One Piece (ITA)", "", "anime", 0),
            Anime(2, "One Piece", "", "anime", 0),
            Anime(3, "Bleach", "", "anime", 0)
        )
        val filtered = list.filterItaDubDuplicates { it.title }

        assertEquals(listOf(2, 3), filtered.map { it.id })
    }

    @Test
    fun `anime filters keep ita variant when no original exists`() {
        val list = listOf(Anime(1, "Kimetsu (ITA)", "", "anime", 0))
        val filtered = list.filterItaDubDuplicates { it.title }

        assertEquals(1, filtered.size)
    }
}
