package com.chen.beeaudio.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

class PlayListCollectionPagingSource(
    private val cat : String,
    private val api : LocalApi,
    private var initialPagingSize : Int
) : PagingSource<Int, PlayList>() {
    override fun getRefreshKey(state: PagingState<Int, PlayList>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlayList> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val currentPlayListSongsResponse : ResponseBody<List<PlayList>> = api.getTagPlayListCollection(
                cat = cat,
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
                if (currentPlayListSongsResponse.data.isEmpty()) {
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