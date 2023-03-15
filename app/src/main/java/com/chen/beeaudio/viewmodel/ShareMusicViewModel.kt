package com.chen.beeaudio.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.net.LocalSearchApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ShareMusicViewModel @Inject constructor(
    @Named("PlayListCover")
    private val imageLoader: ImageLoader,
    @Named("LocalServer")
    private val localApi: LocalApi,
    @Named("LocalSearchServer")
    private val localSearchApi : LocalSearchApi,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val myImageLoader = imageLoader
    /* 当前用户的默认喜爱歌单 */
    private val myFavoritePlaylistId : Long = savedStateHandle.get<Long>("my_like_playlist") ?: 0.toLong()

    /* 搜索关键字 */
    val currentKeyWords : MutableStateFlow<String> = MutableStateFlow("")

    /* 单曲搜索结果 - List */
    private val _resultSongs = MutableStateFlow<List<Track>>(emptyList())
    val resultSongs = _resultSongs

    /* 专辑搜索结果 - List */
    private val _resultAlbums = MutableStateFlow<List<Album>>(emptyList())
    val resultAlbums = _resultAlbums

    /* 艺人搜索结果 - List */
    private val _resultArtists = MutableStateFlow<List<Artist>>(emptyList())
    val resultArtists = _resultArtists

    /* 歌单搜索结果 */
    private val _resultPlayList = MutableStateFlow<List<PlayList>>(emptyList())
    val resultPlayList = _resultPlayList

    /* 我的喜欢曲目列表 */
    private val _myFavoriteList = MutableStateFlow<List<Track>>(emptyList())
    val myFavoriteList = _myFavoriteList

    init {
        loadMyFavoriteSongList()
    }

    /* 变更搜索关键字 */
    fun changeCurrentKeyWords(newWords : String) {
        currentKeyWords.value = newWords
        /* 当搜索关键字发生变动时，继续搜索新结果 */
        loadResultSongs()
        loadResultAlbums()
        loadResultArtists()
        loadResultPlayList()
    }

    /* 加载我的默认喜爱歌单列表 */
    fun loadMyFavoriteSongList() {
        viewModelScope.launch {
            try {
                _myFavoriteList.value = localApi.getPlayListTrackItems(
                    playListId = myFavoritePlaylistId,
                    page = 1,
                    size = 20
                ).data
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }

    /* 依据当前搜索关键字，加载单曲搜索结果 - List */
    fun loadResultSongs() {
        viewModelScope.launch {
            try {
                _resultSongs.value = localSearchApi.getSearchSongsResult(
                    keywords = currentKeyWords.value,
                    page = 1
                ).data.songs
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }

    /* 依据当前搜索关键字，加载专辑搜索结果 - List */
    fun loadResultAlbums() {
        viewModelScope.launch {
            try {
                _resultAlbums.value = localSearchApi.getSearchAlbumsResult(
                    keywords = currentKeyWords.value,
                    page = 1
                ).data.albums
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }

    /* 依据当前搜索关键字，加载艺人搜索结果 - List */
    fun loadResultArtists() {
        viewModelScope.launch {
            try {
                _resultArtists.value = localSearchApi.getSearchArtistsResult(
                    keywords = currentKeyWords.value,
                    page = 1
                ).data.artists
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }

    /* 依据当前搜索关键字，加载歌单搜索结果 - List */
    fun loadResultPlayList() {
        viewModelScope.launch {
            try {
                _resultPlayList.value = localSearchApi.getSearchPlayListsResult(
                    keywords = currentKeyWords.value,
                    page = 1
                ).data.playlists
            } catch (e : Throwable) {
                e.printStackTrace()
            }
        }
    }
}