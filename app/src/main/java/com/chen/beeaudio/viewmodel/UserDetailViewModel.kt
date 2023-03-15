package com.chen.beeaudio.viewmodel

import android.util.Log
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import coil.ImageLoader
import com.chen.beeaudio.mock.PremiumMock
import com.chen.beeaudio.mock.RequestUserDetailMock
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Premium
import com.chen.beeaudio.model.blog.RequestBlog
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.model.blog.SimpleUserCount
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.UserDetailRepository
import com.chen.beeaudio.screen.NetUserCreatedPlaylistResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    @Named("PlayListCover")
    private val imageLoader: ImageLoader,
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val userDetailRepository: UserDetailRepository,
) : ViewModel() {
    val myImageLoader = imageLoader
    /* 用户粉丝、关注数量 */
    val requestUserCount = MutableStateFlow(SimpleUserCount(0,0,0))
    /* 用户是否为Premium会员表示 */
    val isPremiumTag = MutableStateFlow(PremiumMock)
    /* 我与当前用户的关系 */
    val weRelative = MutableStateFlow(0)
    /* 用户博文 - Paging3 分页 */
    private val _blogs = MutableStateFlow<PagingData<RequestBlog>>(PagingData.empty())
    val blogs = _blogs

    /* 用户原创博文 - Paging3 分页 */
    private val _originalBlogs = MutableStateFlow<PagingData<RequestBlog>>(PagingData.empty())
    val originalBlogs = _originalBlogs

    /* 用户自建歌单 - 公开 */
    private val _playlists = MutableStateFlow<NetUserCreatedPlaylistResult<List<PlayList>>>(NetUserCreatedPlaylistResult.Loading)
    val playlists = _playlists

    /* 加载用户粉丝、关注数量 */
    fun loadUserCount(userid: Long) {
        viewModelScope.launch {
            try {
                requestUserCount.value = localApi.getCountUser(userId = userid).data
            } catch (e : Throwable) {
                /* 出现错误时使用空数据 */
                requestUserCount.value = SimpleUserCount(0,0,0)
            }
        }
    }

    /* 加载用户Premium会员状态 */
    fun loadIsPremiumTag(userId : Long) {
        viewModelScope.launch {
            try {
                isPremiumTag.value = localApi.getIsPremium(userId = userId).data
            } catch (e : Throwable) {
                /* 出现错误时使用空数据 */
                isPremiumTag.value = Premium("", 0, "", "-", 0)
            }
        }
    }

    /* 加载我与当前用户的关系 */
    fun loadRelative(myId : Long, targetId: Long) {
        viewModelScope.launch {
            try {
                weRelative.value = localApi.getTargetUserRelative(myId, targetId).data
            } catch (e : Throwable) {
                /* 出现错误时使用默认数据 */
                weRelative.value = 0
            }
        }
    }

    /* 分页加载用户博文 */
    fun loadUserBlogs(userId : Long) {
        viewModelScope.launch {
            userDetailRepository
                .getAllUserBlogList(userId)
                .cachedIn(viewModelScope)
                .collect {
                    _blogs.value = it
                }
        }
    }

    /* 分页加载用户原创博文 */
    fun loadUserOriginalBlogs(userId : Long) {
        viewModelScope.launch {
            userDetailRepository
                .getOriginalUserBlogList(userId)
                .cachedIn(viewModelScope)
                .collect {
                    _originalBlogs.value = it
                }
        }
    }

    /* 加载用户博文 */
    fun loadUserCreatedPlaylist(userId : Long) {
        viewModelScope.launch {
            playlists.value = NetUserCreatedPlaylistResult.Loading
            try {
                val response = localApi.accessUserCreatedPlaylistCollection(userId)
                if (response.ok == 1) {
                    playlists.value = NetUserCreatedPlaylistResult.Success(response.data)
                } else {
                    playlists.value = NetUserCreatedPlaylistResult.Error
                }
            } catch (e : Throwable) {
                e.printStackTrace()
                playlists.value = NetUserCreatedPlaylistResult.Error
            }
        }
    }

    /* 执行关注、取消关注事务 */
    fun dealWithFollowAction(myId: Long, targetUserId: Long, snackBarState: SnackbarHostState) {
        viewModelScope.launch {
            val result = localApi.dealWithFollowAction(myId, targetUserId)
            /* 执行关注事务 */
            launch {
                if (result.data) {
                    snackBarState.showSnackbar("关注成功！","关闭", SnackbarDuration.Short)
                } else {
                    snackBarState.showSnackbar("完成取消关注！","关闭", SnackbarDuration.Short)
                }
            }
            delay(50)
            /* 重新加载关注关系 与当前用户 粉丝关注数据 */
            loadRelative(myId ,targetUserId)
            loadUserCount(targetUserId)
        }
    }

    /* 请求当前用户个人页面的 用户详细信息 */
    fun currentUserDetailFlow(userId : Long) : Flow<RequestUserDetail> {
        return flow {
            emit(localApi.getUserDetail(userId).data)
        }.flowOn(Dispatchers.IO)
    }
}