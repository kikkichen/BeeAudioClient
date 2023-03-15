package com.chen.beeaudio.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

/** 艺人详情页 专辑作品PagingSource
 *  @param  artistId    目标艺人ID
 *  @param  api         api网络请求接口
 *  @param  initialPagingSize   初始化请求单页容量
 *
 */
class AlbumsOfArtistPagingSource(
    private val artistId : Long,
    private val api : LocalApi,
    private var initialPagingSize : Int
) :PagingSource<Int, Album>() {
    override fun getRefreshKey(state: PagingState<Int, Album>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Album> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val artistAlbumsResponse : ResponseBody<List<Album>> = api.getCurrentArtistAlbums(
                artistId = artistId,
                page = targetPage,
                size = pageSize
            )

            LoadResult.Page(
                data = artistAlbumsResponse.data,
                prevKey = if (targetPage == 1) null else targetPage.minus(1),
                nextKey = if (artistAlbumsResponse.data.isEmpty()) null else targetPage.plus(1),
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}