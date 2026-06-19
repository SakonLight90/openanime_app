package com.savage.anime.data.repository

import com.savage.anime.data.local.dao.AnimeDao
import com.savage.anime.data.local.dao.EpisodeDao
import com.savage.anime.data.local.dao.SearchCacheDao
import com.savage.anime.data.local.entity.AnimeCacheEntity
import com.savage.anime.data.local.entity.SearchCacheEntity
import com.savage.anime.data.network.api.AnimeApi
import com.savage.anime.domain.models.Anime
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
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

    @Test
    fun `search emits cached results first then fresh`() = runTest {
        val query = "naruto"
        val normalized = "naruto"
        val cachedEntities = listOf(
            SearchCacheEntity(normalized, 1, "Naruto", "img", "TV", 220, 8.5f)
        )
        val freshResults = listOf(
            Anime(1, "Naruto", "img", "TV", 220),
            Anime(2, "Naruto Shippuden", "img2", "TV", 500)
        )

        coEvery { searchCacheDao.getByQuery(normalized) } returns cachedEntities
        every { api.search(query) } returns freshResults
        coEvery { searchCacheDao.clearByQuery(normalized) } returns Unit
        coEvery { searchCacheDao.insertAll(any()) } returns Unit

        val emissions = repository.search(query).toList()

        assertEquals(2, emissions.size)
        assertEquals(1, emissions[0].size)
        assertEquals("Naruto", emissions[0][0].title)
        assertEquals(2, emissions[1].size)
        coVerify { searchCacheDao.clearByQuery(normalized) }
        coVerify { searchCacheDao.insertAll(any()) }
    }

    @Test
    fun `search emits only fresh when cache empty`() = runTest {
        val query = "naruto"
        val normalized = "naruto"
        val freshResults = listOf(
            Anime(1, "Naruto", "img", "TV", 220)
        )

        coEvery { searchCacheDao.getByQuery(normalized) } returns emptyList()
        every { api.search(query) } returns freshResults
        coEvery { searchCacheDao.clearByQuery(normalized) } returns Unit
        coEvery { searchCacheDao.insertAll(any()) } returns Unit

        val emissions = repository.search(query).toList()

        assertEquals(1, emissions.size)
        assertEquals(1, emissions[0].size)
    }

    @Test
    fun `getAzList caches by letter`() = runTest {
        val letter = "a"
        val category = "az:a"
        val results = listOf(Anime(1, "Attack on Titan", "img", "TV", 100))

        coEvery { animeDao.getByCategoryList(category) } returns emptyList()
        every { api.getAzList(letter) } returns results
        coEvery { animeDao.insertAll(any()) } returns Unit

        val emissions = repository.getAzList(letter).toList()

        assertEquals(1, emissions.size)
        assertEquals(1, emissions[0].size)
        coVerify { animeDao.insertAll(any()) }
    }

    @Test
    fun `getGenreAnime caches by genre id`() = runTest {
        val genreId = 5
        val category = "genre:5"
        val results = listOf(Anime(10, "Action Anime", "img", "TV", 24))

        coEvery { animeDao.getByCategoryList(category) } returns emptyList()
        every { api.getGenreAnime(genreId) } returns results
        coEvery { animeDao.insertAll(any()) } returns Unit

        val emissions = repository.getGenreAnime(genreId).toList()

        assertEquals(1, emissions.size)
        assertEquals(1, emissions[0].size)
        coVerify { animeDao.insertAll(any()) }
    }

    @Test
    fun `fetchHome inserts all categories`() = runBlocking {
        val homeResponse = com.savage.anime.data.network.dto.HomeResponse(
            hero = listOf(Anime(1, "Hero", "img", "TV", 12)),
            popular = listOf(Anime(2, "Popular", "img", "TV", 24)),
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
