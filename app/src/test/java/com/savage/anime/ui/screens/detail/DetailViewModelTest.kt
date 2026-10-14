package com.savage.anime.ui.screens.detail

import com.savage.anime.domain.models.AnimeDetail
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
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val animeRepository: AnimeRepository = mockk()
    private val localUserDataRepository: LocalUserDataRepository = mockk()
    private lateinit var viewModel: DetailViewModel

    private val sampleDetail = AnimeDetail(
        id = 1,
        title = "Test Anime",
        synopsis = "A test anime",
        genres = listOf("Action"),
        rating = 8.5f,
        episodeCount = 12,
        type = "TV",
        status = "ongoing",
        isDub = false,
        language = "sub",
        coverImage = "cover.jpg",
        bannerImage = "",
        relatedVersions = emptyList(),
        episodes = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { animeRepository.getDetail(1) } returns flowOf(sampleDetail)
        coEvery { localUserDataRepository.getWatchedEpisodeIds(1) } returns emptyList()
        coEvery { localUserDataRepository.getLastWatchedEpisode(1) } returns null
        coEvery { localUserDataRepository.getPosition(1, any()) } returns null
        coEvery { localUserDataRepository.getDuration(1, any()) } returns null
        every { localUserDataRepository.isInWatchlist(1) } returns flowOf(false)
        viewModel = DetailViewModel(animeRepository, localUserDataRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDetail populates anime`() = runTest(testDispatcher) {
        viewModel.loadDetail(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.anime == sampleDetail)
        assert(!state.isLoading)
        assert(state.error == null)
    }

    @Test
    fun `loadWatchlistStatus updates isInWatchlist`() = runTest(testDispatcher) {
        every { localUserDataRepository.isInWatchlist(1) } returns flowOf(true)

        viewModel.loadWatchlistStatus(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.uiState.value.isInWatchlist)
    }

    @Test
    fun `toggleWatchlist adds and removes`() = runTest(testDispatcher) {
        coEvery { localUserDataRepository.addToWatchlist(1) } returns Unit
        coEvery { localUserDataRepository.removeFromWatchlist(1) } returns Unit

        viewModel.loadDetail(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleWatchlist()
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { localUserDataRepository.addToWatchlist(1) }

        viewModel.toggleWatchlist()
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { localUserDataRepository.removeFromWatchlist(1) }
    }

    @Test
    fun `toggleEpisodeWatched marks and unmarks`() = runTest(testDispatcher) {
        coEvery { localUserDataRepository.markEpisodeAsWatched(1, 100) } returns Unit
        coEvery { localUserDataRepository.markEpisodeAsUnwatched(1, 100) } returns Unit
        coEvery { localUserDataRepository.getWatchedEpisodeIds(1) } returns listOf(100)

        viewModel.loadDetail(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleEpisodeWatched(100)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { localUserDataRepository.markEpisodeAsWatched(1, 100) }
    }

    @Test
    fun `selectLanguage saves preference`() = runTest(testDispatcher) {
        val detailWithVersions = sampleDetail.copy(
            language = "sub",
            relatedVersions = listOf(
                com.savage.anime.domain.models.RelatedVersion(2, "Dub Version", "dub", true)
            )
        )
        every { animeRepository.getDetail(1) } returns flowOf(detailWithVersions)
        coEvery { localUserDataRepository.saveLanguagePreference(1, 2) } returns Unit

        viewModel.loadDetail(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectLanguage(2)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { localUserDataRepository.saveLanguagePreference(1, 2) }
        assert(viewModel.uiState.value.selectedVersionId == 2)
        assert(viewModel.uiState.value.selectedLanguage == "dub")
    }
}
