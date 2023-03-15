package com.chen.beeaudio.di

import android.util.Log
import com.chen.beeaudio.init.*
import com.chen.beeaudio.net.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RetrofitClientModel {

    @Singleton
    @Provides
    fun provideOKHttpClient() : OkHttpClient {
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
            Log.d("_chen", it)
        })
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
//            .addInterceptor(interceptor)          // 网络拦截在这里打开 <-
            .build()
    }

    @Singleton
    @Provides
    @Named("AuthServer")
    fun provideBasicAuthClient() : OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor(AUTH_CLIENT_USERNAME, AUTH_CLIENT_PASSWORD))
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit {

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(LOCAL_SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     *  云析API Client
     */
    @Singleton
    @Provides
    @Named("cloud_conn")
    fun provideCloudRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(CLOUD_CONN_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @Named("LocalServer")
    fun provideRemoteServer(retrofit: Retrofit) : LocalApi {
        return retrofit.create(LocalApi::class.java)
    }

    @Singleton
    @Provides
    @Named("LocalSearchServer")
    fun provideAudioSearchServer(retrofit: Retrofit) : LocalSearchApi {
        return retrofit.create(LocalSearchApi::class.java)
    }

    @Singleton
    @Provides
    @Named("LocalPremiumServer")
    fun providePremiumServer(retrofit: Retrofit) : LocalPremiumApi {
        return retrofit.create(LocalPremiumApi::class.java)
    }

    @Singleton
    @Provides
    @Named("AuthServer")
    fun provideAuthServer(
        @Named("AuthServer") okHttpClient: OkHttpClient
    ) : AuthApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(AUTH_SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    /**
     *  请求热门话题数据 Net Server
     */
    @Singleton
    @Provides
    fun provideTopServer(
        @Named("cloud_conn") retrofit: Retrofit
    ): CloudConnApi {
        return retrofit.create(CloudConnApi::class.java)
    }
}