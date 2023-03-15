package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import com.chen.beeaudio.model.blog.SimpleUser
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Named

/**
 *  请求我的关注列表 @ 页面
 */
@HiltViewModel
class CallFollowerViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi
) : ViewModel() {

    /**
     *  请求关注列表
     */
    suspend fun loadFollowerList(userId : Long, myId : Long) = flow {
        val result = localApi.getMyFocus(userId = userId, myId = myId)
        emit(result.data)
    }

}