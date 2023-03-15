package com.chen.beeaudio.di

import android.content.Context
import androidx.room.Room
import com.chen.beeaudio.init.LOCAL_ROOM_SUBSCRIBE_TABLE
import com.chen.beeaudio.repository.database.SubscribeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubscribeDatabaseModel {
    @Provides
    @Singleton
    fun provideSubscribeDatabase(
        @ApplicationContext context: Context
    ) : SubscribeDatabase {
        return Room.databaseBuilder(
            context,
            SubscribeDatabase::class.java,
            LOCAL_ROOM_SUBSCRIBE_TABLE
        ).build()
    }
}