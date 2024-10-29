package com.beemer.seoulbike.model.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.beemer.seoulbike.model.dao.FavoriteStationDao
import com.beemer.seoulbike.model.database.Database
import com.beemer.seoulbike.model.repository.BikeRepository
import com.beemer.seoulbike.model.repository.FavoriteStationRepository
import com.beemer.seoulbike.model.service.RetrofitService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = RetrofitService.getRetrofit()

    @Provides
    @Singleton
    fun provideDatabase(context: Context): Database = Room.databaseBuilder(context, Database::class.java, "database").build()

    @Provides
    @Singleton
    fun provideFavoriteStationDao(database: Database): FavoriteStationDao = database.favoriteStationDao()

    @Provides
    @Singleton
    fun provideFavoriteStationRepository(dao: FavoriteStationDao): FavoriteStationRepository = FavoriteStationRepository(dao)

    @Provides
    @Singleton
    fun provideBikeRepository(retrofit: Retrofit): BikeRepository = BikeRepository(retrofit)
}