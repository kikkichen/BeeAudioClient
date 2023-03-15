package com.chen.beeaudio.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.mock.EmptyPlayListData
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.net.getPart
import com.chen.beeaudio.repository.SubscribeRepository
import com.chen.beeaudio.repository.TrackSummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class EditPlayListViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val trackSummaryRepository: TrackSummaryRepository,
    private val subscribeRepository: SubscribeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /* 当前操作页目标对象的ID ， 若ID为 0 表示当前为“ 创建歌单 ”的情况， 若ID大于0 则表示当前为编辑歌单的情况 */
    val playlistId = savedStateHandle.get<Long>("playlistId") ?: 0

    /* 当前歌单模型数据的编辑对象 */
    private val _playlistState : MutableStateFlow<PlayListDetailLoadState> = MutableStateFlow(PlayListDetailLoadState.Loading)
    val playlistState = _playlistState

    /* 编辑内容 */
    /* 歌单标题 */
    val playlistTitle : MutableStateFlow<String> = MutableStateFlow("")
    /* 歌单标签 */
    val playListTags : MutableStateFlow<String> = MutableStateFlow("")
    /* 歌单可见性 */
    val playListPublic : MutableStateFlow<Boolean> = MutableStateFlow(true)
    /* 歌单描述 */
    val playlistDescription : MutableStateFlow<String> = MutableStateFlow("")

    /* 待上传歌单封面图片uri */
    val image : MutableStateFlow<String> = MutableStateFlow("")
    /* 封面上传状态 */
    val coverImageUploadState : MutableStateFlow<CoverUploadState> = MutableStateFlow(CoverUploadState.None)

    init {
        if (playlistId != 0.toLong()) {
            loadPlaylistBaseInfo()
        } else run {
            _playlistState.value = PlayListDetailLoadState.Success(playList = EmptyPlayListData)
        }
    }

    /* 加载当前歌单信息 */
    fun loadPlaylistBaseInfo() {
        viewModelScope.launch {
            try {
                _playlistState.value = PlayListDetailLoadState.Loading
                if (playlistId != 0.toLong()) {
                    val result = localApi.getPlayListDetail(playListId = playlistId).data
                    _playlistState.value = PlayListDetailLoadState.Success(result)
                    (_playlistState.value as PlayListDetailLoadState.Success).playList.apply {
                        playlistTitle.value = this.name
                        playListTags.value = this.tags.toString()
                        playListPublic.value = true
                        playlistDescription.value = this.description
                    }
                }
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }

    /** 变更标题
     *  @param  words   新字符串
     */
    fun changePlaylistTitle(words: String) {
        playlistTitle.value = words
    }

    /** 变更标签
     *  @param  words   新字符串
     */
    fun changePlayListTags(words: String) {
        playListTags.value = words
    }

    /** 变更可见性
     *  @param  state   新的状态
     */
    fun changePlayListPublic(state : Boolean) {
//        playListPublic.value = state
        playListPublic.value = true     // beta - 仅保持公开性
    }

    /** 变更歌单描述性文字
     *  @param  description 描述字符串
     */
    fun changeDescription(description : String) {
        playlistDescription.value = description
    }

    /** 保存并提交表单到相应的业务逻辑
     *  @param  context 上下文参数
     *  @param  currentUserId   当前执行用户ID
     */
    fun saveEditForm(context: Context, currentUserId : Long, finishedEvent: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (playlistId == 0.toLong()) {
                /* 创建新的歌单逻辑 */
                try {
                    if (verityTextIsNotEmpty()) {
                        /* 向服务端发送新歌单创建表单 */
                        val response = localApi.createNewMyPlaylist(
                            uid = currentUserId,
                            name = playlistTitle.value,
                            description = playlistDescription.value,
                            tags = "[" + playListTags.value + "]",
                            public = playListPublic.value
                        )
                        if (response.ok == 1) {
                            /* 将订阅数据存储到数据库, 并完成订阅信息与服务端的同步 */
                            subscribeRepository.subscribeTargetPlaylist(
                                playlistId = response.data,
                                currentUserId = currentUserId,
                                toastEvent = { launch(Dispatchers.Main) { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
                            )
                            finishedEvent()
                        } else {
                            launch(Dispatchers.IO) { launch(Dispatchers.Main) { Toast.makeText(context, "服务端响应有误，请稍后重试", Toast.LENGTH_SHORT).show() } }
                        }
                    } else {
                        launch(Dispatchers.Main) { Toast.makeText(context, "请检查表单项目是否有遗漏", Toast.LENGTH_SHORT).show() }
                    }
                } catch (e : Throwable) {
                    e.printStackTrace()
                    launch(Dispatchers.Main) { Toast.makeText(context, "未知异常 (1)", Toast.LENGTH_SHORT).show() }
                }
            } else {
                /* 更改歌单信息逻辑 */
                try {
                    if (verityTextIsChanged() && verityTextIsNotEmpty()) {
                        /* 向服务端提交歌单信息更改表单 */
                        val response = localApi.editorMyCreatedPlayListInfo(
                            uid = currentUserId,
                            pid = playlistId,
                            name = playlistTitle.value,
                            description = playlistDescription.value,
                            tags = "[" + playListTags.value + "]",
                            public = playListPublic.value
                        )
                        if (response.ok == 1) {
                            /* 获取从服务端传回的新的歌单信息 */
                            val newPlayListDetail = response.data
                            /* 获取原有数据库中的原始数据 */
                            val originalSubscribeData = subscribeRepository.getSubscribeData(newPlayListDetail.id, 1000)
                            /* 保存数据到数据库 */
                            subscribeRepository.changeSubscribeDataItem(
                                newPlayListDetail.toMySubscribe(true).copy(
                                    isTop = originalSubscribeData.isTop,
                                    weight = originalSubscribeData.weight
                                )
                            )
                            /* 与服务端进行同步 */
                            subscribeRepository.syncCurrentSubscribeDataToServer(
                                currentUserId = currentUserId,
                                toastEvent = { launch(Dispatchers.Main) { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
                            )
                            finishedEvent()
                        } else {
                            launch(Dispatchers.Main) { Toast.makeText(context, "服务端响应有误，请稍后重试", Toast.LENGTH_SHORT).show()}
                        }
                    } else {
                        launch(Dispatchers.Main) { Toast.makeText(context, "您还没有变更任何数据~", Toast.LENGTH_SHORT).show()}
                    }

                } catch (e : Throwable) {
                    e.printStackTrace()
                    launch(Dispatchers.Main) { Toast.makeText(context, "未知错误 (2)", Toast.LENGTH_SHORT).show()}
                }
            }
        }
    }

    /** 删除我当前的自建歌单
     *  @param  currentUserId   当前执行用户ID
     */
    fun deleteMyCreatedPlaylist(context: Context, currentUserId: Long, finishedEvent: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                /* 向服务端发送删除当前歌单的请求 */
                val result = localApi.deleteMyPlaylist(uid = currentUserId, pid = playlistId)
                if (result.ok == 1 && result.data) {
                    /* 服务端执行目标歌单删除成功， 在本地数据库删除所有对饮的订阅信息以及曲目概括信息 */
                    trackSummaryRepository.deleteTrackSummaryOfTargetPlaylist(playlistId = playlistId)
                    subscribeRepository.deleteSubscribeDataByPrimaryKey(itemId = playlistId, itemType = 1000)
                    /* 向服务端同步数据 */
                    subscribeRepository.syncCurrentSubscribeDataToServer(
                        currentUserId = currentUserId,
                        toastEvent = { launch(Dispatchers.Main) { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
                    )
                    finishedEvent()
                } else {
                    launch(Dispatchers.Main) { Toast.makeText(context, "服务端响应有误，请稍后重试", Toast.LENGTH_SHORT).show()}
                }
            } catch (e : Throwable) {
                e.printStackTrace()
                launch(Dispatchers.Main) { Toast.makeText(context, "未知错误 (2)", Toast.LENGTH_SHORT).show()}
            }
        }
    }

    /** 歌单封面上传的逻辑
     *  @param  finishedEvent   执行结束收尾任务
     */
    fun uploadPlaylistCoverImage(context: Context, currentUserId: Long, finishedEvent: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            coverImageUploadState.value = CoverUploadState.Running
            try {
                if (image.value.isEmpty()) {
                    launch(Dispatchers.Main) { Toast.makeText(context, "还没有选择作为封面的图片呢～", Toast.LENGTH_SHORT).show() }
                } else {
                    val result = localApi.uploadPlaylistCover(
                        uid = currentUserId,
                        pid = playlistId,
                        upload = getPart(context, "uploadfile", ".jpg", image.value.toUri())
                    )
                    if (result.data) {
                        coverImageUploadState.value = CoverUploadState.Success
                        launch(Dispatchers.Main) { finishedEvent() }
                    } else {
                        coverImageUploadState.value = CoverUploadState.None
                        launch(Dispatchers.Main) { Toast.makeText(context, "服务端发生了错误，请稍后重试", Toast.LENGTH_SHORT).show() }
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                coverImageUploadState.value = CoverUploadState.Error(e)
            }
        }
    }


    /** 验证输入关键字段不为空
     *
     */
    private fun verityTextIsNotEmpty() : Boolean {
        return !(playlistTitle.value.isEmpty() || playlistDescription.value.isEmpty())
    }

    /** 验证输入关键字段有所更改
     *
     */
    private fun verityTextIsChanged() : Boolean {
        val originalDetail = (playlistState.value as PlayListDetailLoadState.Success).playList
        originalDetail.apply {
            return this.name != playlistTitle.value || this.tags.toString() != playListTags.value || this.description != playlistDescription.value
        }
    }
}

/* 歌单信息加载状态 */
sealed class PlayListDetailLoadState() {
    object Loading : PlayListDetailLoadState()      // 加载中
    class Success(val playList : PlayList) : PlayListDetailLoadState()  // 加载完成
}

/* 歌单封面上传任务进行状态 */
sealed class CoverUploadState() {
    object None : CoverUploadState()
    object Running : CoverUploadState()
    object Success : CoverUploadState()
    class Error(val e : Exception) : CoverUploadState()
}
