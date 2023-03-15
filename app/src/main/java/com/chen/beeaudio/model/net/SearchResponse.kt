package com.chen.beeaudio.model.net

import com.chen.beeaudio.model.audio.Album
import com.chen.beeaudio.model.audio.Artist
import com.chen.beeaudio.model.audio.PlayList
import com.chen.beeaudio.model.audio.Track
import com.google.gson.annotations.SerializedName

/* 单曲搜索结果子项 */
data class SearchSongs(
    @SerializedName("songs")
    val songs : List<Track>,
    @SerializedName("songCount")
    val songCount : Int
)

/* 专辑搜索结果子项 */
data class SearchAlbums(
    @SerializedName("albums")
    val albums : List<Album>,
    @SerializedName("albumCount")
    val albumCount : Int
)

/* 艺人搜索结果子项 */
data class SearchArtists(
    @SerializedName("artists")
    val artists : List<Artist>,
    @SerializedName("artistCount")
    val artistCount : Int
)

/* 歌单搜索结果子项 */
data class SearchPlayLists(
    @SerializedName("playlists")
    val playlists : List<PlayList>,
    @SerializedName("playlistCount")
    val playlistCount : Int
)