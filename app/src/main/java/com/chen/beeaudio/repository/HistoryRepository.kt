package com.chen.beeaudio.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.model.history.HistoryData
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.paging.HistoryDataPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

class HistoryRepository @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi
) {
    /** 获取历史播放记录 - 分页
     *  @param  currentUserId   当前请求历史播放记录的用户ID
     */
    fun getPlayHistoryData(currentUserId : Long) : Flow<PagingData<HistoryData>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 4,
            ),
            pagingSourceFactory = {
                HistoryDataPagingSource(
                    currentUserId = currentUserId,
                    api = localApi,
                    initialPagingSize = 20
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }
}