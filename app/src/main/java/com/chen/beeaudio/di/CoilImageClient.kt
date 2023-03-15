package com.chen.beeaudio.di

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.transform.CircleCropTransformation
import com.chen.beeaudio.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoilImageClient {

    @Provides
    @Singleton
    @Named("PlayListCover")
    fun providePlayListCoverImageLoader(
        @ApplicationContext context: Context
    ) : ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(durationMillis = 1000)
            .build()
    }

    @Provides
    @Singleton
    @Named("PlayListPageBackground")
    fun providesPlayListBackGroundImageLoader(
        @ApplicationContext context: Context
    ) : ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(durationMillis = 300)
            .build()
    }
}