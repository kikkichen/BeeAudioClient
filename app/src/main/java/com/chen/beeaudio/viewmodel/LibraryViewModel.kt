package com.chen.beeaudio.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.net.getPart
import com.chen.beeaudio.repository.SubscribeRepository
import com.chen.beeaudio.screen.SubscribeFilterChip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val subscribeRepository: SubscribeRepository,
) : ViewModel() {
    /* 关键字搜索订阅数据结果 */
    private val _searchResultList : MutableStateFlow<SubscribeDataState> = MutableStateFlow(SubscribeDataState.None)
    val searchResultList = _searchResultList
    /* 订阅数据查询结果 */
    private val _subscribeDataList : MutableStateFlow<SubscribeDataState> = MutableStateFlow(SubscribeDataState.None)
    val subscribeDataList = _subscribeDataList

    /* 待上传歌单封面图片uri */
    val preUploadAvatarImageUri : MutableStateFlow<String> = MutableStateFlow("")
    /* 封面上传状态 */
    val avatarImageUploadState : MutableStateFlow<AvatarUploadState> = MutableStateFlow(AvatarUploadState.None)

    init {
        loadSubscribeData(SubscribeFilterChip.All)
    }
    /** 加载订阅数据
     *  @param  subscribeSignal 订阅筛选状态
     */
    fun loadSubscribeData(subscribeSignal : SubscribeFilterChip) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchResultList.value = SubscribeDataState.None
            _subscribeDataList.value = SubscribeDataState.None
            when (subscribeSignal) {
                is SubscribeFilterChip.All -> {
                    val result = subscribeRepository.loadAllSubscribeData()
                    if (result.isEmpty()) _subscribeDataList.value = SubscribeDataState.EmptyData
                    else _subscribeDataList.value = SubscribeDataState.Success(result)
                }
                SubscribeFilterChip.Album -> {
                    val result = subscribeRepository.loadAllAlbumSubscribeData()
                    if (result.isEmpty()) _subscribeDataList.value = SubscribeDataState.EmptyData
                    else _subscribeDataList.value = SubscribeDataState.Success(result)
                }
                SubscribeFilterChip.Artist -> {
                    val result = subscribeRepository.loadAllArtistSubscribeData()
                    if (result.isEmpty()) _subscribeDataList.value = SubscribeDataState.EmptyData
                    else _subscribeDataList.value = SubscribeDataState.Success(result)
                }
                SubscribeFilterChip.Clear -> { /* empty */ }
                SubscribeFilterChip.PlayList -> {
                    val result = subscribeRepository.loadAllPlayListSubscribeData()
                    if (result.isEmpty()) _subscribeDataList.value = SubscribeDataState.EmptyData
                    else _subscribeDataList.value = SubscribeDataState.Success(result)
                }
                SubscribeFilterChip.PlayListOfMyCreated -> {
                    val result = subscribeRepository.loadAllMyCreatedPlayList()
                    if (result.isEmpty()) _subscribeDataList.value = SubscribeDataState.EmptyData
                    else _subscribeDataList.value = SubscribeDataState.Success(result)
                }
            }
        }
    }

    /** 通过关键字检索相关订阅项目数据
     *  @param  keyword 关键字字符串字段
     */
    fun loadSubscribeDataByKeyword(keyword : String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchResultList.value = SubscribeDataState.None
            _subscribeDataList.value = SubscribeDataState.None
            val result = subscribeRepository.loadSubscribeDataByKeyword(keyword)
            if (result.isEmpty()) {
                _searchResultList.value = SubscribeDataState.EmptyData
            } else {
                _searchResultList.value = SubscribeDataState.Success(data = result)
            }
        }
    }

    /** 更改目标订阅项目的置顶状态
     *  @param  originalSubscribeData   原订阅数据信息
     *  @param  myFavoritePlaylistId    当前用户默认喜爱曲目收藏歌单ID
     */
    fun changeSubscribeDataTopState(originalSubscribeData : Subscribe, myFavoritePlaylistId : Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (originalSubscribeData.itemId != myFavoritePlaylistId) {
                subscribeRepository.changeSubscribeDataTopState(originalSubscribeData = originalSubscribeData)
            }
        }
    }

    /** 重置搜索状态
     *
     */
    fun replaceSearchResultList() {
        _searchResultList.value = SubscribeDataState.None
    }

    /** 向服务端同步状态
     *  @param  currentUserId   当前用户ID
     *  @param  context         上下文参数
     */
    fun syncSubscribeDataToServer(context : Context, currentUserId : Long) {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeRepository.syncCurrentSubscribeDataToServer(currentUserId) {
                launch(Dispatchers.Main) { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    /** 歌单封面上传的逻辑
     *  @param  finishedEvent   执行结束收尾任务
     *  @param  currentUserId   当前用户ID
     *  @param  context         上下文参数
     */
    fun uploadPlaylistCoverImage(context: Context, currentUserId: Long, finishedEvent: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            avatarImageUploadState.value = AvatarUploadState.Running
            try {
                if (preUploadAvatarImageUri.value.isEmpty()) {
                    launch(Dispatchers.Main) { Toast.makeText(context, "还没有选择作为头像的图片呢～", Toast.LENGTH_SHORT).show() }
                } else {
                    val result = localApi.uploadUserAvatar(
                        uid = currentUserId,
                        upload = getPart(context, "uploadfile", ".jpg", preUploadAvatarImageUri.value.toUri())
                    )
                    if (result.data) {
                        avatarImageUploadState.value = AvatarUploadState.Success
                        launch(Dispatchers.Main) { finishedEvent() }
                    } else {
                        avatarImageUploadState.value = AvatarUploadState.None
                        launch(Dispatchers.Main) { Toast.makeText(context, "服务端发生了错误，请稍后重试", Toast.LENGTH_SHORT).show() }
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                avatarImageUploadState.value = AvatarUploadState.Error(e)
            }
        }
    }
}

sealed class SubscribeDataState() {
    object None : SubscribeDataState()
    object EmptyData : SubscribeDataState()
    class Success(val data : List<Subscribe>) : SubscribeDataState()
}

/* 头像上传任务进行状态 */
sealed class AvatarUploadState() {
    object None : AvatarUploadState()
    object Running : AvatarUploadState()
    object Success : AvatarUploadState()
    class Error(val e : Exception) : AvatarUploadState()
}