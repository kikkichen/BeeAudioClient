package com.chen.beeaudio.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import coil.ImageLoader
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.net.LocalSearchApi
import com.chen.beeaudio.repository.AudioSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    @Named("PlayListCover")
    private val imageLoader: ImageLoader,
    private val repository: AudioSearchRepository
) : ViewModel() {
    val myImageLoader = imageLoader
    /* 搜索关键字 */
    val currentKeyWords : MutableStateFlow<String> = MutableStateFlow("")
    /* 变更搜索关键字 */
    fun changeCurrentKeyWords(newWords : String) {
        currentKeyWords.value = newWords
        /* 当搜索关键字发生变动时，将原有的搜索结果pagingData清零 */
        _resultSongs.value = PagingData.empty()
        _resultAlbums.value = PagingData.empty()
        _resultArtists.value = PagingData.empty()
        _resultPlayList.value = PagingData.empty()
    }

    /* 单曲搜索结果 - PagingData */
    private val _resultSongs = MutableStateFlow<PagingData<Track>>(PagingData.empty())
    val resultSongs = _resultSongs
    /* 依据当前搜索关键字，加载单曲搜索结果 - PagingData */
    fun loadResultSongs() {
        viewModelScope.launch {
            repository
                .getSearchResultSongs(currentKeyWords.value)
                .cachedIn(viewModelScope)
                .collect {
                    _resultSongs.value = it
                }
        }
    }

    /* 专辑搜索结果 - PagingData */
    private val _resultAlbums = MutableStateFlow<PagingData<Album>>(PagingData.empty())
    val resultAlbums = _resultAlbums
    /* 依据当前搜索关键字，加载专辑搜索结果 - PagingData */
    fun loadResultAlbums() {
        viewModelScope.launch {
            repository
                .getSearchResultAlbums(currentKeyWords.value)
                .cachedIn(viewModelScope)
                .collect {
                    _resultAlbums.value = it
                }
        }
    }

    /* 艺人搜索结果 - PagingData */
    private val _resultArtists = MutableStateFlow<PagingData<Artist>>(PagingData.empty())
    val resultArtists = _resultArtists
    /* 依据当前搜索关键字，加载艺人搜索结果 - PagingData */
    fun loadResultArtists() {
        viewModelScope.launch {
            repository
                .getSearchResultArtists(currentKeyWords.value)
                .cachedIn(viewModelScope)
                .collect {
                    _resultArtists.value = it
                }
        }
    }

    /* 歌单搜索结果 */
    private val _resultPlayList = MutableStateFlow<PagingData<PlayList>>(PagingData.empty())
    val resultPlayList = _resultPlayList
    /* 依据当前搜索关键字，加载歌单搜索结果 - PagingData */
    fun loadResultPlayList() {
        viewModelScope.launch {
            repository
                .getSearchResultPlayLists(currentKeyWords.value)
                .cachedIn(viewModelScope)
                .collect {
                    _resultPlayList.value = it
                }
        }
    }
}