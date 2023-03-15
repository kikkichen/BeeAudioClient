package com.chen.beeaudio.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.paging.blog.UserBlogListPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

class UserDetailRepository @Inject constructor(
    @Named("LocalServer") private val localApi : LocalApi
) {
    /** 获取用户全部历史博文动态
     *  @param  userId  目标查询用户ID
     */
    fun getAllUserBlogList(userId : Long) : Flow<PagingData<RequestBlog>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                UserBlogListPagingSource(
                    userId = userId,
                    isOriginal = false,
                    api = localApi,
                    initialPagingSize = 20
                )
            }
        ).flow
    }

    /** 获取用户原创博文动态
     *  @param  userId  目标查询用户ID
     */
    fun getOriginalUserBlogList(userId : Long) : Flow<PagingData<RequestBlog>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                UserBlogListPagingSource(
                    userId = userId,
                    isOriginal = true,
                    api = localApi,
                    initialPagingSize = 20
                )
            }
        ).flow
    }
}