package com.chen.beeaudio.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chen.beeaudio.mock.PlayListCollectionMock1
import com.chen.beeaudio.mock.SubscribeDataCollectionMock
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.model.localmodel.TrackSummary
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.AudioRepository
import com.chen.beeaudio.repository.SubscribeRepository
import com.chen.beeaudio.repository.database.SubscribeDatabase
import com.chen.beeaudio.repository.database.TrackSummaryDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class PlayListViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val repository : AudioRepository,
    private val subscribeRepository: SubscribeRepository,
    private val subscribeDatabase: SubscribeDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /* 当前歌单ID */
    val currentPlayListID : Long = savedStateHandle.get<Long>("playlist_id") ?: PlayListCollectionMock1[0].id
    /* 歌单收藏订阅状态 */
    val isSubscribe : MutableStateFlow<Boolean> = MutableStateFlow(false)
    /* 歌单是否为自建歌单 */
    val isMyCreated : MutableStateFlow<Boolean> = MutableStateFlow(false)

    /* 全部歌曲信息 */
    private val _currentPlayListTracks = MutableStateFlow<PagingData<Track>>(PagingData.empty())
    val currentPlayListTracks = _currentPlayListTracks

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadPlayListTracks()
        }
        viewModelScope.launch(Dispatchers.IO) {
            loadSubscribeStateFromDatabase()
        }
        viewModelScope.launch(Dispatchers.IO) {
            loadMyCreatedStateFromDatabase()
        }
    }

    /* 加载歌单内歌曲条目 - 分页 */
    suspend fun loadPlayListTracks() {
        repository
            .getCurrentPlayListTracks(playListId = currentPlayListID)
            .cachedIn(viewModelScope)
            .collect {
                _currentPlayListTracks.value = it
            }
    }
    /* 请求当前跳转歌单的详细信息 */
    fun currentPlayListDetailFlow() : Flow<PlayList>{
        return flow {
            emit(localApi.getPlayListDetail(playListId = currentPlayListID).data)
        }.flowOn(Dispatchers.IO)
    }

    /* 查询该歌单是否被当前操作用户订阅 */
    private fun loadSubscribeStateFromDatabase() {
        val allMySubscribePlaylist : List<Subscribe> = subscribeDatabase.subscribeDao().getAllPlayListSubscribeData()
        isSubscribe.value = allMySubscribePlaylist.map { it.itemId }.contains(currentPlayListID)
    }

    /* 查询歌单是否为当前操作用户创建 */
    private fun loadMyCreatedStateFromDatabase() {
        val allMyCreatedPlaylist : List<Subscribe> = subscribeDatabase.subscribeDao().getAllMyCreatedPlayList()
        isMyCreated.value = allMyCreatedPlaylist.map { it.itemId }.contains(currentPlayListID)
    }

    /* 变更当前歌单的收藏订阅状态 */
    fun changeSubscribeState(context: Context, currentUserId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isSubscribe.value) {
                /* 取消收藏订阅逻辑 */
                /* 查询原订阅项目数据，用于作自建歌单订阅项目判断 */
                val originalSubscribeData : Subscribe = subscribeDatabase.subscribeDao()
                    .searchSubscribeDataByPrimaryKey(currentPlayListID, 1000)
                if (!originalSubscribeData.isMyCreated) {
                    /* 删除本地数据库中的对应订阅收藏数据对象 */
                    subscribeDatabase.subscribeDao().deleteSingleSubscribeData(originalSubscribeData)
                    /* 更新订阅状态数据 */
                    loadSubscribeStateFromDatabase()
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "自建歌单无法取消收藏订阅哦～", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                /* 加入收藏订阅逻辑 */
                var playlist : PlayList = PlayListCollectionMock1[0]
                /* 获取歌单信息 */
                currentPlayListDetailFlow().collect {
                    playlist = it
                }
                /* 将该歌单的信息转换为订阅数据信息进行处理 */
                subscribeDatabase.subscribeDao().addSingleSubscribeData(playlist.toMySubscribe(false))
                /* 更新订阅状态数据 */
                loadSubscribeStateFromDatabase()
            }
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
}