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

    @Query("SELECT * FROM anime_cache WHERE category = :category")
    fun getByCategory(category: String): Flow<List<AnimeCacheEntity>>

    @Query("SELECT * FROM anime_cache WHERE category = :category")
    suspend fun getByCategoryList(category: String): List<AnimeCacheEntity>

    @Query("SELECT * FROM anime_cache WHERE id = :id")
    suspend fun getById(id: Int): AnimeCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<AnimeCacheEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeCacheEntity)

    @Query("DELETE FROM anime_cache")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM anime_cache")
    suspend fun getCount(): Int
}
