package com.chen.beeaudio.paging.blog

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.blog.Retweeted
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

/** 目标动态博文转发列表 PagingSource
 *  @param  blogId  目标博文ID
 *  @param  isReport    查询博文是否为转发博文动态
 *  @param  api     api请求服务对象
 *  @param  initialPagingSize   初始化页面大小
 */
class RetweetedListPagingSource(
    private val blogId : Long,
    private val isReport : Boolean,
    private val api : LocalApi,
    private var initialPagingSize : Int
) : PagingSource<Int, Retweeted>() {
    override fun getRefreshKey(state: PagingState<Int, Retweeted>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Retweeted> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val retweetedListResponse : ResponseBody<List<Retweeted>> = api.getBlogRetweetedList(
                blogId = blogId,
                page = targetPage,
                size = pageSize,
                isReport = isReport
            )

            val prevKey: Int?
            val nextKey: Int?

            if (targetPage <= 1) {
                prevKey = null
                nextKey = initialPagingSize / pageSize + 1
            } else {
                prevKey = targetPage - 1
                nextKey = if (retweetedListResponse.data.isEmpty()) {
                    null
                } else {
                    targetPage + 1
                }
            }
            LoadResult.Page(
                data = retweetedListResponse.data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}