package com.savage.anime.di

import com.savage.anime.data.local.dao.AnimeDao
import com.savage.anime.data.local.dao.ContinueWatchingDao
import com.savage.anime.data.local.dao.CustomListDao
import com.savage.anime.data.local.dao.EpisodeDao
import com.savage.anime.data.local.dao.LanguagePreferenceDao
import com.savage.anime.data.local.dao.SearchCacheDao
import com.savage.anime.data.local.dao.SettingsDao
import com.savage.anime.data.local.dao.WatchHistoryDao
import com.savage.anime.data.local.dao.WatchlistDao
import com.savage.anime.data.network.api.AnimeApi
import com.savage.anime.data.repository.AnimeRepositoryImpl
import com.savage.anime.data.repository.LocalUserDataRepositoryImpl
import com.savage.anime.domain.repository.AnimeRepository
import com.savage.anime.domain.repository.LocalUserDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnimeRepository(
        api: AnimeApi,
        animeDao: AnimeDao,
        episodeDao: EpisodeDao,
        searchCacheDao: SearchCacheDao
    ): AnimeRepository {
        return AnimeRepositoryImpl(
            api = api,
            animeDao = animeDao,
            episodeDao = episodeDao,
            searchCacheDao = searchCacheDao
        )
    }

    @Provides
    @Singleton
    fun provideLocalUserDataRepository(
        animeDao: AnimeDao,
        watchlistDao: WatchlistDao,
        continueWatchingDao: ContinueWatchingDao,
        languagePreferenceDao: LanguagePreferenceDao,
        settingsDao: SettingsDao,
        episodeDao: EpisodeDao,
        watchHistoryDao: WatchHistoryDao,
        customListDao: CustomListDao
    ): LocalUserDataRepository {
        return LocalUserDataRepositoryImpl(
            animeDao = animeDao,
            watchlistDao = watchlistDao,
            continueWatchingDao = continueWatchingDao,
            languagePreferenceDao = languagePreferenceDao,
            settingsDao = settingsDao,
            episodeDao = episodeDao,
            watchHistoryDao = watchHistoryDao,
            customListDao = customListDao
        )
    }
}
