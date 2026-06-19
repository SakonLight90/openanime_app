package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.EpisodeCacheEntity
import com.savage.anime.data.local.entity.EpisodeWithAnime
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<EpisodeCacheEntity>)

    @Query("SELECT * FROM episodes_cache WHERE anime_id = :animeId ORDER BY number ASC")
    fun getByAnimeId(animeId: Int): Flow<List<EpisodeCacheEntity>>

    @Query("""
        SELECT e.id, e.anime_id AS animeId, e.number, e.title, e.token,
               a.title AS animeTitle, a.image AS animeImage
        FROM episodes_cache e
        INNER JOIN anime_cache a ON e.anime_id = a.id
        ORDER BY e.id DESC
    """)
    fun getLatest(): Flow<List<EpisodeWithAnime>>

    @Query("SELECT * FROM episodes_cache WHERE id = :episodeId")
    suspend fun getById(episodeId: Int): EpisodeCacheEntity?

    @Query("DELETE FROM episodes_cache WHERE anime_id = :animeId")
    suspend fun clearByAnimeId(animeId: Int)

    @Query("DELETE FROM episodes_cache")
    suspend fun clearAll()

    @Query("SELECT COUNT(DISTINCT anime_id) FROM episodes_cache")
    suspend fun getDistinctAnimeCount(): Int
}
