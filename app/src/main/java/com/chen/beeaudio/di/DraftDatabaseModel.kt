package com.chen.beeaudio.di

import android.content.Context
import androidx.room.Room
import com.chen.beeaudio.init.DRAFT_DATABASE_NAME
import com.chen.beeaudio.repository.database.DraftDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DraftDatabaseModel {
    @Provides
    @Singleton
    fun provideDraftDatabase(
        @ApplicationContext context: Context
    ) : DraftDatabase {
        return Room.databaseBuilder(
            context,
            DraftDatabase::class.java,
            DRAFT_DATABASE_NAME
        ).build()
    }
}