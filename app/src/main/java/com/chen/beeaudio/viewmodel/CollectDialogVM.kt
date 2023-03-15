package com.chen.beeaudio.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.chen.beeaudio.mock.RequestUserDetailMock
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.model.localmodel.TrackSummary
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.TrackSummaryRepository
import com.chen.beeaudio.repository.database.SubscribeDatabase
import com.chen.beeaudio.repository.database.TrackSummaryDatabase
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
class CollectDialogVM @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    @Named("PlayListCover")
    private val imageLoader: ImageLoader,
    private val subscribeDatabase: SubscribeDatabase,
    private val trackSummaryDatabase: TrackSummaryDatabase,
    private val trackSummaryRepository: TrackSummaryRepository,
) : ViewModel() {
    val myImageLoader = imageLoader

    /* 当前用户信息 */
    private val _userDetail = MutableStateFlow(RequestUserDetailMock)
    val userDetail = _userDetail

    /* 用户自建歌单列表 选择类型对象列表 */
    val createdPlayList = MutableStateFlow<List<PlaylistChoose>>(emptyList())

    /** 请求当前操作用户的详细信息
     *  @param  userId  目标请求用户信息的用户ID
     */
    fun currentUserDetailFlow(userId : Long) : Flow<RequestUserDetail> {
        return flow {
            emit(localApi.getUserDetail(userId).data)
        }.flowOn(Dispatchers.IO)
    }

    /** 加载当前操作用户的自建歌单信息 - 订阅自建歌单 、 歌单收容数量
     *  @param  songId  执行目标曲目ID
     */
    fun loadUserCreatedPlaylistInfo(songId : Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val playlistChooseList = mutableListOf<PlaylistChoose>()
            /* 获取自建歌单列表 */
            val myPlaylists = subscribeDatabase.subscribeDao().getAllMyCreatedPlayList()

            for (playlist in myPlaylists) {
                /* 检查目标曲目在自建歌单中的存在情况 */
                val result = trackSummaryDatabase.trackSummaryDao().getTrackExistInPlaylist(songId = songId, playlistId = playlist.itemId)
                val isCollected : Boolean = result > 0
                /* 子项整合 */
                val tempPlaylistChoose = PlaylistChoose(
                    playlist = playlist,
                    tracksAmount = trackSummaryDatabase.trackSummaryDao().getAmountOfPlaylist(playlistId = playlist.itemId),
                    originChoose = isCollected,
                    afterChoose = isCollected
                )
                /* 添加到主列表 */
                playlistChooseList.add(tempPlaylistChoose)
            }
            playlistChooseList.sortByDescending {
                it.tracksAmount
            }
            /* 将有效数据赋值到歌单列表中 */
            createdPlayList.value = playlistChooseList
        }
    }

    /** 变更歌单选择对象的选择信号
     *  @param  playlistChoose  自建歌单选择对象
     */
    fun changeChooseSignal(playlistChoose: PlaylistChoose) {
        val newList : MutableList<PlaylistChoose> = mutableListOf()
        newList.addAll(createdPlayList.value)
        newList.remove(playlistChoose)
        newList.add(playlistChoose.apply {
            setAfterChoose()
        })
        newList.sortByDescending {
            it.tracksAmount
        }
        createdPlayList.value = newList
    }

    /** 执行曲目从歌单的 收藏/取消收藏操作 操作
     *  @param  context 上下文对象
     *  @param  songId  目标曲目ID
     */
    fun collectionTransitionForPlayList(context: Context, songId: Long, currentUserId : Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addCreatedPlaylistIdList : MutableList<Long> = mutableListOf()
                val removeCreatedPlaylistIdList : MutableList<Long> = mutableListOf()
                /* 将需要执行的 PlayList 过滤、执行分类 */
                for (subPlaylist in createdPlayList.value) {
                    if (!subPlaylist.originChoose and subPlaylist.afterChoose) {
                        addCreatedPlaylistIdList.add(subPlaylist.playlist.itemId)
                    } else if (subPlaylist.originChoose and !subPlaylist.afterChoose) {
                        removeCreatedPlaylistIdList.add(subPlaylist.playlist.itemId)
                    }
                }
                /* 向服务器提交执行歌单的名单 */
                val result = localApi.editSongFromMyMultipleCreatedPlaylist(
                    uid = currentUserId,
                    sid = songId,
                    addPlayListIds = addCreatedPlaylistIdList,
                    removePlayListIds = removeCreatedPlaylistIdList,
                ).data
                if (result) {
                    /* 服务端执行顺利完成, 变更本地数据库数据 */
                    addCreatedPlaylistIdList.forEach { playlistId ->
                        trackSummaryDatabase.trackSummaryDao().addSingleTrackSummary(
                            TrackSummary(songId = songId, playlistId = playlistId)
                        )
                    }
                    removeCreatedPlaylistIdList.forEach { playlistId ->
                        trackSummaryDatabase.trackSummaryDao().deleteSingleTrackSummary(
                            TrackSummary(songId = songId, playlistId = playlistId)
                        )
                    }
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "操作完成 ~", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    /* 服务端执行有误 */
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "曲目执行收藏操作失败，请稍后重试 ~", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e : Throwable) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "曲目执行收藏操作失败，请稍后重试 ~", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 判断选择表单数据是否有变更
     *
     */
    fun verifyPlaylistChooseChange() : Boolean {
        createdPlayList.value.forEach { playlistChoose ->
            if (playlistChoose.originChoose != playlistChoose.afterChoose) return true
        }
        return false
    }
}

/* 歌单选择类型， 其中包含歌单订阅数据对象、歌单曲目数量、曲目收录信号、 曲目收录信号（变更后） */
data class PlaylistChoose(
    val playlist : Subscribe,
    val tracksAmount : Int,
    val originChoose : Boolean,
    var afterChoose : Boolean
) {
    fun setAfterChoose() {
        this.afterChoose = !this.afterChoose
    }
}

/* 用户详细页面 - 用户详细信息请求结果状态 */
sealed class NetUserInfoLoadResult<T> {
    object Loading : NetUserInfoLoadResult<RequestUserDetail>()
    object Error : NetUserInfoLoadResult<RequestUserDetail>()
    data class Success(val userDetail: RequestUserDetail) : NetUserInfoLoadResult<RequestUserDetail>()
}