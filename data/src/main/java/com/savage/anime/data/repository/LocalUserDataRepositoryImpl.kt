package com.savage.anime.data.repository

import com.savage.anime.data.local.dao.AnimeDao
import com.savage.anime.data.local.dao.ContinueWatchingDao
import com.savage.anime.data.local.dao.CustomListDao
import com.savage.anime.data.local.dao.EpisodeDao
import com.savage.anime.data.local.dao.LanguagePreferenceDao
import com.savage.anime.data.local.dao.SettingsDao
import com.savage.anime.data.local.dao.WatchHistoryDao
import com.savage.anime.data.local.dao.WatchlistDao
import com.savage.anime.data.local.entity.CustomListEntity
import com.savage.anime.data.local.entity.CustomListItemEntity
import com.savage.anime.data.local.entity.SettingsEntity
import com.savage.anime.data.local.entity.LanguagePreferenceEntity
import com.savage.anime.data.local.entity.WatchHistoryEntity
import com.savage.anime.data.local.entity.WatchlistEntity
import com.savage.anime.data.mapper.toDomain
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.domain.models.CustomList
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.models.WatchHistoryEntry
import com.savage.anime.domain.repository.LocalUserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalUserDataRepositoryImpl @Inject constructor(
    private val animeDao: AnimeDao,
    private val watchlistDao: WatchlistDao,
    private val continueWatchingDao: ContinueWatchingDao,
    private val languagePreferenceDao: LanguagePreferenceDao,
    private val settingsDao: SettingsDao,
    private val episodeDao: EpisodeDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val customListDao: CustomListDao
) : LocalUserDataRepository {

    override fun getWatchlist(): Flow<List<Anime>> =
        watchlistDao.getWatchlist()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun addToWatchlist(animeId: Int) {
        watchlistDao.add(WatchlistEntity(animeId))
    }

    override suspend fun removeFromWatchlist(animeId: Int) {
        watchlistDao.remove(animeId)
    }

    override fun isInWatchlist(animeId: Int): Flow<Boolean> =
        watchlistDao.isInWatchlist(animeId)

    override fun getContinueWatching(): Flow<List<ContinueWatchingItem>> =
        continueWatchingDao.getContinueWatching()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun savePosition(animeId: Int, episodeId: Int, positionMs: Long, durationMs: Long) {
        continueWatchingDao.save(
            com.savage.anime.data.local.entity.ContinueWatchingEntity(
                animeId = animeId,
                episodeId = episodeId,
                positionMs = positionMs,
                durationMs = durationMs,
                lastWatchedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getPosition(animeId: Int, episodeId: Int): Long? {
        return continueWatchingDao.getPosition(animeId, episodeId)?.positionMs
    }

    override suspend fun clearPosition(animeId: Int, episodeId: Int) {
        continueWatchingDao.clearPosition(animeId, episodeId)
    }

    override suspend fun clearAnimePosition(animeId: Int) {
        continueWatchingDao.clearAnimePosition(animeId)
    }

    override suspend fun getLastWatchedEpisode(animeId: Int): Episode? {
        val episodeId = continueWatchingDao.getLastWatchedEpisodeId(animeId) ?: return null
        val entity = episodeDao.getById(episodeId) ?: return null
        return entity.toDomain()
    }

    override suspend fun saveLanguagePreference(animeId: Int, preferredVersionId: Int) {
        languagePreferenceDao.save(
            LanguagePreferenceEntity(
                animeId = animeId,
                preferredVersionId = preferredVersionId,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getLanguagePreference(animeId: Int): Int? {
        return languagePreferenceDao.getPreference(animeId)?.preferredVersionId
    }

    override suspend fun getWatchedEpisodeIds(animeId: Int): List<Int> {
        return continueWatchingDao.getWatchedEpisodeIds(animeId)
    }

    override suspend fun getDuration(animeId: Int, episodeId: Int): Long? {
        return continueWatchingDao.getDuration(animeId, episodeId)
    }

    override suspend fun getAutoPlayNext(): Boolean {
        return settingsDao.getValue("auto_play_next")?.toBooleanStrictOrNull() ?: true
    }

    override suspend fun setAutoPlayNext(enabled: Boolean) {
        settingsDao.setValue(SettingsEntity("auto_play_next", enabled.toString()))
    }

    override suspend fun markEpisodeAsWatched(animeId: Int, episodeId: Int) {
        savePosition(animeId, episodeId, 1L)
    }

    override suspend fun markEpisodeAsUnwatched(animeId: Int, episodeId: Int) {
        clearPosition(animeId, episodeId)
    }

    override fun getWatchHistory(): Flow<List<WatchHistoryEntry>> =
        watchHistoryDao.getAll()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun addToWatchHistory(animeId: Int, episodeId: Int, animeTitle: String, animeImage: String, episodeNumber: Double) {
        watchHistoryDao.insert(
            WatchHistoryEntity(
                animeId = animeId,
                episodeId = episodeId,
                animeTitle = animeTitle,
                animeImage = animeImage,
                episodeNumber = episodeNumber
            )
        )
    }

    override suspend fun clearWatchHistory() {
        watchHistoryDao.clearAll()
    }

    override fun getCustomLists(): Flow<List<CustomList>> =
        customListDao.getAllLists()
            .map { entities ->
                entities.mapNotNull { entity ->
                    val itemIds = customListDao.getItems(entity.id)
                    val items = itemIds.mapNotNull { item ->
                        val cached = animeDao.getById(item.animeId)
                        cached?.toDomain()
                    }
                    CustomList(
                        id = entity.id,
                        name = entity.name,
                        createdAt = entity.createdAt,
                        items = items
                    )
                }
            }

    override suspend fun createCustomList(name: String): Int {
        return customListDao.createList(CustomListEntity(name = name)).toInt()
    }

    override suspend fun deleteCustomList(listId: Int) {
        customListDao.clearItems(listId)
        customListDao.deleteList(listId)
    }

    override suspend fun renameCustomList(listId: Int, name: String) {
        customListDao.renameList(listId, name)
    }

    override suspend fun addToCustomList(listId: Int, animeId: Int, position: Int) {
        customListDao.addItem(CustomListItemEntity(listId = listId, animeId = animeId, position = position))
    }

    override suspend fun removeFromCustomList(listId: Int, animeId: Int) {
        customListDao.removeItem(listId, animeId)
    }

    override suspend fun getCustomListItems(listId: Int): List<Int> {
        return customListDao.getItems(listId).map { it.animeId }
    }

    override suspend fun setAccentColor(color: Long) {
        settingsDao.setValue(SettingsEntity("accent_color", color.toString()))
    }

    override suspend fun getAccentColor(): Long? {
        return settingsDao.getValue("accent_color")?.toLongOrNull()
    }

    override fun getAccentColorFlow(): Flow<Long?> {
        return settingsDao.getValueFlow("accent_color").map { it?.toLongOrNull() }
    }
}
