package com.savage.anime.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.savage.anime.data.local.entity.AnimeCacheEntity
import com.savage.anime.data.local.entity.CustomListItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class MigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        context.deleteDatabase(TEST_DB)
        createLegacyDatabaseV8()
    }

    @After
    fun tearDown() {
        context.deleteDatabase(TEST_DB)
    }

    private fun createLegacyDatabaseV8() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            object : SupportSQLiteOpenHelper.Callback(LEGACY_VERSION) {
                override fun onCreate(db: SupportSQLiteDatabase) = seedLegacySchema(db)

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) = Unit
            }
        )
        helper.writableDatabase
        helper.close()
    }

    private fun seedLegacySchema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS continue_watching (
                anime_id INTEGER NOT NULL,
                episode_id INTEGER NOT NULL DEFAULT 0,
                position_ms INTEGER NOT NULL DEFAULT 0,
                duration_ms INTEGER NOT NULL DEFAULT 0,
                last_watched_at INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(anime_id, episode_id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS anime_cache (
                id INTEGER NOT NULL,
                title TEXT NOT NULL,
                synopsis TEXT,
                image TEXT NOT NULL,
                cover_image TEXT,
                banner_image TEXT,
                type TEXT NOT NULL,
                episode_count INTEGER NOT NULL DEFAULT 0,
                rating REAL NOT NULL DEFAULT 0,
                release_date TEXT,
                status TEXT,
                is_dub INTEGER NOT NULL DEFAULT 0,
                language TEXT,
                genres TEXT,
                category TEXT NOT NULL DEFAULT '',
                updated_at INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(id, category)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS search_cache (
                query TEXT NOT NULL,
                anime_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                image TEXT NOT NULL,
                type TEXT NOT NULL,
                episode_count INTEGER NOT NULL DEFAULT 0,
                rating REAL NOT NULL DEFAULT 0,
                release_date TEXT,
                status TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(query, anime_id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS watch_history (
                anime_id INTEGER NOT NULL,
                episode_id INTEGER NOT NULL,
                anime_title TEXT NOT NULL,
                anime_image TEXT NOT NULL,
                episode_number REAL NOT NULL DEFAULT 0,
                watched_at INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(anime_id, episode_id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS custom_lists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                created_at INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS custom_list_items (
                list_id INTEGER NOT NULL,
                anime_id INTEGER NOT NULL,
                position INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(list_id, anime_id)
            )
            """.trimIndent()
        )
        db.execSQL(
            "INSERT INTO continue_watching (anime_id, episode_id, position_ms, duration_ms, last_watched_at) VALUES (42, 7, 123456, 1440000, 1700000000000)"
        )
        db.execSQL(
            "INSERT INTO anime_cache (id, title, synopsis, image, type, category, updated_at) VALUES (1, 'Test', 'S', 'i.png', 'anime', 'popular', 1700000000000)"
        )
        db.execSQL(
            "INSERT INTO search_cache (query, anime_id, title, image, type) VALUES ('naruto', 1, 'Test', 'i.png', 'anime')"
        )
        db.execSQL(
            "INSERT INTO watch_history (anime_id, episode_id, anime_title, anime_image, episode_number, watched_at) VALUES (1, 7, 'Test', 'i.png', 3.0, 1700000000000)"
        )
        db.execSQL("INSERT INTO custom_lists (id, name, created_at) VALUES (1, 'Preferiti', 1700000000000)")
        db.execSQL("INSERT INTO custom_list_items (list_id, anime_id, position) VALUES (1, 1, 0)")
    }

    private fun openMigrated(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `migration 8 to 9 preserves continue watching`() = runBlocking {
        val db = openMigrated()
        val item = db.continueWatchingDao().getPosition(42, 7)
        db.close()

        assertNotNull(item)
        assertEquals(123456L, item?.positionMs)
        assertEquals(1440000L, item?.durationMs)
        assertEquals(1700000000000L, item?.lastWatchedAt)
        assertEquals("", item?.animeTitle)
    }

    @Test
    fun `migration 8 to 9 preserves anime cache`() = runBlocking {
        val db = openMigrated()
        val anime = db.animeDao().getById(1)
        db.close()

        assertEquals("Test", anime?.title)
        assertEquals("popular", anime?.category)
    }

    @Test
    fun `migration 8 to 9 preserves search cache`() = runBlocking {
        val db = openMigrated()
        val results = db.searchCacheDao().getByQuery("naruto")
        db.close()

        assertEquals(1, results.size)
        assertEquals("Test", results[0].title)
    }

    @Test
    fun `migration 8 to 9 preserves watch history`() = runBlocking {
        val db = openMigrated()
        val history = db.watchHistoryDao().getAll().first()
        db.close()

        assertEquals(1, history.size)
        assertEquals("Test", history[0].animeTitle)
    }

    @Test
    fun `migration 8 to 9 preserves custom lists and items`() = runBlocking {
        val db = openMigrated()
        val list = db.customListDao().getListById(1)
        val items = db.customListDao().getItems(1)
        db.close()

        assertEquals("Preferiti", list?.name)
        assertEquals(1, items.size)
        assertEquals(1, items[0].animeId)
    }

    @Test
    fun `getById prefers the row carrying the detail synopsis`() = runBlocking {
        val db = openMigrated()

        val listRow = AnimeCacheEntity(
            id = 99,
            title = "Solo lista",
            synopsis = null,
            image = "a.png",
            coverImage = null,
            bannerImage = null,
            type = "anime",
            episodeCount = 12,
            rating = 8f,
            releaseDate = "2024",
            status = "ongoing",
            isDub = false,
            language = null,
            genres = null,
            category = "popular"
        )
        val detailRow = listRow.copy(
            synopsis = "Una trama molto lunga che descrive nel dettaglio",
            coverImage = "cover.png",
            category = ""
        )

        db.animeDao().insertAll(listOf(listRow, detailRow))
        val result = db.animeDao().getById(99)
        db.close()

        assertEquals("Una trama molto lunga che descrive nel dettaglio", result?.synopsis)
        assertEquals("cover.png", result?.coverImage)
    }

    @Test
    fun `getByCustomList returns items ordered by position`() = runBlocking {
        val db = openMigrated()

        val second = AnimeCacheEntity(
            id = 2, title = "Secondo", synopsis = null, image = "b.png",
            coverImage = null, bannerImage = null, type = "anime", episodeCount = 1,
            rating = 5f, releaseDate = null, status = null, isDub = false,
            language = null, genres = null, category = "popular"
        )
        val first = second.copy(id = 3, title = "Terzo", image = "c.png")
        db.animeDao().insertAll(listOf(second, first))
        db.customListDao().addItem(CustomListItemEntity(listId = 1, animeId = 3, position = 5))
        db.customListDao().addItem(CustomListItemEntity(listId = 1, animeId = 2, position = 1))

        val items = db.animeDao().getByCustomList(1)
        db.close()

        assertEquals(listOf(2, 3), items.map { it.id })
    }

    @Test
    fun `migration 9 to 10 adds sort order default and preserves rows`() = runBlocking {
        val db = openMigrated()

        val anime = db.animeDao().getById(1)
        val rows = db.animeDao().getByCategoryList("popular")
        db.close()

        assertNotNull(anime)
        assertEquals("Test", anime?.title)
        assertEquals(0, anime?.sortOrder)
        assertEquals(1, rows.size)
    }

    @Test
    fun `getByCategoryList returns rows in sort order`() = runBlocking {
        val db = openMigrated()

        val base = AnimeCacheEntity(
            id = 10, title = "Dieci", synopsis = null, image = "d.png",
            coverImage = null, bannerImage = null, type = "anime", episodeCount = 1,
            rating = 5f, releaseDate = null, status = null, isDub = false,
            language = null, genres = null, category = "trending", sortOrder = 5
        )
        val first = base.copy(id = 11, title = "Primo", image = "e.png", sortOrder = 1)
        val second = base.copy(id = 12, title = "Secondo", image = "f.png", sortOrder = 3)
        db.animeDao().insertAll(listOf(base, first, second))

        val rows = db.animeDao().getByCategoryList("trending")
        db.close()

        assertEquals(listOf(11, 12, 10), rows.map { it.id })
    }

    @Test
    fun `clearUnreferenced keeps anime used by watchlist and custom lists`() = runBlocking {
        val db = openMigrated()

        db.animeDao().insertAll(
            listOf(
                AnimeCacheEntity(
                    id = 1, title = "In watchlist", synopsis = null, image = "a.png",
                    coverImage = null, bannerImage = null, type = "anime", episodeCount = 1,
                    rating = 5f, releaseDate = null, status = null, isDub = false,
                    language = null, genres = null, category = "popular"
                ),
                AnimeCacheEntity(
                    id = 77, title = "Orfano", synopsis = null, image = "b.png",
                    coverImage = null, bannerImage = null, type = "anime", episodeCount = 1,
                    rating = 5f, releaseDate = null, status = null, isDub = false,
                    language = null, genres = null, category = "popular"
                )
            )
        )

        db.animeDao().clearUnreferenced()

        val kept = db.animeDao().getById(1)
        val orphan = db.animeDao().getById(77)
        val watchlist = db.watchlistDao().getWatchlist().first()
        db.close()

        assertNotNull(kept)
        assertEquals("In watchlist", kept?.title)
        assertEquals(null, orphan)
        assertEquals(1, watchlist.size)
    }

    private companion object {
        const val TEST_DB = "migration_test.db"
        const val LEGACY_VERSION = 8
    }
}
