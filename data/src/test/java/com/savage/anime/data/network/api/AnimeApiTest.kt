package com.savage.anime.data.network.api

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AnimeApiTest {

    private val mockWebServer = MockWebServer()
    private lateinit var api: AnimeApi

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    @Before
    fun setUp() {
        mockWebServer.start()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AnimeApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enqueue(body: String) {
        mockWebServer.enqueue(MockResponse().setBody(body).setResponseCode(200))
    }

    @Test
    fun `search returns paginated anime list`() = runBlocking {
        enqueue(
            """
            {
                "items": [
                    {"id": 1, "title": "Naruto", "image": "img.jpg", "type": "anime", "episode_count": 220, "rating": 8.5, "release_date": "2002-10-03", "status": "completed"},
                    {"id": 2, "title": "One Piece", "image": "img2.jpg", "type": "anime", "episode_count": 1000, "rating": 9.0, "release_date": null, "status": "ongoing"}
                ],
                "page": 1, "page_size": 60, "total": 2, "total_pages": 1
            }
            """.trimIndent()
        )

        val response = api.search("naruto")

        assertEquals(2, response.items.size)
        assertEquals("Naruto", response.items[0].title)
        assertEquals("2002-10-03", response.items[0].releaseDate)
        assertEquals(2002, response.items[0].releaseYear)
        assertNull(response.items[1].releaseDate)
        assertEquals(1, response.totalPages)
        assertEquals(false, response.hasMore)
    }

    @Test
    fun `search sends filters and page`() = runBlocking {
        enqueue("""{"items":[],"page":3,"page_size":60,"total":0,"total_pages":0}""")

        api.search("naruto", "movie", "completed", "2015", 3)

        val path = mockWebServer.takeRequest().path.orEmpty()
        assertEquals(true, path.contains("q=naruto"))
        assertEquals(true, path.contains("type=movie"))
        assertEquals(true, path.contains("status=completed"))
        assertEquals(true, path.contains("year=2015"))
        assertEquals(true, path.contains("page=3"))
    }

    @Test
    fun `getDetail returns anime detail`() = runBlocking {
        enqueue(
            """
            {
                "id": 1,
                "title": "Test Anime",
                "synopsis": "A test",
                "genres": ["Action", "Adventure"],
                "rating": 8.5,
                "episode_count": 12,
                "type": "anime",
                "status": "ongoing",
                "is_dub": false,
                "language": "jp",
                "cover_image": "cover.jpg",
                "banner_image": "banner.jpg",
                "release_date": "2026-10-04",
                "related_versions": [],
                "episodes": [
                    {"id": 11, "number": 1.0, "title": "Ep 1", "token": "tok1", "language": "jp"}
                ]
            }
            """.trimIndent()
        )

        val detail = api.getDetail(1)

        assertEquals(1, detail.id)
        assertEquals("Test Anime", detail.title)
        assertEquals(2, detail.genres.size)
        assertEquals("2026-10-04", detail.releaseDate)
        assertEquals(1, detail.episodes.size)
        assertEquals("tok1", detail.episodes[0].token)
    }

    @Test
    fun `detail tolerates missing optional fields`() = runBlocking {
        enqueue("""{"id": 7, "title": "Solo id e titolo"}""")

        val detail = api.getDetail(7)

        assertEquals(7, detail.id)
        assertEquals("", detail.synopsis)
        assertEquals(0, detail.episodeCount)
        assertEquals("", detail.coverImage)
        assertNull(detail.releaseDate)
    }

    @Test
    fun `getHome returns home response`() = runBlocking {
        enqueue(
            """
            {
                "hero": [],
                "popular": [],
                "ongoing": [],
                "upcoming": [],
                "newest": [],
                "latest_episodes": []
            }
            """.trimIndent()
        )

        val home = api.getHome()

        assertNotNull(home)
        assertEquals(0, home.popular.size)
    }

    @Test
    fun `genre anime is paginated`() = runBlocking {
        enqueue("""{"items":[],"page":1,"page_size":60,"total":0,"total_pages":0}""")

        val response = api.getGenreAnime(3, 1)

        assertEquals(0, response.items.size)
        assertEquals("/api/v2/genres/3/anime?page=1", mockWebServer.takeRequest().path)
    }
}
