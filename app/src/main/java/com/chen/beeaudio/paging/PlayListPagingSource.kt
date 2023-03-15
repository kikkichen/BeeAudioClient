package com.chen.beeaudio.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

/** 目标歌单中的曲目 PagingSource
 * @param   playListId  目的歌单ID
 * @param   api     关于歌单内曲目请求的请求接口
 * @param   initialPagingSize   初始化页面大小
 *
 */
class PlayListPagingSource(
    private val playListId : Long,
    private val api : LocalApi,
    private var initialPagingSize : Int
) : PagingSource<Int, Track>() {
    override fun getRefreshKey(state: PagingState<Int, Track>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val currentPlayListSongsResponse : ResponseBody<List<Track>> = api.getPlayListTrackItems(
                playListId = playListId,
                page = targetPage,
                size = pageSize
            )

            val prevKey: Int?
            var nextKey: Int?

            if (targetPage <= 1) {
                prevKey = null
                nextKey = initialPagingSize / pageSize + 1
            } else {
                prevKey = targetPage - 1
                nextKey = targetPage + 1
                try {
                    currentPlayListSongsResponse.data.isNotEmpty()
                } catch (e : NullPointerException) {
                    nextKey = null
                }
            }
            LoadResult.Page(
                data = currentPlayListSongsResponse.data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}