package com.chen.beeaudio.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.model.blog.Attitude
import com.chen.beeaudio.model.blog.Comment
import com.chen.beeaudio.model.blog.Retweeted
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.paging.blog.AttitudeListPagingSource
import com.chen.beeaudio.paging.blog.CommentListPagingSource
import com.chen.beeaudio.paging.blog.RetweetedListPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

/** 微博详情页 数据曾
 *  @param  localApi    api网络服务对象
 */
class BlogDetailRepository @Inject constructor(
    @Named("LocalServer") private val localApi: LocalApi
) {
    /** 获取转发列表条目 - 分页
     *  @param  blogId  当前请求博文动态ID
     *  @param  isReport    当前请求博文是否为转发博文
     */
    fun getRetweetedList(blogId : Long, isReport : Boolean) : Flow<PagingData<Retweeted>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                RetweetedListPagingSource(
                    blogId = blogId,
                    isReport = isReport,
                    api = localApi,
                    initialPagingSize = 20
                )
            }
        ).flow
    }

    /** 获取评论列表条目 - 分页
     *  @param  blogId  当前请求博文动态ID
     */
    fun getCommentList(blogId : Long) : Flow<PagingData<Comment>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                CommentListPagingSource(
                    blogId = blogId,
                    api = localApi,
                    initialPagingSize = 20
                )
            }
        ).flow
    }

    /** 获取点赞列表条目 - 分页
     *  @param  blogId  当前请求博文动态ID
     */
    fun getAttitudeList(blogId : Long) : Flow<PagingData<Attitude>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                AttitudeListPagingSource(
                    blogId = blogId,
                    api = localApi,
                    initialPagingSize = 20
                )
            }
        ).flow
    }
}