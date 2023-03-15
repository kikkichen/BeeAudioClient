package com.chen.beeaudio.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chen.beeaudio.mock.UserIDMock
import com.chen.beeaudio.model.history.HistoryData
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HistoryViewModel @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val repository: HistoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /* 当前使用用户ID */
    private val currentUserId : Long = savedStateHandle.get<Long>("user_id") ?: UserIDMock

    private val _historyDataCollections = MutableStateFlow<PagingData<HistoryData>>(PagingData.empty())
    val historyDataCollections = _historyDataCollections

    init {
        loadHistoryData()
    }

    /* 请求历史播放记录 */
    fun loadHistoryData() {
        viewModelScope.launch {
            repository
                .getPlayHistoryData(currentUserId)
                .cachedIn(viewModelScope)
                .collect {
                    _historyDataCollections.value = it
                }
        }
    }

    /* 请求清空播放历史记录 */
    fun clearMyPlayHistory(
        context: Context,
        onAfterClear: () -> Unit
    ) {
        viewModelScope.launch {
            val result = localApi.clearMyPlayHistory(currentUserId)
            if (result.data) {
                Toast.makeText(context, "历史记录清空成功！", Toast.LENGTH_SHORT).show()
                onAfterClear()
            } else {
                Toast.makeText(context, "历史记录清空错误，请稍后重试。", Toast.LENGTH_SHORT).show()
            }
        }
    }
}