package com.chen.beeaudio.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.paging.PlayListCollectionPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

class PlayListsRepository @Inject constructor(
    @Named("LocalServer") private val localApi: LocalApi
) {
    /** 获取索引标签指定的歌单条目 - 分页
     *
     */
    fun getTargetPlayLists(cat : String) : Flow<PagingData<PlayList>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                initialLoadSize = 50,
                prefetchDistance = 5,
            ),
            pagingSourceFactory = {
                PlayListCollectionPagingSource(
                    cat = cat,
                    api = localApi,
                    initialPagingSize = 50
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }
}