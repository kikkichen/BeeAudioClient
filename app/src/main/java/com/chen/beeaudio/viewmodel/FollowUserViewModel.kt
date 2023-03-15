package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.chen.beeaudio.model.blog.SimpleUser
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class FollowUserViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi
) : ViewModel() {
    /* 关注用户列表 */
    private val _focusUsers = MutableStateFlow<FollowResultState>(FollowResultState.Loading)
    val focusUsers = _focusUsers

    /* 加载关注用户列表 */
    fun loadFollowUserList(userId : Long, myId : Long) {
        viewModelScope.launch {
            try {
                _focusUsers.value = FollowResultState.Loading
                val followsResult = localApi.getMyFocus(userId = userId, myId = myId).data
                _focusUsers.value = FollowResultState.Success(followsResult)
            } catch (e : Throwable) {
                _focusUsers.value = FollowResultState.Error(e)
            }
        }
    }

    /* 执行关注、取消关注事务 */
    fun dealWithFollowAction(myId: Long, targetUserId: Long) {
        viewModelScope.launch {
            try {
                localApi.dealWithFollowAction(myId, targetUserId)
                delay(500)
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }
}

/* 关注用户列表请求结果状态 */
sealed class FollowResultState() {
    object Loading : FollowResultState()                                // 加载中
    class Error(e : Throwable) : FollowResultState()                    // 错误
    class Success(val list : List<SimpleUser>) : FollowResultState()    // 请求结果成功
}