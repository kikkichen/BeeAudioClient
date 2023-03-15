package com.chen.beeaudio.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chen.beeaudio.model.localmodel.Blog
import com.chen.beeaudio.model.localmodel.BlogRemoteKeys
import com.chen.beeaudio.repository.dao.BlogDao
import com.chen.beeaudio.repository.dao.BlogRemoteKeyDao
import com.chen.beeaudio.repository.dao.RemoteKeyTransaction

@Database(entities = [Blog::class, BlogRemoteKeys::class], version = 1, exportSchema = false)
abstract class BlogsDatabase : RoomDatabase() {
    abstract fun blogImageDao(): BlogDao
    abstract fun blogRemoteKeysDao(): BlogRemoteKeyDao
    abstract fun remoteKeyTransaction(): RemoteKeyTransaction
}