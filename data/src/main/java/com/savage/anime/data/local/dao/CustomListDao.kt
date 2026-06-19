package com.savage.anime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savage.anime.data.local.entity.CustomListEntity
import com.savage.anime.data.local.entity.CustomListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomListDao {

    @Query("SELECT * FROM custom_lists ORDER BY created_at DESC")
    fun getAllLists(): Flow<List<CustomListEntity>>

    @Query("SELECT * FROM custom_lists WHERE id = :listId")
    suspend fun getListById(listId: Int): CustomListEntity?

    @Insert
    suspend fun createList(list: CustomListEntity): Long

    @Query("DELETE FROM custom_lists WHERE id = :listId")
    suspend fun deleteList(listId: Int)

    @Query("UPDATE custom_lists SET name = :name WHERE id = :listId")
    suspend fun renameList(listId: Int, name: String)

    @Query("SELECT * FROM custom_list_items WHERE list_id = :listId ORDER BY position ASC")
    suspend fun getItems(listId: Int): List<CustomListItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: CustomListItemEntity)

    @Query("DELETE FROM custom_list_items WHERE list_id = :listId AND anime_id = :animeId")
    suspend fun removeItem(listId: Int, animeId: Int)

    @Query("DELETE FROM custom_list_items WHERE list_id = :listId")
    suspend fun clearItems(listId: Int)

    @Query("UPDATE custom_list_items SET position = :position WHERE list_id = :listId AND anime_id = :animeId")
    suspend fun updateItemPosition(listId: Int, animeId: Int, position: Int)

    @Query("SELECT COUNT(*) FROM custom_list_items WHERE list_id = :listId")
    suspend fun getItemCount(listId: Int): Int
}
