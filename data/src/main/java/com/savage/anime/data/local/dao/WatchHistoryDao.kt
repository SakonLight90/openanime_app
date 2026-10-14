package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY watched_at DESC")
    fun getAll(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE anime_id = :animeId AND episode_id = :episodeId")
    suspend fun delete(animeId: Int, episodeId: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
