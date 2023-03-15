package com.chen.beeaudio.paging.blog

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.blog.Comment
import com.chen.beeaudio.model.blog.Retweeted
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

/** 目标动态博文评论列表 PagingSource
 * @param   blogId  目的博文动态ID
 * @param   api     关于歌单内曲目请求的请求接口
 * @param   initialPagingSize   初始化页面大小
 */
class CommentListPagingSource(
    private val blogId : Long,
    private val api : LocalApi,
    private var initialPagingSize : Int
) : PagingSource<Int, Comment>() {
    override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val commentListResponse : ResponseBody<List<Comment>> = api.getBlogCommentList(
                blogId = blogId,
                page = targetPage,
                size = pageSize
            )


            val prevKey: Int?
            val nextKey: Int?

            if (targetPage <= 1) {
                prevKey = null
                nextKey = initialPagingSize / pageSize + 1
            } else {
                prevKey = targetPage - 1
                nextKey = if (commentListResponse.data.isEmpty()) {
                    null
                } else {
                    targetPage + 1
                }
            }
            LoadResult.Page(
                data = commentListResponse.data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}