package com.chen.beeaudio.paging.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.model.net.SearchSongs
import com.chen.beeaudio.net.LocalSearchApi

class SearchSongsPagingSource(
    private val songsKeywords : String,
    private val api : LocalSearchApi,
    private var initialPagingSize : Int
) : PagingSource<Int, Track>() {
    override fun getRefreshKey(state: PagingState<Int, Track>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val searchSongsResponse: ResponseBody<SearchSongs> = api.getSearchSongsResult(
                keywords = songsKeywords,
                type = 1,
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
                    searchSongsResponse.data.songs.isNotEmpty()
                } catch (e: NullPointerException) {
                    nextKey = null
                }
            }
            LoadResult.Page(
                data = searchSongsResponse.data.songs,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}