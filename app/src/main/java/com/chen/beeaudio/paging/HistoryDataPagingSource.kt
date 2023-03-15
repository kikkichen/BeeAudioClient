package com.chen.beeaudio.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chen.beeaudio.model.history.HistoryData
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi

/** 当前用户的历史记录 PagingSource
 *  @param  currentUserId   请求历史播放记录的用户ID
 * @param   api     关于歌单内曲目请求的请求接口
 * @param   initialPagingSize   初始化页面大小
 */
class HistoryDataPagingSource(
    private val currentUserId: Long,
    private val api : LocalApi,
    private var initialPagingSize : Int
) : PagingSource<Int, HistoryData>() {
    override fun getRefreshKey(state: PagingState<Int, HistoryData>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryData> {
        return try {
            val targetPage = params.key ?: 1
            val pageSize = params.loadSize
            val historyResponse: ResponseBody<List<HistoryData>> = api.browserMyHistoryData(
                uid = currentUserId,
                page = targetPage,
                size = pageSize
            )

            LoadResult.Page(
                data = historyResponse.data,
                prevKey = if (targetPage == 1) null else targetPage.minus(1),
                nextKey = if (historyResponse.data.isEmpty()) null else targetPage.plus(1),
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}