package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.chen.beeaudio.model.audio.Creator
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.net.ResponseBody
import com.chen.beeaudio.net.LocalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AudioHomeViewModel @Inject constructor(
    @Named("LocalServer") val localApi: LocalApi,
    @Named("PlayListCover") imageLoader: ImageLoader
) : ViewModel() {
    val myImageLoader = imageLoader

    /* 热门歌单 */
    private var _hotPlayListDataState : MutableStateFlow<HotPlayListDataState> = MutableStateFlow(HotPlayListDataState.Loading)
    val hotPlayListDataState = _hotPlayListDataState


    init {
        getHotPlayListData()
    }
    /* 请求热门歌单数据 */
    fun getHotPlayListData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _hotPlayListDataState.value = HotPlayListDataState.Loading
                val hotPlaylistData = localApi.getHotPlayListCollection().data
                if (hotPlaylistData.isNotEmpty()) {
                    _hotPlayListDataState.value = HotPlayListDataState.Success(collection = hotPlaylistData)
                } else {
                    _hotPlayListDataState.value = HotPlayListDataState.Error(Exception())
                }
            } catch (e : Throwable) {
                _hotPlayListDataState.value = HotPlayListDataState.Error(e)
            }
        }
    }

}

/* 热门歌单数据获取状态 */
sealed class HotPlayListDataState() {
    object Loading : HotPlayListDataState()     // 加载状态
    class Error(val e: Throwable) : HotPlayListDataState()      // 错误状态
    class Success(val collection : List<PlayList>) : HotPlayListDataState()
}