package com.chen.beeaudio.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.paging.PlayListPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

class AudioRepository @Inject constructor(
    @Named("LocalServer") private val localApi : LocalApi
) {
    /** 获取歌单歌曲条目 - 分页
     *  @param  playListId  目标查询歌单的ID
     */
    fun getCurrentPlayListTracks(playListId : Long) : Flow<PagingData<Track>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 10,
            ),
            pagingSourceFactory = {
                PlayListPagingSource(
                    playListId = playListId,
                    api = localApi,
                    initialPagingSize = 20
                )
            }
        ).flow
    }
}