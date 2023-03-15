package com.chen.beeaudio.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.net.LocalSearchApi
import com.chen.beeaudio.paging.search.SearchAlbumsPagingSource
import com.chen.beeaudio.paging.search.SearchArtistPagingSource
import com.chen.beeaudio.paging.search.SearchPlayListsPagingSource
import com.chen.beeaudio.paging.search.SearchSongsPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

class AudioSearchRepository @Inject constructor (
    @Named("LocalSearchServer")
    private val localSearchApi: LocalSearchApi
) {
    /** 获取单曲搜索结果 - 分页
     *  @param  keywords    搜索关键字
     */
    fun getSearchResultSongs(keywords : String) : Flow<PagingData<Track>>{
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 5,
            ),
            pagingSourceFactory = {
                SearchSongsPagingSource(
                    songsKeywords = keywords,
                    api = localSearchApi,
                    initialPagingSize = 30
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }

    /** 获取专辑搜索结果
     *  @param  keywords    搜索关键字
     */
    fun getSearchResultAlbums(keywords : String) : Flow<PagingData<Album>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 5,
            ),
            pagingSourceFactory = {
                SearchAlbumsPagingSource(
                    albumsKeywords = keywords,
                    api = localSearchApi,
                    initialPagingSize = 30
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }

    /** 获取艺人搜索结果
     *  @param  keywords    搜索关键字
     */
    fun getSearchResultArtists(keywords : String) : Flow<PagingData<Artist>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 5,
            ),
            pagingSourceFactory = {
                SearchArtistPagingSource(
                    artistsKeywords = keywords,
                    api = localSearchApi,
                    initialPagingSize = 30
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }

    /** 获取歌单搜索结果
     *  @param  keywords    搜索关键字
     */
    fun getSearchResultPlayLists(keywords : String) : Flow<PagingData<PlayList>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 5,
            ),
            pagingSourceFactory = {
                SearchPlayListsPagingSource(
                    playlistsKeywords = keywords,
                    api = localSearchApi,
                    initialPagingSize = 30
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }
}