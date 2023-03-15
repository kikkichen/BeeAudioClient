package com.chen.beeaudio.model.audio

import com.chen.beeaudio.model.localmodel.Subscribe
import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("artist") val artist: Artist,
    @SerializedName("description") val description: String,
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("picUrl") val picUrl: String,
    @SerializedName("size") val size: Int
) {
    /** 将该专辑加入我的订阅数据
     *
     */
    fun toMySubscribe() : Subscribe {
        return Subscribe(
            itemId = this.id,
            type = 10,
            title = this.name,
            creator = this.artist.name,
            coverImgUrl = this.picUrl,
            isMyCreated = false,
            isTop = false,
            weight = 0
        )
    }
}

/* 用于在在专辑详情页请求详细信息的 Album数据模型 */
data class AlbumDetail(
    @SerializedName("resourceState") val resourceState : Boolean,
    @SerializedName("songs") val songs : List<Track>,
    @SerializedName("code") val code : Int,
    @SerializedName("album") val album : Album,
)