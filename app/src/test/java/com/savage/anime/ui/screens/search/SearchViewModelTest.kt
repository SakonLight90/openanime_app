package com.savage.anime.ui.screens.search

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.PagedAnime
import com.savage.anime.domain.repository.AnimeRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val animeRepository: AnimeRepository = mockk(relaxed = true)
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { animeRepository.getAzList(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun page(
        items: List<Anime>,
        page: Int = 1,
        total: Int = items.size,
        totalPages: Int = 1
    ) = PagedAnime(items = items, page = page, pageSize = items.size, total = total, totalPages = totalPages)

    @Test
    fun `initial state has empty query and results`() {
        val state = viewModel.uiState.value
        assertTrue(state.query.isEmpty())
        assertTrue(state.results.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `search emits results after debounce`() = runTest(testDispatcher) {
        val results = listOf(
            Anime(1, "Naruto", "img1", "anime", 220),
            Anime(2, "Naruto Shippuden", "img2", "anime", 500)
        )
        coEvery { animeRepository.search("naruto", any(), any(), any(), any()) } returns flowOf(page(results))

        viewModel.search("naruto")
        testDispatcher.scheduler.advanceBy(300)

        val state = viewModel.uiState.value
        assertEquals("naruto", state.query)
        assertEquals(2, state.results.size)
        assertEquals("Naruto", state.results[0].title)
        assertFalse(state.isLoading)
    }

    @Test
    fun `type filter is forwarded to the repository`() = runTest(testDispatcher) {
        coEvery { animeRepository.search(any(), any(), any(), any(), any()) } returns
            flowOf(page(listOf(Anime(2, "Movie", "img", "movie", 1))))

        viewModel.search("test")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.setTypeFilter("movie")
        testDispatcher.scheduler.advanceBy(300)

        assertEquals("movie", viewModel.uiState.value.selectedType)
        coVerify { animeRepository.search("test", "movie", null, null, 1) }
    }

    @Test
    fun `status and year filters are forwarded to the repository`() = runTest(testDispatcher) {
        coEvery { animeRepository.search(any(), any(), any(), any(), any()) } returns
            flowOf(page(listOf(Anime(1, "A", "img", "anime", 1, releaseDate = "2015-10-03"))))

        viewModel.search("a")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.setStatusFilter("ongoing")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.setYearFilter("2015")
        testDispatcher.scheduler.advanceBy(300)

        val state = viewModel.uiState.value
        assertEquals("ongoing", state.selectedStatus)
        assertEquals("2015", state.selectedYear)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun `clearFilters resets all selections`() = runTest(testDispatcher) {
        coEvery { animeRepository.search(any(), any(), any(), any(), any()) } returns flowOf(page(emptyList()))

        viewModel.search("a")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.setTypeFilter("movie")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.setYearFilter("2020")
        testDispatcher.scheduler.advanceBy(300)

        viewModel.clearFilters()
        testDispatcher.scheduler.advanceBy(300)

        val state = viewModel.uiState.value
        assertEquals(null, state.selectedType)
        assertEquals(null, state.selectedYear)
        assertFalse(state.hasActiveFilters)
    }

    @Test
    fun `showMore appends the next page`() = runTest(testDispatcher) {
        val first = (1..30).map { Anime(it, "Anime $it", "img", "anime", it) }
        val second = (31..60).map { Anime(it, "Anime $it", "img", "anime", it) }

        coEvery { animeRepository.search(any(), any(), any(), any(), 1) } returns
            flowOf(page(first, page = 1, total = 60, totalPages = 2))
        coEvery { animeRepository.search(any(), any(), any(), any(), 2) } returns
            flowOf(page(second, page = 2, total = 60, totalPages = 2))

        viewModel.search("anime")
        testDispatcher.scheduler.advanceBy(300)

        assertEquals(30, viewModel.uiState.value.results.size)
        assertTrue(viewModel.uiState.value.hasMore)

        viewModel.showMore()
        testDispatcher.scheduler.advance()

        val state = viewModel.uiState.value
        assertEquals(60, state.results.size)
        assertEquals(2, state.page)
        assertFalse(state.hasMore)
    }

    @Test
    fun `release year is derived from release date`() {
        assertEquals(2015, Anime(1, "A", "", "anime", 1, releaseDate = "2015-10-03").releaseYear)
        assertEquals(null, Anime(1, "A", "", "anime", 1).releaseYear)
    }

    @Test
    fun `clearResults resets state`() = runTest(testDispatcher) {
        coEvery { animeRepository.search(any(), any(), any(), any(), any()) } returns
            flowOf(page(listOf(Anime(1, "Naruto", "img", "anime", 220))))

        viewModel.search("naruto")
        testDispatcher.scheduler.advanceBy(300)
        viewModel.clearResults()

        val state = viewModel.uiState.value
        assertTrue(state.query.isEmpty())
        assertTrue(state.results.isEmpty())
        assertEquals(SearchUiState.PAGE_SIZE, state.displayLimit)
    }
}
