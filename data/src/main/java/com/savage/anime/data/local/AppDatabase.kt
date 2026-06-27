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
    version = 7,
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
                db.execSQL("INSERT INTO anime_cache_new SELECT * FROM anime_cache")
                db.execSQL("DROP TABLE anime_cache")
                db.execSQL("ALTER TABLE anime_cache_new RENAME TO anime_cache")
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
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
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
