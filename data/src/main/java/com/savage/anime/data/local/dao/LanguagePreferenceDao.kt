package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.LanguagePreferenceEntity

@Dao
interface LanguagePreferenceDao {

    @Query("SELECT * FROM language_preferences WHERE anime_id = :animeId")
    suspend fun getPreference(animeId: Int): LanguagePreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(preference: LanguagePreferenceEntity)

    @Query("DELETE FROM language_preferences WHERE anime_id = :animeId")
    suspend fun clear(animeId: Int)
}
