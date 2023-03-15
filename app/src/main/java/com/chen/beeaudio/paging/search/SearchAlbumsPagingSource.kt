package com.chen.beeaudio.paging.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.model.net.SearchAlbums
import com.chen.beeaudio.net.LocalSearchApi

class SearchAlbumsPagingSource(
    private val albumsKeywords : String,
    private val api : LocalSearchApi,
    private var initialPagingSize : Int
) : PagingSource<Int, Album>() {
    override fun getRefreshKey(state: PagingState<Int, Album>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Album> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val searchAlbumsResponse : ResponseBody<SearchAlbums> = api.getSearchAlbumsResult(
                keywords = albumsKeywords,
                type = 10,
                page = targetPage
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
                    searchAlbumsResponse.data.albums.isNotEmpty()
                } catch (e : NullPointerException) {
                    nextKey = null
                }
            }
            LoadResult.Page(
                data = searchAlbumsResponse.data.albums,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}