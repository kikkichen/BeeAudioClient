package com.chen.beeaudio.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.init.BLOGS_INITIAL_SIZE
import com.chen.beeaudio.init.BLOGS_PER_PAGE_SIZE
import com.chen.beeaudio.model.localmodel.Blog
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.paging.BlogsRemoteMediator
import com.chen.beeaudio.repository.database.BlogsDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

@ExperimentalPagingApi
class BlogRepository @Inject constructor(
    @Named("LocalServer")
    private val localApi : LocalApi,
    private val localDatabase: BlogsDatabase
) {

    fun getAllSubscribeBlogs(currentUserId: Long) : Flow<PagingData<Blog>> {
        val pagingSourceFactory = { localDatabase.blogImageDao().getAllBlogs() }
        return Pager(
            config = PagingConfig(
                pageSize = BLOGS_PER_PAGE_SIZE,
                initialLoadSize = BLOGS_INITIAL_SIZE,
                prefetchDistance = 2,
            ),
            remoteMediator = BlogsRemoteMediator(
                currentUserId = currentUserId,
                localApi = localApi,
                localDatabase = localDatabase,
                initialPagingSize = BLOGS_INITIAL_SIZE
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

}