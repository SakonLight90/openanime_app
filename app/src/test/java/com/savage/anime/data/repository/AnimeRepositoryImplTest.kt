package com.savage.anime.data.repository

import com.savage.anime.data.local.dao.AnimeDao
import com.savage.anime.data.local.dao.EpisodeDao
import com.savage.anime.data.local.dao.SearchCacheDao
import com.savage.anime.data.local.entity.SearchCacheEntity
import com.savage.anime.data.network.api.AnimeApi
import com.savage.anime.data.network.dto.HomeResponse
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.PagedAnime
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnimeRepositoryImplTest {

    private val api: AnimeApi = mockk()
    private val animeDao: AnimeDao = mockk()
    private val episodeDao: EpisodeDao = mockk()
    private val searchCacheDao: SearchCacheDao = mockk()
    private lateinit var repository: AnimeRepositoryImpl

    @Before
    fun setUp() {
        repository = AnimeRepositoryImpl(api, animeDao, episodeDao, searchCacheDao)
    }

    private fun page(
        items: List<Anime>,
        page: Int = 1,
        total: Int = items.size,
        totalPages: Int = 1
    ) = PagedAnime(items = items, page = page, pageSize = items.size, total = total, totalPages = totalPages)

    @Test
    fun `search emits cached results first then fresh`() = runTest {
        val normalized = "naruto"
        val cached = listOf(SearchCacheEntity(normalized, 1, "Naruto", "img", "anime", 220, 8.5f))
        val fresh = listOf(
            Anime(1, "Naruto", "img", "anime", 220),
            Anime(2, "Naruto Shippuden", "img2", "anime", 500)
        )

        coEvery { searchCacheDao.getByQuery(normalized) } returns cached
        coEvery { api.search("naruto", null, null, null, 1) } returns page(fresh, total = 2)
        coEvery { searchCacheDao.clearByQuery(normalized) } returns Unit
        coEvery { searchCacheDao.insertAll(any()) } returns Unit

        val emissions = repository.search("naruto").toList()

        assertEquals(2, emissions.size)
        assertEquals(1, emissions[0].items.size)
        assertEquals("Naruto", emissions[0].items[0].title)
        assertEquals(2, emissions[1].items.size)
        assertEquals(2, emissions[1].total)
        coVerify { searchCacheDao.clearByQuery(normalized) }
        coVerify { searchCacheDao.insertAll(any()) }
    }

    @Test
    fun `search emits only fresh when cache empty`() = runTest {
        val fresh = listOf(Anime(1, "Naruto", "img", "anime", 220))

        coEvery { searchCacheDao.getByQuery("naruto") } returns emptyList()
        coEvery { api.search("naruto", null, null, null, 1) } returns page(fresh)
        coEvery { searchCacheDao.clearByQuery("naruto") } returns Unit
        coEvery { searchCacheDao.insertAll(any()) } returns Unit

        val emissions = repository.search("naruto").toList()

        assertEquals(1, emissions.size)
        assertEquals(1, emissions[0].items.size)
    }

    @Test
    fun `search with filters skips the cache entirely`() = runTest {
        val fresh = listOf(Anime(1, "Movie", "img", "movie", 1))

        coEvery { api.search("naruto", "movie", "completed", "2015", 1) } returns page(fresh)

        val emissions = repository.search("naruto", "movie", "completed", "2015", 1).toList()

        assertEquals(1, emissions.size)
        coVerify(exactly = 0) { searchCacheDao.getByQuery(any()) }
        coVerify(exactly = 0) { searchCacheDao.insertAll(any()) }
    }

    @Test
    fun `search forwards page number`() = runTest {
        val second = listOf(Anime(31, "Anime 31", "img", "anime", 1))

        coEvery { api.search("a", null, null, null, 2) } returns page(second, page = 2, total = 40, totalPages = 2)

        val result = repository.search("a", page = 2).toList().last()

        assertEquals(2, result.page)
        assertEquals(40, result.total)
        assertEquals(true, result.hasMore)
    }

    @Test
    fun `getAzList unwraps envelope and caches by letter`() = runTest {
        val category = "az:a"
        val items = listOf(Anime(1, "Attack on Titan", "img", "anime", 100))
        val envelope = PagedAnime(items = items, page = 1, pageSize = 60, total = 1, totalPages = 1)

        coEvery { animeDao.getByCategoryList(category) } returns emptyList()
        every { api.getAzList("a", 1) } returns envelope
        coEvery { animeDao.insertAll(any()) } returns Unit

        val emissions = repository.getAzList("a").toList()

        assertEquals(1, emissions.size)
        assertEquals(1, emissions[0].size)
        coVerify { animeDao.insertAll(any()) }
    }

    @Test
    fun `getGenreAnime caches by genre id and paginates`() = runTest {
        val category = "genre:5"
        val results = listOf(Anime(10, "Action Anime", "img", "anime", 24))

        coEvery { animeDao.getByCategoryList(category) } returns emptyList()
        coEvery { api.getGenreAnime(5, 1) } returns page(results)
        coEvery { animeDao.insertAll(any()) } returns Unit

        val emissions = repository.getGenreAnime(5).toList()

        assertEquals(1, emissions.size)
        assertEquals(1, emissions[0].items.size)
        coVerify { animeDao.insertAll(any()) }
    }

    @Test
    fun `fetchHome inserts all categories`() = runBlocking {
        val homeResponse = HomeResponse(
            hero = listOf(Anime(1, "Hero", "img", "anime", 12)),
            popular = listOf(Anime(2, "Popular", "img", "anime", 24)),
            ongoing = emptyList(),
            upcoming = emptyList(),
            newest = emptyList(),
            latestEpisodes = emptyList(),
            continueWatching = emptyList()
        )

        every { api.getHome() } returns homeResponse
        every { api.getUpdated() } returns emptyList()
        coEvery { animeDao.insertAll(any()) } returns Unit
        coEvery { episodeDao.insertAll(any()) } returns Unit

        repository.fetchHome()

        coVerify(atLeast = 2) { animeDao.insertAll(any()) }
        coVerify { episodeDao.insertAll(any()) }
    }
}
