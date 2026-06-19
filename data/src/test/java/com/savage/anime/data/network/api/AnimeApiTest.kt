package com.savage.anime.data.network.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

class AnimeApiTest {

    private val mockWebServer = MockWebServer()
    private lateinit var api: AnimeApi

    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        mockWebServer.start()
        val contentType = "application/json".toMediaType()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AnimeApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `search returns anime list`() = runBlocking {
        val responseBody = """
            [
                {"id": 1, "title": "Naruto", "image": "img.jpg", "type": "TV", "episode_count": 220, "rating": 8.5},
                {"id": 2, "title": "One Piece", "image": "img2.jpg", "type": "TV", "episode_count": 1000, "rating": 9.0}
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse().setBody(responseBody).setResponseCode(200)
        )

        val results = api.search("naruto")
        assertEquals(2, results.size)
        assertEquals("Naruto", results[0].title)
        assertEquals("One Piece", results[1].title)
    }

    @Test
    fun `getDetail returns anime detail`() = runBlocking {
        val responseBody = """
            {
                "id": 1,
                "title": "Test Anime",
                "synopsis": "A test",
                "genres": ["Action", "Adventure"],
                "rating": 8.5,
                "episode_count": 12,
                "type": "TV",
                "status": "ongoing",
                "is_dub": false,
                "language": "sub",
                "cover_image": "cover.jpg",
                "banner_image": "banner.jpg",
                "related_versions": [],
                "episodes": []
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse().setBody(responseBody).setResponseCode(200)
        )

        val detail = api.getDetail(1)
        assertEquals(1, detail.id)
        assertEquals("Test Anime", detail.title)
        assertEquals(2, detail.genres.size)
    }

    @Test
    fun `getHome returns home response`() = runBlocking {
        val responseBody = """
            {
                "hero": [],
                "popular": [],
                "ongoing": [],
                "upcoming": [],
                "newest": [],
                "latest_episodes": []
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse().setBody(responseBody).setResponseCode(200)
        )

        val home = api.getHome()
        assertNotNull(home)
        assertEquals(0, home.popular.size)
    }

    @Test
    fun `search passes query parameter`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setBody("[]").setResponseCode(200)
        )

        api.search("one piece")
        val request = mockWebServer.takeRequest()
        assertEquals("/search?q=one+piece", request.path)
    }
}
