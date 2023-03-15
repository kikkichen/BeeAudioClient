package com.chen.beeaudio.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chen.beeaudio.model.audio.HotAndAllTags
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SearchViewModel @Inject constructor(
    @Named("LocalServer") val api: LocalApi
) : ViewModel() {
    init {
        getHotAndAllTagsData()
    }

    private val _searchKeyWords = mutableStateOf("")
    val searchKeyWords = _searchKeyWords
    /* 搜索关键字变更 */
    fun changeSearchKeyWords(newWords : String) {
        _searchKeyWords.value = newWords
    }

    private val _hotPlaylistTags = MutableStateFlow<NetTagsResults<HotAndAllTags>>(NetTagsResults.Loading)
    val hotPlaylistTags = _hotPlaylistTags

    /* 获取歌单索引列表信息 */
    fun getHotAndAllTagsData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _hotPlaylistTags.value = NetTagsResults.Loading
                val data = api.getHotAndAllTags().data
                if (data.allTags.isEmpty() || data.hotTags.isEmpty()) {
                    _hotPlaylistTags.value = NetTagsResults.Error
                } else {
                    _hotPlaylistTags.value = NetTagsResults.Success(data)
                }
            } catch ( _ : Throwable) {
                _hotPlaylistTags.value = NetTagsResults.Error
            }
        }
    }
}

/* 歌单索引标签获取结果 */
sealed class NetTagsResults<T>() {
    object Loading : NetTagsResults<HotAndAllTags>()
    object Error : NetTagsResults<HotAndAllTags>()
    data class Success(val tags : HotAndAllTags) : NetTagsResults<HotAndAllTags>()
}