package com.chen.beeaudio.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import coil.ImageLoader
import com.chen.beeaudio.mock.SingleAlbum
import com.chen.beeaudio.mock.SingleArtistMock
import com.chen.beeaudio.mock.SingleTrackMock
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.ArtistRepository
import com.chen.beeaudio.repository.SubscribeRepository
import com.chen.beeaudio.repository.database.SubscribeDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ArtistViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    @Named("PlayListCover")
    private val imageLoader: ImageLoader,
    private val repository: ArtistRepository,
    private val subscribeRepository: SubscribeRepository,
    private val subscribeDatabase: SubscribeDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val currentArtistId : Long = savedStateHandle.get<Long>("artist_id") ?: SingleArtistMock.id

    val myImageLoader = imageLoader
    /* 当前艺人曲目作品列表 - Paging*/
    private val _currentArtistTracks = MutableStateFlow<PagingData<Track>>(PagingData.empty())
    val currentArtistTracks = _currentArtistTracks

    /* 当前艺人专辑作品列表 */
    private val _currentArtistAlbums = MutableStateFlow<PagingData<Album>>(PagingData.empty())
    val currentArtistAlbums = _currentArtistAlbums

    /* 歌单收藏订阅状态 */
    val isSubscribe : MutableStateFlow<Boolean> = MutableStateFlow(false)

    /* 曲目作品与专辑信息的加载要位于  currentArtistId 的初始化之后 */
    init {
        viewModelScope.launch {
            loadArtistTracks()
        }
        viewModelScope.launch {
            loadArtistAlbums()
        }
        viewModelScope.launch(Dispatchers.IO) {
            loadSubscribeStateFromDatabase()
        }
    }

    /* 查询是否被该歌单是否被订阅 */
    private fun loadSubscribeStateFromDatabase() {
        val allMySubscribeArtist : List<Subscribe> = subscribeDatabase.subscribeDao().getAllArtistSubscribeData()
        isSubscribe.value = allMySubscribeArtist.map { it.itemId }.contains(currentArtistId)
    }

    /** 加载艺人曲目作品 - 分页
     *
     */
    private suspend fun loadArtistTracks() {
        repository
            .getArtistResultTracks(artistId = currentArtistId)
            .cachedIn(viewModelScope)
            .collect {
                _currentArtistTracks.value = it
            }
    }

    /** 加载艺人专辑作品 - 分页
     *
     */
    private suspend fun loadArtistAlbums() {
        repository
            .getArtistResultAlbums(artistId = currentArtistId)
            .cachedIn(viewModelScope)
            .collect {
                _currentArtistAlbums.value = it
            }
    }

    /* 当前艺人信息 */
    fun currentArtistDetailFlow() : Flow<Artist> {
        return flow {
            emit(localApi.getArtistDetail(artistId = currentArtistId).data)
        }.flowOn(Dispatchers.IO)
    }

    /* 变更当前艺人的收藏订阅状态 */
    fun changeSubscribeState(context: Context, currentUserId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isSubscribe.value) {
                /* 取消订阅逻辑 */
                val originalSubscribeData : Subscribe = subscribeDatabase.subscribeDao()
                    .searchSubscribeDataByPrimaryKey(currentArtistId, 100)
                if (originalSubscribeData.itemId != 0.toLong()) {
                    /* 删除本地数据库中的对应订阅收藏数据对象 */
                    subscribeDatabase.subscribeDao().deleteSingleSubscribeData(originalSubscribeData)
                } else {
                    Toast.makeText(context, "该艺人未进入我的订阅", Toast.LENGTH_SHORT).show()
                }
            } else {
                /* 加入订阅逻辑 */
                var artist = SingleArtistMock
                currentArtistDetailFlow().collect {
                    artist = it
                }
                subscribeDatabase.subscribeDao().addSingleSubscribeData(artist.toMySubscribe())
            }
            /* 更新订阅状态数据 */
            loadSubscribeStateFromDatabase()
            /* 向服务器同步我的收藏订阅数据 */
            subscribeRepository.syncCurrentSubscribeDataToServer(
                currentUserId = currentUserId,
                toastEvent = { alertMessage ->
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, alertMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    /* 请求播放曲目信息 */
    fun loadSelectedTrackDetail(context: Context, track: Track, finishedEvent: (Track) -> Unit) {
        viewModelScope.launch {
            try {
                val result = localApi.getTrackDetail(trackIds = track.id.toString())
                if (result.ok == 1 && result.data[0].id == track.id) {
                    finishedEvent(result.data[0])
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "服务器忙，请稍后重试", Toast.LENGTH_SHORT).show()
                    }
                    throw CancellationException()
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "曲目信息请求，请稍后重试", Toast.LENGTH_SHORT).show()
                }
                throw CancellationException()
            }
        }
    }
}