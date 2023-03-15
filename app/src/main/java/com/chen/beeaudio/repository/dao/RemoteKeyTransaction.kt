package com.chen.beeaudio.repository.dao

import androidx.room.Dao
import androidx.room.Transaction
import com.chen.beeaudio.model.localmodel.Blog
import com.chen.beeaudio.model.localmodel.BlogRemoteKeys

@Dao
interface RemoteKeyTransaction {
    @Transaction
    suspend fun saveDataToDatabase(
        isRefresh: Boolean,
        keys: List<BlogRemoteKeys>,
        blogs: List<Blog>,
        blogDao: BlogDao,
        blogRemoteKeyDao: BlogRemoteKeyDao
    ) {
        if (isRefresh) {
            blogDao.deleteAllBlogs()
            blogRemoteKeyDao.deleteAllRemoteKeys()
        }
        blogRemoteKeyDao.addAllRemoteKeys(remoteKeys = keys)
        blogDao.addBlogs(blogs = blogs)
    }
}