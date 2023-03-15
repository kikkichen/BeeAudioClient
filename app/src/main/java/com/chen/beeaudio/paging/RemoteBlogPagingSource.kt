package com.chen.beeaudio.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.net.LocalApi

class RemoteBlogPagingSource(
    private val currentUserid : Long,
    private val api : LocalApi,
    private var initialPagingSize : Int
) : PagingSource<Int, RequestBlog>() {
    override fun getRefreshKey(state: PagingState<Int, RequestBlog>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RequestBlog> {
        val targetPage = params.key ?: 1
        val pageSize = params.loadSize
        val repoStatues = api.getMyFollowBlogsV2(
            userId = currentUserid,
            size = pageSize,
            page = targetPage
        )

        val prevKey: Int?
        val nextKey: Int?

        if (targetPage == 1) {
            prevKey = null
            nextKey = initialPagingSize / pageSize + 1
        } else {
            prevKey = targetPage - 1
            nextKey = if (repoStatues.data.isNotEmpty()) targetPage + 1 else null
        }

        return try {
            LoadResult.Page(
                data = repoStatues.data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}