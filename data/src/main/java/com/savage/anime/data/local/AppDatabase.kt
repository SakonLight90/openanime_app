package com.savage.anime.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.savage.anime.data.local.dao.AnimeDao
import com.savage.anime.data.local.dao.CustomListDao
import com.savage.anime.data.local.dao.ContinueWatchingDao
import com.savage.anime.data.local.dao.EpisodeDao
import com.savage.anime.data.local.dao.LanguagePreferenceDao
import com.savage.anime.data.local.dao.SearchCacheDao
import com.savage.anime.data.local.dao.SettingsDao
import com.savage.anime.data.local.dao.WatchHistoryDao
import com.savage.anime.data.local.dao.WatchlistDao
import com.savage.anime.data.local.entity.AnimeCacheEntity
import com.savage.anime.data.local.entity.ContinueWatchingEntity
import com.savage.anime.data.local.entity.CustomListEntity
import com.savage.anime.data.local.entity.CustomListItemEntity
import com.savage.anime.data.local.entity.EpisodeCacheEntity
import com.savage.anime.data.local.entity.LanguagePreferenceEntity
import com.savage.anime.data.local.entity.SearchCacheEntity
import com.savage.anime.data.local.entity.SettingsEntity
import com.savage.anime.data.local.entity.WatchHistoryEntity
import com.savage.anime.data.local.entity.WatchlistEntity
import com.savage.anime.data.local.util.Converters

