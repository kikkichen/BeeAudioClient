package com.chen.beeaudio.di

import android.content.Context
import androidx.room.Room
import com.chen.beeaudio.init.LOCAL_ROOM_TRACK_SUMMARY_TABLE
import com.chen.beeaudio.repository.database.TrackSummaryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrackSummaryDatabaseModel {
    @Provides
    @Singleton
    fun provideTrackSummaryDatabase(
        @ApplicationContext context: Context
    ) : TrackSummaryDatabase {
        return Room.databaseBuilder(
            context,
            TrackSummaryDatabase::class.java,
            LOCAL_ROOM_TRACK_SUMMARY_TABLE
        ).build()
    }
}