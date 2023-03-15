package com.chen.beeaudio.paging.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.model.net.SearchArtists
import com.chen.beeaudio.net.LocalSearchApi

class SearchArtistPagingSource(
    private val artistsKeywords : String,
    private val api : LocalSearchApi,
    private var initialPagingSize : Int
) : PagingSource<Int, Artist>() {
    override fun getRefreshKey(state: PagingState<Int, Artist>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Artist> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val searchArtistsResponse : ResponseBody<SearchArtists> = api.getSearchArtistsResult(
                keywords = artistsKeywords,
                type = 100,
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
                    searchArtistsResponse.data.artists.isNotEmpty()
                } catch (e : NullPointerException) {
                    nextKey = null
                }
            }
            LoadResult.Page(
                data = searchArtistsResponse.data.artists,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}