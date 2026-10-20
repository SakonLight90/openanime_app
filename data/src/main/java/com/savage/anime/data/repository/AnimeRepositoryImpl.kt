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
import com.savage.anime.domain.models.PagedAnime
import com.savage.anime.domain.models.StreamResponse
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.util.cleanAnimeTitle
import com.savage.anime.domain.util.filterItaDubDuplicates
import kotlinx.coroutines.CancellationException
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
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    emptyList()
                }
                val enriched = remote.copy(episodes = episodes)
                val categories = animeDao.getCategoriesByAnimeId(id)
                if (categories.isEmpty()) {
                    animeDao.insert(enriched.toCacheEntity(category = ""))
                } else {
                    animeDao.insertAll(categories.map { enriched.toCacheEntity(category = it) })
                }
                if (episodes.isNotEmpty()) {
                    episodeDao.insertAll(episodes.map { it.toCacheEntity(id) })
                }
                emit(enriched)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (cached == null) throw e
            }
        }

    override fun search(
        query: String,
        type: String?,
        status: String?,
        year: String?,
        language: String?,
        page: Int
    ): Flow<PagedAnime> = flow {
        val normalized = query.trim().lowercase()
        val hasFilters = !type.isNullOrBlank() || !status.isNullOrBlank() || !year.isNullOrBlank() || !language.isNullOrBlank()

        val cached = if (page == 1 && !hasFilters) {
            searchCacheDao.getByQuery(normalized)
        } else {
            emptyList()
        }

        if (cached.isNotEmpty()) {
            val items = cached.map { it.toDomain() }
            emit(PagedAnime(items = items, page = 1, pageSize = items.size, total = items.size, totalPages = 1))
        }

        try {
            val response = api.search(
                query = query,
                type = type,
                status = status,
                year = year,
                language = language,
                page = page
            )
            val items = response.items.cleanList()

            if (page == 1 && !hasFilters) {
                searchCacheDao.clearByQuery(normalized)
                searchCacheDao.insertAll(response.items.map { it.toSearchCacheEntity(normalized) })
            }

            emit(
                PagedAnime(
                    items = items,
                    page = response.page,
                    pageSize = response.pageSize,
                    total = response.total,
                    totalPages = response.totalPages
                )
            )
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
            val collected = mutableListOf<Anime>()
            var page = 1
            var totalPages = 1
            do {
                val response = api.getAzList(letter, page)
                collected += response.items
                totalPages = response.totalPages.coerceAtLeast(1)
                page++
            } while (page <= totalPages)

            val results = collected.distinctBy { it.id }
            animeDao.insertAll(results.mapIndexed { i, a -> a.toCacheEntity(category, i) })
            emit(results.cleanList())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cached.isEmpty()) throw e
        }
    }

    override fun getLatestEpisodes(): Flow<List<Episode>> =
        episodeDao.getLatest().map { list ->
            list.map { it.toDomainEpisode() }
                .distinctBy { Pair(cleanAnimeTitle(it.anime?.title ?: ""), it.number) }
                .distinctBy { it.id }
        }

    override fun getGenreAnime(genreId: Int, page: Int): Flow<PagedAnime> = flow {
        val category = "genre:$genreId"
        val cached = if (page == 1) animeDao.getByCategoryList(category) else emptyList()

        if (cached.isNotEmpty()) {
            val items = cached.toDomainFiltered()
            emit(
                PagedAnime(
                    items = items,
                    page = 1,
                    pageSize = items.size,
                    total = items.size,
                    totalPages = 1
                )
            )
        }

        try {
            val response = api.getGenreAnime(genreId, page)
            animeDao.insertAll(response.items.mapIndexed { i, a -> a.toCacheEntity(category, (page - 1) * response.pageSize.coerceAtLeast(1) + i) })
            emit(
                PagedAnime(
                    items = response.items.cleanList(),
                    page = response.page,
                    pageSize = response.pageSize,
                    total = response.total,
                    totalPages = response.totalPages
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cached.isEmpty()) throw e
        }
    }

    override suspend fun getStreamUrl(token: String): StreamResponse {
        val dto = api.getStreamUrl(token)
        return dto.toDomain()
    }

    override suspend fun getEpisodeById(episodeId: Int): Episode? {
        val entity = episodeDao.getByIdWithAnime(episodeId) ?: return null
        return entity.toDomainEpisode()
    }

    override suspend fun fetchHome() {
        val home = api.getHome()
        val animeEntities = mutableListOf<AnimeCacheEntity>()
        animeEntities.addAll(home.hero.mapIndexed { i, a -> a.toCacheEntity("trending", i) })
        animeEntities.addAll(home.popular.mapIndexed { i, a -> a.toCacheEntity("popular", i) })
        animeEntities.addAll(home.ongoing.mapIndexed { i, a -> a.toCacheEntity("ongoing", i) })
        animeEntities.addAll(home.upcoming.mapIndexed { i, a -> a.toCacheEntity("upcoming", i) })
        animeEntities.addAll(home.newest.mapIndexed { i, a -> a.toCacheEntity("newest", i) })
        try {
            val updated = api.getUpdated()
            animeEntities.addAll(updated.mapIndexed { i, a -> a.toCacheEntity("updated", i) })
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) { }
        animeDao.insertAll(animeEntities)
        episodeDao.insertAll(
            home.latestEpisodes.mapNotNull { episode ->
                episode.anime?.id?.let { episode.toCacheEntity(animeId = it) }
            }
        )
    }

    override suspend fun getGenres(): List<Genre> {
        return api.getGenres().map { it.toDomainGenre() }
    }

    override fun getUpdatedFeed(): Flow<List<Anime>> =
        animeDao.getByCategory("updated").map { list ->
            list.toDomainFiltered()
        }

    override suspend fun clearCache() {
        animeDao.clearUnreferenced()
        episodeDao.clearAll()
        searchCacheDao.clearAll()
    }

    override suspend fun getCacheCount(): Int {
        return animeDao.getCount()
    }
}
