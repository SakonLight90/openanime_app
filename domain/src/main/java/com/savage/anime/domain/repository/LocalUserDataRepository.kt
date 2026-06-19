package com.savage.anime.domain.repository

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.domain.models.CustomList
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.models.WatchHistoryEntry
import kotlinx.coroutines.flow.Flow

interface LocalUserDataRepository {

    fun getWatchlist(): Flow<List<Anime>>

    suspend fun addToWatchlist(animeId: Int)
    suspend fun removeFromWatchlist(animeId: Int)
    fun isInWatchlist(animeId: Int): Flow<Boolean>

    fun getContinueWatching(): Flow<List<ContinueWatchingItem>>

    suspend fun savePosition(
        animeId: Int,
        episodeId: Int,
        positionMs: Long,
        durationMs: Long = 0L
    )

    suspend fun getPosition(
        animeId: Int,
        episodeId: Int
    ): Long?

    suspend fun clearPosition(
        animeId: Int,
        episodeId: Int
    )

    suspend fun clearAnimePosition(animeId: Int)

    suspend fun getLastWatchedEpisode(animeId: Int): Episode?

    suspend fun saveLanguagePreference(
        animeId: Int,
        preferredVersionId: Int
    )

    suspend fun getLanguagePreference(animeId: Int): Int?

    suspend fun getWatchedEpisodeIds(animeId: Int): List<Int>

    suspend fun getDuration(animeId: Int, episodeId: Int): Long?

    suspend fun getAutoPlayNext(): Boolean
    suspend fun setAutoPlayNext(enabled: Boolean)

    suspend fun markEpisodeAsWatched(animeId: Int, episodeId: Int)
    suspend fun markEpisodeAsUnwatched(animeId: Int, episodeId: Int)

    fun getWatchHistory(): Flow<List<WatchHistoryEntry>>
    suspend fun addToWatchHistory(animeId: Int, episodeId: Int, animeTitle: String, animeImage: String, episodeNumber: Double)
    suspend fun clearWatchHistory()

    fun getCustomLists(): Flow<List<CustomList>>
    suspend fun createCustomList(name: String): Int
    suspend fun deleteCustomList(listId: Int)
    suspend fun renameCustomList(listId: Int, name: String)
    suspend fun addToCustomList(listId: Int, animeId: Int, position: Int)
    suspend fun removeFromCustomList(listId: Int, animeId: Int)
    suspend fun getCustomListItems(listId: Int): List<Int>

    suspend fun setAccentColor(color: Long)
    suspend fun getAccentColor(): Long?
    fun getAccentColorFlow(): Flow<Long?>
}
