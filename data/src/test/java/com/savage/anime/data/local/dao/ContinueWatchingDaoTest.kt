package com.savage.anime.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.savage.anime.data.local.AppDatabase
import com.savage.anime.data.local.entity.AnimeCacheEntity
import com.savage.anime.data.local.entity.ContinueWatchingEntity
import com.savage.anime.data.local.entity.EpisodeCacheEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ContinueWatchingDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ContinueWatchingDao
    private lateinit var animeDao: AnimeDao
    private lateinit var episodeDao: EpisodeDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.continueWatchingDao()
        animeDao = db.animeDao()
        episodeDao = db.episodeDao()

        runBlocking {
            animeDao.insert(
                AnimeCacheEntity(
                    id = 1, title = "Test Anime", synopsis = null,
                    image = "img.jpg", coverImage = null, bannerImage = null,
                    type = "TV", episodeCount = 12, rating = 8.5f,
                    releaseDate = null, status = null, isDub = false,
                    language = null, genres = null, category = "test"
                )
            )
            episodeDao.insertAll(
                listOf(
                    EpisodeCacheEntity(
                        id = 100, animeId = 1, number = 1.0,
                        title = "Ep 1", token = "tok1"
                    )
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `save and get position`() = runBlocking {
        dao.save(
            ContinueWatchingEntity(
                animeId = 1, episodeId = 100,
                positionMs = 5000L, durationMs = 120000L
            )
        )

        val result = dao.getPosition(1, 100)
        assertNotNull(result)
        assertEquals(5000L, result?.positionMs)
        assertEquals(120000L, result?.durationMs)
    }

    @Test
    fun `getLastWatchedEpisodeId returns most recent`() = runBlocking {
        dao.save(
            ContinueWatchingEntity(
                animeId = 1, episodeId = 100,
                positionMs = 5000L, lastWatchedAt = 100L
            )
        )
        dao.save(
            ContinueWatchingEntity(
                animeId = 1, episodeId = 101,
                positionMs = 10000L, lastWatchedAt = 200L
            )
        )

        val lastId = dao.getLastWatchedEpisodeId(1)
        assertEquals(101, lastId)
    }

    @Test
    fun `clearPosition removes entry`() = runBlocking {
        dao.save(
            ContinueWatchingEntity(
                animeId = 1, episodeId = 100,
                positionMs = 5000L
            )
        )
        dao.clearPosition(1, 100)

        val result = dao.getPosition(1, 100)
        assertNull(result)
    }

    @Test
    fun `clearAnimePosition removes all entries for anime`() = runBlocking {
        dao.save(
            ContinueWatchingEntity(animeId = 1, episodeId = 100, positionMs = 5000L)
        )
        dao.save(
            ContinueWatchingEntity(animeId = 1, episodeId = 101, positionMs = 10000L)
        )
        dao.clearAnimePosition(1)

        val ids = dao.getWatchedEpisodeIds(1)
        assertEquals(0, ids.size)
    }

    @Test
    fun `getWatchedEpisodeIds returns all episode ids`() = runBlocking {
        dao.save(
            ContinueWatchingEntity(animeId = 1, episodeId = 100, positionMs = 5000L)
        )
        dao.save(
            ContinueWatchingEntity(animeId = 1, episodeId = 101, positionMs = 10000L)
        )

        val ids = dao.getWatchedEpisodeIds(1)
        assertEquals(2, ids.size)
        assertEquals(listOf(100, 101).sorted(), ids.sorted())
    }

    @Test
    fun `getDuration returns duration_ms`() = runBlocking {
        dao.save(
            ContinueWatchingEntity(
                animeId = 1, episodeId = 100,
                positionMs = 5000L, durationMs = 120000L
            )
        )

        val dur = dao.getDuration(1, 100)
        assertEquals(120000L, dur)
    }
}