@Database(
    entities = [
        AnimeCacheEntity::class,
        EpisodeCacheEntity::class,
        WatchlistEntity::class,
        ContinueWatchingEntity::class,
        LanguagePreferenceEntity::class,
        SettingsEntity::class,
        SearchCacheEntity::class,
        WatchHistoryEntity::class,
        CustomListEntity::class,
        CustomListItemEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun continueWatchingDao(): ContinueWatchingDao
    abstract fun languagePreferenceDao(): LanguagePreferenceDao
    abstract fun settingsDao(): SettingsDao
    abstract fun searchCacheDao(): SearchCacheDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun customListDao(): CustomListDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE anime_cache ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS continue_watching_new (
                        anime_id INTEGER NOT NULL,
                        episode_id INTEGER NOT NULL DEFAULT 0,
                        positionMs INTEGER NOT NULL DEFAULT 0,
                        lastWatchedAt INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(anime_id, episode_id)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO continue_watching_new (anime_id, episode_id, positionMs, lastWatchedAt)
                    SELECT anime_id, 0, positionMs, lastWatchedAt FROM continue_watching
                """.trimIndent())
                db.execSQL("DROP TABLE continue_watching")
                db.execSQL("ALTER TABLE continue_watching_new RENAME TO continue_watching")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS app_settings (`key` TEXT NOT NULL PRIMARY KEY, `value` TEXT NOT NULL)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE continue_watching ADD COLUMN duration_ms INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
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
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS anime_cache_new (
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
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO anime_cache_new (id, title, synopsis, image, cover_image, banner_image, type, episode_count, rating, release_date, status, is_dub, language, genres, category, updated_at)
                    SELECT id, title, synopsis, image, cover_image, banner_image, type, episode_count, rating, release_date, status, is_dub, language, genres, category, updated_at FROM anime_cache
                """.trimIndent())
                db.execSQL("DROP TABLE anime_cache")
                db.execSQL("ALTER TABLE anime_cache_new RENAME TO anime_cache")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val columns = db.query("PRAGMA table_info(continue_watching)").use { cursor ->
                    buildSet {
                        while (cursor.moveToNext()) {
                            add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                        }
                    }
                }
                if ("positionMs" in columns) {
                    db.execSQL("ALTER TABLE continue_watching RENAME COLUMN positionMs TO position_ms")
                }
                if ("lastWatchedAt" in columns) {
                    db.execSQL("ALTER TABLE continue_watching RENAME COLUMN lastWatchedAt TO last_watched_at")
                }
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS language_preferences (
                        anime_id INTEGER NOT NULL,
                        preferred_version_id INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        PRIMARY KEY(anime_id)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS continue_watching_new (
                        anime_id INTEGER NOT NULL,
                        episode_id INTEGER NOT NULL DEFAULT 0,
                        position_ms INTEGER NOT NULL DEFAULT 0,
                        duration_ms INTEGER NOT NULL DEFAULT 0,
                        last_watched_at INTEGER NOT NULL DEFAULT 0,
                        anime_title TEXT NOT NULL DEFAULT '',
                        anime_image TEXT NOT NULL DEFAULT '',
                        episode_number REAL NOT NULL DEFAULT 0,
                        PRIMARY KEY(anime_id, episode_id)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO continue_watching_new (anime_id, episode_id, position_ms, duration_ms, last_watched_at)
                    SELECT anime_id, episode_id, position_ms, duration_ms, last_watched_at FROM continue_watching
                """.trimIndent())
                db.execSQL("DROP TABLE continue_watching")
                db.execSQL("ALTER TABLE continue_watching_new RENAME TO continue_watching")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS anime_cache_new (
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
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO anime_cache_new (id, title, synopsis, image, cover_image, banner_image, type, episode_count, rating, release_date, status, is_dub, language, genres, category, updated_at)
                    SELECT id, title, synopsis, image, cover_image, banner_image, type, episode_count, rating, release_date, status, is_dub, language, genres, category, updated_at FROM anime_cache
                """.trimIndent())
                db.execSQL("DROP TABLE anime_cache")
                db.execSQL("ALTER TABLE anime_cache_new RENAME TO anime_cache")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS search_cache_new (
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
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO search_cache_new (query, anime_id, title, image, type, episode_count, rating, release_date, status)
                    SELECT query, anime_id, title, image, type, episode_count, rating, release_date, status FROM search_cache
                """.trimIndent())
                db.execSQL("DROP TABLE search_cache")
                db.execSQL("ALTER TABLE search_cache_new RENAME TO search_cache")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS watch_history_new (
                        anime_id INTEGER NOT NULL,
                        episode_id INTEGER NOT NULL,
                        anime_title TEXT NOT NULL,
                        anime_image TEXT NOT NULL,
                        episode_number REAL NOT NULL DEFAULT 0,
                        watched_at INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(anime_id, episode_id)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO watch_history_new (anime_id, episode_id, anime_title, anime_image, episode_number, watched_at)
                    SELECT anime_id, episode_id, anime_title, anime_image, episode_number, watched_at FROM watch_history
                """.trimIndent())
                db.execSQL("DROP TABLE watch_history")
                db.execSQL("ALTER TABLE watch_history_new RENAME TO watch_history")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_lists_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        created_at INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO custom_lists_new (id, name, created_at)
                    SELECT id, name, created_at FROM custom_lists
                """.trimIndent())
                db.execSQL("DROP TABLE custom_lists")
                db.execSQL("ALTER TABLE custom_lists_new RENAME TO custom_lists")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_list_items_new (
                        list_id INTEGER NOT NULL,
                        anime_id INTEGER NOT NULL,
                        position INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(list_id, anime_id)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO custom_list_items_new (list_id, anime_id, position)
                    SELECT list_id, anime_id, position FROM custom_list_items
                """.trimIndent())
                db.execSQL("DROP TABLE custom_list_items")
                db.execSQL("ALTER TABLE custom_list_items_new RENAME TO custom_list_items")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS watch_history (
                        anime_id INTEGER NOT NULL,
                        episode_id INTEGER NOT NULL,
                        anime_title TEXT NOT NULL,
                        anime_image TEXT NOT NULL,
                        episode_number REAL NOT NULL DEFAULT 0,
                        watched_at INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(anime_id, episode_id)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_lists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        created_at INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_list_items (
                        list_id INTEGER NOT NULL,
                        anime_id INTEGER NOT NULL,
                        position INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(list_id, anime_id)
                    )
                """.trimIndent())
            }
        }
    }
}
