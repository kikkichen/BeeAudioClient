package com.chen.beeaudio.di

import android.content.Context
import com.chen.beeaudio.repository.DataStoreManager
import com.chen.beeaudio.repository.DataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providerDataStoreManager(
        @ApplicationContext context: Context
    ) = DataStoreManager(context)

    @Provides
    @Singleton
    fun providerRepository(dataStoreManager: DataStoreManager) : DataStoreRepository {
        return DataStoreRepository(dataStoreManager)
    }
}