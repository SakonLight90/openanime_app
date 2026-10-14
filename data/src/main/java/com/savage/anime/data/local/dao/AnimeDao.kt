package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.AnimeCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime_cache")
    fun getAll(): Flow<List<AnimeCacheEntity>>

    @Query("SELECT * FROM anime_cache WHERE category = :category ORDER BY sort_order ASC")
    fun getByCategory(category: String): Flow<List<AnimeCacheEntity>>

    @Query("SELECT * FROM anime_cache WHERE category = :category ORDER BY sort_order ASC")
    suspend fun getByCategoryList(category: String): List<AnimeCacheEntity>

    @Query("""
        SELECT * FROM anime_cache
        WHERE id = :id
        ORDER BY (category = '') DESC, LENGTH(synopsis) DESC, updated_at DESC
        LIMIT 1
    """)
    suspend fun getById(id: Int): AnimeCacheEntity?

    @Query("SELECT DISTINCT category FROM anime_cache WHERE id = :id")
    suspend fun getCategoriesByAnimeId(id: Int): List<String>

    @Query("""
        SELECT a.* FROM anime_cache a
        INNER JOIN custom_list_items ci ON a.id = ci.anime_id
        WHERE ci.list_id = :listId
        GROUP BY a.id
        ORDER BY MAX(ci.position) ASC
    """)
    suspend fun getByCustomList(listId: Int): List<AnimeCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<AnimeCacheEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeCacheEntity)

    @Query("DELETE FROM anime_cache")
    suspend fun clearAll()

    @Query("""
        DELETE FROM anime_cache
        WHERE id NOT IN (SELECT anime_id FROM watchlist)
          AND id NOT IN (SELECT anime_id FROM custom_list_items)
    """)
    suspend fun clearUnreferenced()

    @Query("SELECT COUNT(DISTINCT id) FROM anime_cache")
    suspend fun getCount(): Int
}
