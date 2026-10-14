package com.savage.anime.ui.screens.home

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.repository.LocalUserDataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val animeRepository: AnimeRepository = mockk()
    private val localUserDataRepository: LocalUserDataRepository = mockk()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { animeRepository.getTrending() } returns flowOf(emptyList())
        every { animeRepository.getPopular() } returns flowOf(emptyList())
        every { animeRepository.getOngoing() } returns flowOf(emptyList())
        every { animeRepository.getUpcoming() } returns flowOf(emptyList())
        every { animeRepository.getNewest() } returns flowOf(emptyList())
        every { animeRepository.getUpdatedFeed() } returns flowOf(emptyList())
        every { animeRepository.getLatestEpisodes() } returns flowOf(emptyList())
        every { animeRepository.getUpdatedFeed() } returns flowOf(emptyList())
        every { localUserDataRepository.getWatchlist() } returns flowOf(emptyList())
        every { localUserDataRepository.getContinueWatching() } returns flowOf(emptyList())
        coEvery { animeRepository.fetchHome() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        viewModel = HomeViewModel(animeRepository, localUserDataRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.isLoading.value)
    }

    @Test
    fun `uiState combines all flows after refresh`() = runTest(testDispatcher) {
        val trending = listOf(Anime(1, "Trending", "", "TV", 12))
        val popular = listOf(Anime(2, "Popular", "", "TV", 24))
        val ongoing = listOf(Anime(3, "Ongoing", "", "TV", 0))
        val upcoming = listOf(Anime(4, "Upcoming", "", "TV", 0))
        val newest = listOf(Anime(5, "Newest", "", "TV", 1))
        val updated = listOf(Anime(6, "Updated", "", "TV", 10))
        val latestEpisodes = listOf(
            Episode(1, 1.0, "tok1", anime = null),
            Episode(2, 2.0, "tok2", anime = null)
        )
        val watchlist = listOf(Anime(10, "Watched", "", "Movie", 1))
        val continueWatching = listOf(
            ContinueWatchingItem(1, 1, 5000L, 100000L, 1000L, "CW Anime", "img", 1.0)
        )

        every { animeRepository.getTrending() } returns flowOf(trending)
        every { animeRepository.getPopular() } returns flowOf(popular)
        every { animeRepository.getOngoing() } returns flowOf(ongoing)
        every { animeRepository.getUpcoming() } returns flowOf(upcoming)
        every { animeRepository.getNewest() } returns flowOf(newest)
        every { animeRepository.getUpdatedFeed() } returns flowOf(updated)
        every { animeRepository.getLatestEpisodes() } returns flowOf(latestEpisodes)
        every { localUserDataRepository.getWatchlist() } returns flowOf(watchlist)
        every { localUserDataRepository.getContinueWatching() } returns flowOf(continueWatching)

        viewModel = HomeViewModel(animeRepository, localUserDataRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.hero == trending)
        assert(state.popular == popular)
        assert(state.ongoing == ongoing)
        assert(state.upcoming == upcoming)
        assert(state.newest == newest)
        assert(state.updated == updated)
        assert(state.latestEpisodes == latestEpisodes)
        assert(state.watchlist == watchlist)
        assert(state.continueWatching == continueWatching)
    }

    @Test
    fun `refresh calls fetchHome`() = runTest(testDispatcher) {
        viewModel = HomeViewModel(animeRepository, localUserDataRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { animeRepository.fetchHome() }
    }

    @Test
    fun `removeFromContinueWatching calls clearAnimePosition`() = runTest(testDispatcher) {
        val item = ContinueWatchingItem(42, 1, 5000L, 0L, 1000L, "A", "img", 1.0)
        coEvery { localUserDataRepository.clearAnimePosition(42) } returns Unit

        viewModel = HomeViewModel(animeRepository, localUserDataRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.removeFromContinueWatching(item)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { localUserDataRepository.clearAnimePosition(42) }
    }
}
