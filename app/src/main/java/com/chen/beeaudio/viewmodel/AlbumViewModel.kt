package com.chen.beeaudio.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.mock.SingleAlbum
import com.chen.beeaudio.mock.SingleArtistMock
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.AlbumDetail
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.SubscribeRepository
import com.chen.beeaudio.repository.database.SubscribeDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AlbumViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val subscribeRepository: SubscribeRepository,
    private val subscribeDatabase: SubscribeDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /* 当期专辑ID */
    private val currentAlbumID : Long = savedStateHandle.get<Long>("album_id") ?: SingleArtistMock.id
    /* 专辑收藏订阅状态 */
    val isSubscribe : MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadSubscribeStateFromDatabase()
        }
    }

    /* 查询该专辑是否被订阅 */
    private fun loadSubscribeStateFromDatabase() {
        val allMySubscribeAlbum : List<Subscribe> = subscribeDatabase.subscribeDao().getAllAlbumSubscribeData()
        isSubscribe.value = allMySubscribeAlbum.map { it.itemId }.contains(currentAlbumID)
    }

    /* 请求当前专辑页相关的专辑详细信息 */
    fun currentAlbumDetailFlow() : Flow<AlbumDetail> {
        return flow {
            emit(localApi.getAlbumDetail(albumId = currentAlbumID).data)
        }.flowOn(Dispatchers.IO)
    }

    /* 变更当前专辑的收藏状态 */
    fun changeSubscribeState(context: Context, currentUserId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isSubscribe.value) {
                /* 取消订阅逻辑 */
                val originalSubscribeData : Subscribe = subscribeDatabase.subscribeDao()
                    .searchSubscribeDataByPrimaryKey(currentAlbumID, 10)
                if (originalSubscribeData.itemId != 0.toLong()) {
                    /* 删除本地数据库中的对应订阅收藏数据对象 */
                    subscribeDatabase.subscribeDao().deleteSingleSubscribeData(originalSubscribeData)
                } else {
                    Toast.makeText(context, "该专辑未进入我的订阅", Toast.LENGTH_SHORT).show()
                }
            } else {
                /* 加入订阅逻辑 */
                var album = AlbumDetail(resourceState = true, songs = emptyList(), album = SingleAlbum, code = 200)
                /* 获取专辑信息 */
                currentAlbumDetailFlow().collect {
                    album = it
                }
                /* 将当前专辑的信息转换为订阅数据信息进行处理 */
                subscribeDatabase.subscribeDao().addSingleSubscribeData(album.album.toMySubscribe())
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
}