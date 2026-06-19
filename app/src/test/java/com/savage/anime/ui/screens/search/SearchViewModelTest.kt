package com.savage.anime.ui.screens.search

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.repository.AnimeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val animeRepository: AnimeRepository = mockk()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(animeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty query and results`() {
        val state = viewModel.uiState.value
        assert(state.query.isEmpty())
        assert(state.results.isEmpty())
        assert(!state.isLoading)
    }

    @Test
    fun `search emits results after debounce`() = runTest(testDispatcher) {
        val results = listOf(
            Anime(1, "Naruto", "img1", "TV", 220),
            Anime(2, "Naruto Shippuden", "img2", "TV", 500)
        )
        every { animeRepository.search("naruto") } returns flowOf(results)

        viewModel.search("naruto")
        testDispatcher.scheduler.advanceBy(300)

        val state = viewModel.uiState.value
        assert(state.query == "naruto")
        assert(state.results.size == 2)
        assert(state.results[0].title == "Naruto")
        assert(!state.isLoading)
    }

    @Test
    fun `showMore increases display limit`() = runTest(testDispatcher) {
        val manyResults = (1..60).map { i ->
            Anime(i, "Anime $i", "img$i", "TV", i)
        }
        every { animeRepository.search("anime") } returns flowOf(manyResults)

        viewModel.search("anime")
        testDispatcher.scheduler.advanceBy(300)

        assert(viewModel.uiState.value.results.size == 30)
        assert(viewModel.uiState.value.displayLimit == 30)

        viewModel.showMore()
        assert(viewModel.uiState.value.displayLimit == 60)
        assert(viewModel.uiState.value.results.size == 60)
    }

    @Test
    fun `type filter filters results`() = runTest(testDispatcher) {
        val results = listOf(
            Anime(1, "TV Anime", "img", "TV", 12),
            Anime(2, "Movie Anime", "img", "Movie", 1),
            Anime(3, "OVA Anime", "img", "OVA", 6)
        )
        every { animeRepository.search("test") } returns flowOf(results)

        viewModel.search("test")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.setTypeFilter("Movie")

        val state = viewModel.uiState.value
        assert(state.results.size == 1)
        assert(state.results[0].title == "Movie Anime")
        assert(state.selectedType == "Movie")
    }

    @Test
    fun `clearResults resets state`() = runTest(testDispatcher) {
        every { animeRepository.search("naruto") } returns flowOf(
            listOf(Anime(1, "Naruto", "img", "TV", 220))
        )

        viewModel.search("naruto")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.clearResults()

        val state = viewModel.uiState.value
        assert(state.query.isEmpty())
        assert(state.results.isEmpty())
        assert(state.displayLimit == 30)
    }
}
