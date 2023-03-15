package com.chen.beeaudio.paging.search

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.model.net.SearchPlayLists
import com.chen.beeaudio.net.LocalSearchApi

class SearchPlayListsPagingSource(
    private val playlistsKeywords : String,
    private val api : LocalSearchApi,
    private var initialPagingSize : Int
) : PagingSource<Int, PlayList>() {
    override fun getRefreshKey(state: PagingState<Int, PlayList>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlayList> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val searchPlayListsResponse : ResponseBody<SearchPlayLists> = api.getSearchPlayListsResult(
                keywords = playlistsKeywords,
                type = 1000,
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
                    searchPlayListsResponse.data.playlists.isNotEmpty()
                } catch (e : NullPointerException) {
                    nextKey = null
                }
            }
            LoadResult.Page(
                data = searchPlayListsResponse.data.playlists,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}