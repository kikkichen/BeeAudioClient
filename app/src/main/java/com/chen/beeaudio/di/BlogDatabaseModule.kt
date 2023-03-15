package com.chen.beeaudio.di

import android.content.Context
import androidx.room.Room
import com.chen.beeaudio.init.LOCAL_ROOM_DATABASE_NAME
import com.chen.beeaudio.repository.database.BlogsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BlogDatabaseModule {

    @Provides
    @Singleton
    fun provideBlogDatabase(
        @ApplicationContext context: Context
    ) : BlogsDatabase {
        return Room.databaseBuilder(
            context,
            BlogsDatabase::class.java,
            LOCAL_ROOM_DATABASE_NAME
        ).build()
    }

}