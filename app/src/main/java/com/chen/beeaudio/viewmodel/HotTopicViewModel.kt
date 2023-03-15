package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import com.chen.beeaudio.init.CLOUD_CONN_KEY
import com.chen.beeaudio.model.blog.TopicData
import com.chen.beeaudio.net.CloudConnApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class HotTopicViewModel @Inject constructor(
    private val cloudConnApi: CloudConnApi
): ViewModel() {
    companion object {
        private const val REQUEST_TYPE : Int = 17       /* 云析API 请求微博热搜类型 */
    }

    /* 获取热门话题请求数据 */
    suspend fun loadHotTopicList() = flow<TopicData> {
        val result = cloudConnApi.getCloudConnData(REQUEST_TYPE, CLOUD_CONN_KEY)
        emit(result.data[0])
    }
}