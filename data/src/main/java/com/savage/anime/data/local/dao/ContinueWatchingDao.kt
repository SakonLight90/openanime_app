package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.ContinueWatchingEntity
import com.savage.anime.data.local.entity.ContinueWatchingWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ContinueWatchingDao {

    @Query("""
        SELECT DISTINCT
            c.anime_id,
            c.episode_id,
            c.position_ms,
            c.duration_ms,
            c.last_watched_at,
            a.title as animeTitle,
            a.image as animeImage,
            e.number as episodeNumber,
            e.title as episodeTitle
        FROM continue_watching c
        INNER JOIN anime_cache a ON c.anime_id = a.id
        INNER JOIN episodes_cache e ON c.episode_id = e.id
        INNER JOIN (
            SELECT c2.anime_id, MAX(c2.last_watched_at) as max_watched
            FROM continue_watching c2
            GROUP BY c2.anime_id
        ) latest ON c.anime_id = latest.anime_id AND c.last_watched_at = latest.max_watched
        ORDER BY c.last_watched_at DESC
    """)
    fun getContinueWatching(): Flow<List<ContinueWatchingWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(continueWatching: ContinueWatchingEntity)

    @Query("""
        SELECT * FROM continue_watching 
        WHERE anime_id = :animeId AND episode_id = :episodeId
    """)
    suspend fun getPosition(animeId: Int, episodeId: Int): ContinueWatchingEntity?

    @Query("DELETE FROM continue_watching WHERE anime_id = :animeId AND episode_id = :episodeId")
    suspend fun clearPosition(animeId: Int, episodeId: Int)

    @Query("SELECT episode_id FROM continue_watching WHERE anime_id = :animeId")
    suspend fun getWatchedEpisodeIds(animeId: Int): List<Int>

    @Query("DELETE FROM continue_watching WHERE anime_id = :animeId")
    suspend fun clearAnimePosition(animeId: Int)

    @Query("SELECT episode_id FROM continue_watching WHERE anime_id = :animeId ORDER BY last_watched_at DESC LIMIT 1")
    suspend fun getLastWatchedEpisodeId(animeId: Int): Int?

    @Query("SELECT position_ms FROM continue_watching WHERE anime_id = :animeId AND episode_id = :episodeId")
    suspend fun getPositionOnly(animeId: Int, episodeId: Int): Long?

    @Query("SELECT duration_ms FROM continue_watching WHERE anime_id = :animeId AND episode_id = :episodeId")
    suspend fun getDuration(animeId: Int, episodeId: Int): Long?
}
