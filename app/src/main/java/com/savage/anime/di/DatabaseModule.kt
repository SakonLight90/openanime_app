package com.savage.anime.di

import android.content.Context
import androidx.room.Room
import com.savage.anime.data.local.AppDatabase
import com.savage.anime.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "anime_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides fun provideAnimeDao(db: AppDatabase): AnimeDao = db.animeDao()

    @Provides fun provideEpisodeDao(db: AppDatabase): EpisodeDao = db.episodeDao()

    @Provides fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()

    @Provides fun provideContinueWatchingDao(db: AppDatabase): ContinueWatchingDao = db.continueWatchingDao()

    @Provides fun provideLanguagePreferenceDao(db: AppDatabase): LanguagePreferenceDao = db.languagePreferenceDao()

    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()

    @Provides fun provideSearchCacheDao(db: AppDatabase): SearchCacheDao = db.searchCacheDao()

    @Provides fun provideWatchHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides fun provideCustomListDao(db: AppDatabase): CustomListDao = db.customListDao()
}
