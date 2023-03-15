package com.chen.beeaudio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import coil.ImageLoader
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.PlayListsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TagPlayListsViewModel @Inject constructor(
    private val repository: PlayListsRepository,
    @Named("PlayListCover")
    private val imageLoader: ImageLoader
) :ViewModel() {
    val myImageLoader = imageLoader
    /* 索引歌单结果 */
    private val _targetPlayLists = MutableStateFlow<PagingData<PlayList>>(PagingData.empty())
    val targetPlayLists = _targetPlayLists

    /* 获取 指定索引标签下的 歌单列表 */
    fun loadTargetIndexPlayLists(cat : String) {
        viewModelScope.launch {
            repository
                .getTargetPlayLists(cat = cat)
                .cachedIn(viewModelScope)
                .collect {
                    _targetPlayLists.value = it
                }
        }
    }
}