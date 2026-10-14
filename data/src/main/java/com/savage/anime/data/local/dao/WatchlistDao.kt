package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.AnimeCacheEntity
import com.savage.anime.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query(
        """
        SELECT a.* FROM anime_cache a
        INNER JOIN watchlist w ON a.id = w.anime_id
        GROUP BY a.id
        ORDER BY MAX(w.added_at) DESC
        """
    )
    fun getWatchlist(): Flow<List<AnimeCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(watchlist: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE anime_id = :animeId")
    suspend fun remove(animeId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE anime_id = :animeId)")
    fun isInWatchlist(animeId: Int): Flow<Boolean>
}
