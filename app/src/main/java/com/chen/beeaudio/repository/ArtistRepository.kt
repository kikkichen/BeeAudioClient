package com.chen.beeaudio.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.paging.AlbumsOfArtistPagingSource
import com.chen.beeaudio.paging.TracksOfArtistPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

class ArtistRepository @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi
){
    /** 获取目的艺人单曲作品结果 - 分页
     *  @param  artistId    目的艺人ID
     */
    fun getArtistResultTracks(artistId : Long) : Flow<PagingData<Track>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 5,
            ),
            pagingSourceFactory = {
                TracksOfArtistPagingSource(
                    artistId = artistId,
                    api = localApi,
                    initialPagingSize = 30
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }

    /** 获取目的艺人专辑作品结果 - 分页
     *  @param  artistId    目的艺人ID
     */
    fun getArtistResultAlbums(artistId : Long) : Flow<PagingData<Album>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
            ),
            pagingSourceFactory = {
                AlbumsOfArtistPagingSource(
                    artistId = artistId,
                    api = localApi,
                    initialPagingSize = 30
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }
}