package com.savage.anime.data.repository

import com.savage.anime.data.local.dao.AnimeDao
import com.savage.anime.data.local.dao.EpisodeDao
import com.savage.anime.data.local.dao.SearchCacheDao
import com.savage.anime.data.local.entity.AnimeCacheEntity
import com.savage.anime.data.local.entity.SearchCacheEntity
import com.savage.anime.data.mapper.toCacheEntity
import com.savage.anime.data.mapper.toDomain
import com.savage.anime.data.mapper.toDomainDetail
import com.savage.anime.data.mapper.toDomainEpisode
import com.savage.anime.data.mapper.toSearchCacheEntity
import com.savage.anime.data.mapper.toDomain as toDomainGenre
import com.savage.anime.data.mapper.toDomain as toStreamResponseDomain
import com.savage.anime.data.network.api.AnimeApi
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.AnimeDetail
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.models.Genre
import com.savage.anime.domain.models.StreamResponse
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.util.cleanAnimeTitle
import com.savage.anime.domain.util.filterItaDubDuplicates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AnimeRepositoryImpl(
    private val api: AnimeApi,
    private val animeDao: AnimeDao,
    private val episodeDao: EpisodeDao,
    private val searchCacheDao: SearchCacheDao
) : AnimeRepository {

    private fun List<AnimeCacheEntity>.toDomainFiltered(): List<Anime> {
        return filterItaDubDuplicates { it.title }
            .map { it.toDomain() }
    }

    private fun List<Anime>.cleanList(): List<Anime> {
        return filterItaDubDuplicates { it.title }
            .map { it.copy(title = cleanAnimeTitle(it.title)) }
    }

    override fun getTrending(): Flow<List<Anime>> =
        animeDao.getByCategory("trending").map { list ->
            list.toDomainFiltered()
        }

    override fun getPopular(): Flow<List<Anime>> =
        animeDao.getByCategory("popular").map { list ->
            list.toDomainFiltered()
        }

    override fun getOngoing(): Flow<List<Anime>> =
        animeDao.getByCategory("ongoing").map { list ->
            list.toDomainFiltered()
        }

    override fun getUpcoming(): Flow<List<Anime>> =
        animeDao.getByCategory("upcoming").map { list ->
            list.toDomainFiltered()
        }

    override fun getNewest(): Flow<List<Anime>> =
        animeDao.getByCategory("newest").map { list ->
            list.toDomainFiltered()
        }

    override fun getDetail(id: Int): Flow<AnimeDetail> =
        flow {
            val cached = animeDao.getById(id)
            if (cached != null) {
                emit(cached.toDomainDetail())
            }
            try {
                val remote = api.getDetail(id)
                val episodes = try {
                    api.getEpisodes(id)
                } catch (_: Exception) {
                    emptyList()
                }
                val enriched = remote.copy(episodes = episodes)
                val existingCategory = animeDao.getById(id)?.category ?: ""
                animeDao.insert(enriched.toCacheEntity(category = existingCategory))
                if (episodes.isNotEmpty()) {
                    episodeDao.insertAll(episodes.map { it.toCacheEntity(id) })
                }
                emit(enriched)
            } catch (e: Exception) {
                if (cached == null) throw e
            }
        }

    override fun search(query: String): Flow<List<Anime>> = flow {
        val normalized = query.trim().lowercase()
        val cached = searchCacheDao.getByQuery(normalized)
        if (cached.isNotEmpty()) {
            emit(cached.map { it.toDomain() })
        }
        try {
            val results = api.search(query)
            searchCacheDao.clearByQuery(normalized)
            searchCacheDao.insertAll(results.map { it.toSearchCacheEntity(normalized) })
            emit(results.cleanList())
        } catch (e: Exception) {
            if (cached.isEmpty()) throw e
        }
    }

    override fun getAzList(letter: String): Flow<List<Anime>> = flow {
        val category = "az:$letter"
        val cached = animeDao.getByCategoryList(category)
        if (cached.isNotEmpty()) {
            emit(cached.toDomainFiltered())
        }
        try {
            val results = api.getAzList(letter)
            animeDao.insertAll(results.map { it.toCacheEntity(category) })
            emit(results.cleanList())
        } catch (e: Exception) {
            if (cached.isEmpty()) throw e
        }
    }

    override fun getLatestEpisodes(): Flow<List<Episode>> =
        episodeDao.getLatest().map { list ->
            list.map { it.toDomainEpisode() }
                .distinctBy { Pair(cleanAnimeTitle(it.anime?.title ?: ""), it.number) }
        }

    override fun getGenreAnime(genreId: Int): Flow<List<Anime>> = flow {
        val category = "genre:$genreId"
        val cached = animeDao.getByCategoryList(category)
        if (cached.isNotEmpty()) {
            emit(cached.toDomainFiltered())
        }
        try {
            val results = api.getGenreAnime(genreId)
            animeDao.insertAll(results.map { it.toCacheEntity(category) })
            emit(results.cleanList())
        } catch (e: Exception) {
            if (cached.isEmpty()) throw e
        }
    }

    override suspend fun getStreamUrl(token: String): StreamResponse {
        val dto = api.getStreamUrl(token)
        return dto.toDomain()
    }

    override suspend fun getEpisodeById(episodeId: Int): Episode? {
        val entity = episodeDao.getById(episodeId) ?: return null
        return entity.toDomain()
    }

    override suspend fun fetchHome() {
        val home = api.getHome()
        animeDao.insertAll(home.hero.map { it.toCacheEntity("trending") })
        animeDao.insertAll(home.popular.map { it.toCacheEntity("popular") })
        animeDao.insertAll(home.ongoing.map { it.toCacheEntity("ongoing") })
        animeDao.insertAll(home.upcoming.map { it.toCacheEntity("upcoming") })
        animeDao.insertAll(home.newest.map { it.toCacheEntity("newest") })
        episodeDao.insertAll(
            home.latestEpisodes.map { it.toCacheEntity(animeId = it.anime?.id ?: 0) }
        )
        try {
            val updated = api.getUpdated()
            animeDao.insertAll(updated.map { it.toCacheEntity("updated") })
        } catch (_: Exception) { }
    }

    override suspend fun getGenres(): List<Genre> {
        return api.getGenres().map { it.toDomainGenre() }
    }

    override fun getUpdatedFeed(): Flow<List<Anime>> =
        animeDao.getByCategory("updated").map { list ->
            list.toDomainFiltered()
        }

    override suspend fun clearCache() {
        animeDao.clearAll()
        episodeDao.clearAll()
        searchCacheDao.clearAll()
    }

    override suspend fun getCacheCount(): Int {
        return animeDao.getCount()
    }
}
