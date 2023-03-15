package com.chen.beeaudio.paging.blog

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.model.blog.Retweeted
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

/** 目标用户历史博文动态列表 PagingSource
 *  @param  userId  目标用户ID
 *  @param  isOriginal    查询博文是否为转发博文动态
 *  @param  api     api请求服务对象
 *  @param  initialPagingSize   初始化页面大小
 */
class UserBlogListPagingSource(
    private val userId : Long,
    private val isOriginal : Boolean,
    private val api : LocalApi,
    private var initialPagingSize : Int
) : PagingSource<Int, RequestBlog>() {
    override fun getRefreshKey(state: PagingState<Int, RequestBlog>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RequestBlog> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val requestBlogListResponse: ResponseBody<List<RequestBlog>> = api.getTargetUserBlogs(
                targetUserId = userId,
                page = targetPage,
                size = pageSize,
                isOriginal = isOriginal
            )

            val prevKey: Int?
            val nextKey: Int?

            if (targetPage <= 1) {
                prevKey = null
                nextKey = initialPagingSize / pageSize + 1
            } else {
                prevKey = targetPage - 1
                nextKey = try {
                    if (requestBlogListResponse.data.isEmpty()) {
                        null
                    } else {
                        targetPage + 1
                    }
                } catch (e : NullPointerException) {
                    null
                }
            }
            LoadResult.Page(
                data = requestBlogListResponse.data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}