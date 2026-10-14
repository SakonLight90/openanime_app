package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.SearchCacheEntity

@Dao
interface SearchCacheDao {

    @Query("SELECT * FROM search_cache WHERE query = :query")
    suspend fun getByQuery(query: String): List<SearchCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<SearchCacheEntity>)

    @Query("DELETE FROM search_cache WHERE query = :query")
    suspend fun clearByQuery(query: String)

    @Query("DELETE FROM search_cache")
    suspend fun clearAll()
}
