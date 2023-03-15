package com.chen.beeaudio.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

/** 目标艺人中的曲目作品 PagingSource
 * @param   artistId  目的艺人ID
 * @param   api     关于歌单内曲目请求的请求接口
 * @param   initialPagingSize   初始化页面大小
 *
 */
class TracksOfArtistPagingSource(
    private val artistId: Long,
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
            val artistTracksResponse: ResponseBody<List<Track>> = api.getCurrentArtistTracks(
                artistId = artistId,
                page = targetPage,
                size = pageSize
            )

            LoadResult.Page(
                data = artistTracksResponse.data,
                prevKey = if (targetPage == 1) null else targetPage.minus(1),
                nextKey = if (artistTracksResponse.data.isEmpty()) null else targetPage.plus(1),
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}