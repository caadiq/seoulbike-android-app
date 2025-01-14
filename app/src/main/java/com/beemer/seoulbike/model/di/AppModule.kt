package com.beemer.seoulbike.model.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.beemer.seoulbike.model.dao.FavoriteStationDao
import com.beemer.seoulbike.model.dao.SearchHistoryDao
import com.beemer.seoulbike.model.data.DataStoreModule
import com.beemer.seoulbike.model.database.Database
import com.beemer.seoulbike.model.repository.AuthRepository
import com.beemer.seoulbike.model.repository.BikeRepository
import com.beemer.seoulbike.model.repository.DataStoreRepository
import com.beemer.seoulbike.model.repository.FavoriteRepository
import com.beemer.seoulbike.model.repository.FavoriteStationRepository
import com.beemer.seoulbike.model.repository.PopularRepository
import com.beemer.seoulbike.model.repository.SearchHistoryRepository
import com.beemer.seoulbike.model.service.RetrofitService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BasicRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideRetrofitService(dataStoreRepository: DataStoreRepository): RetrofitService = RetrofitService(dataStoreRepository)

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideRetrofit(retrofitService: RetrofitService): Retrofit = retrofitService.getRetrofit()

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(retrofitService: RetrofitService): Retrofit = retrofitService.getAuthRetrofit()

    @Provides
    @Singleton
    fun provideDataStoreModule(@ApplicationContext context: Context) = DataStoreModule(context)

    @Provides
    @Singleton
    fun provideDatabase(context: Context): Database = Room.databaseBuilder(context, Database::class.java, "database").build()

    @Provides
    @Singleton
    fun provideFavoriteStationDao(database: Database): FavoriteStationDao = database.favoriteStationDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: Database): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    @Singleton
    fun provideFavoriteStationRepository(dao: FavoriteStationDao): FavoriteStationRepository = FavoriteStationRepository(dao)

    @Provides
    @Singleton
    fun provideSearchHistoryRepository(dao: SearchHistoryDao): SearchHistoryRepository = SearchHistoryRepository(dao)

    @Provides
    @Singleton
    fun provideDataStoreRepository(dataStoreModule: DataStoreModule) = DataStoreRepository(dataStoreModule)

    @Provides
    @Singleton
    fun provideAuthRepository(@BasicRetrofit retrofit: Retrofit, @AuthRetrofit authRetrofit: Retrofit): AuthRepository = AuthRepository(retrofit, authRetrofit)

    @Provides
    @Singleton
    fun provideBikeRepository(@BasicRetrofit retrofit: Retrofit): BikeRepository = BikeRepository(retrofit)

    @Provides
    @Singleton
    fun provideFavoriteRepository(@AuthRetrofit authRetrofit: Retrofit) = FavoriteRepository(authRetrofit)

    @Provides
    @Singleton
    fun providePopularRepository(@BasicRetrofit retrofit: Retrofit) = PopularRepository(retrofit)
}