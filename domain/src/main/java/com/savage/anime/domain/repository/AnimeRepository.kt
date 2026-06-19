package com.savage.anime.domain.repository

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.AnimeDetail
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.models.Genre
import com.savage.anime.domain.models.StreamResponse
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {

    fun getTrending(): Flow<List<Anime>>
    fun getPopular(): Flow<List<Anime>>
    fun getOngoing(): Flow<List<Anime>>
    fun getUpcoming(): Flow<List<Anime>>
    fun getNewest(): Flow<List<Anime>>

    fun getDetail(id: Int): Flow<AnimeDetail>

    fun search(query: String): Flow<List<Anime>>

    fun getAzList(letter: String): Flow<List<Anime>>

    fun getLatestEpisodes(): Flow<List<Episode>>

    fun getGenreAnime(genreId: Int): Flow<List<Anime>>

    fun getUpdatedFeed(): Flow<List<Anime>>

    suspend fun getGenres(): List<Genre>

    suspend fun getStreamUrl(token: String): StreamResponse
    suspend fun getEpisodeById(episodeId: Int): Episode?
    suspend fun fetchHome()
    suspend fun clearCache()
    suspend fun getCacheCount(): Int
}
