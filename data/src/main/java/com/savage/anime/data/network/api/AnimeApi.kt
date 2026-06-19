package com.savage.anime.data.network.api

import com.savage.anime.data.network.dto.AppVersionResponse
import com.savage.anime.data.network.dto.GenreDto
import com.savage.anime.data.network.dto.HomeResponse
import com.savage.anime.data.network.dto.StreamResponseDto
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.AnimeDetail
import com.savage.anime.domain.models.Episode
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeApi {

    @GET("api/v2/home")
    suspend fun getHome(): HomeResponse

    @GET("api/v2/anime/newest")
    suspend fun getNewest(): List<Anime>

    @GET("api/v2/anime/updated")
    suspend fun getUpdated(): List<Anime>

    @GET("api/v2/anime/ongoing")
    suspend fun getOngoing(): List<Anime>

    @GET("api/v2/anime/upcoming")
    suspend fun getUpcoming(
        @Query("year") year: Int? = null,
        @Query("season") season: String? = null
    ): List<Anime>

    @GET("api/v2/anime/az-list/{letter}")
    suspend fun getAzList(
        @Path("letter") letter: String
    ): List<Anime>

    @GET("api/v2/anime/tops/{period}")
    suspend fun getTops(
        @Path("period") period: String
    ): List<Anime>

    @GET("api/v2/anime/{id}")
    suspend fun getDetail(
        @Path("id") id: Int
    ): AnimeDetail

    @GET("api/v2/anime/{id}/episodes")
    suspend fun getEpisodes(
        @Path("id") id: Int
    ): List<Episode>

    @GET("api/v2/search")
    suspend fun search(
        @Query("q") query: String
    ): List<Anime>

    @GET("api/v2/stream")
    suspend fun getStreamUrl(
        @Query("token") token: String
    ): StreamResponseDto

    @GET("api/v2/genres")
    suspend fun getGenres(): List<GenreDto>

    @GET("api/v2/genres/{id}/anime")
    suspend fun getGenreAnime(
        @Path("id") id: Int
    ): List<Anime>

    @GET("api/v2/app-version")
    suspend fun getAppVersion(): AppVersionResponse
}
