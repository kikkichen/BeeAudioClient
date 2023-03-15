package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.blog.SimpleUser
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class FansUserViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi
) : ViewModel() {
    /* 粉丝用户列表 */
    private val _fansUsers = MutableStateFlow<FansResultState>(FansResultState.Loading)
    val fansUsers = _fansUsers

    /* 加载粉丝用户列表 */
    fun loadFansUserList(userId : Long, myId: Long) {
        viewModelScope.launch {
            try {
                _fansUsers.value = FansResultState.Loading
                val fansResult = localApi.getMyFans(userId = userId, myId = myId).data
                _fansUsers.value = FansResultState.Success(list = fansResult)
            } catch (e : Throwable) {
                _fansUsers.value = FansResultState.Error(e)
            }
        }
    }

    /* 执行关注、取消关注事务 */
    fun dealWithFollowAction(myId: Long, targetUserId: Long) {
        viewModelScope.launch {
            try {
                localApi.dealWithFollowAction(myId, targetUserId)
                delay(500)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}

/* 关注用户列表请求结果状态 */
sealed class FansResultState() {
    object Loading : FansResultState()                                // 加载中
    class Error(e : Throwable) : FansResultState()                    // 错误
    class Success(val list : List<SimpleUser>) : FansResultState()    // 请求结果成功
}