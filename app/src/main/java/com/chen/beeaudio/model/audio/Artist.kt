package com.chen.beeaudio.model.audio

import com.chen.beeaudio.model.localmodel.Subscribe
import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("albumSize") val albumSize: Int,
    @SerializedName("briefDesc") val briefDesc: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("picUrl") val picUrl : String,
    @SerializedName("id") val id: Long,
    @SerializedName("musicSize") val musicSize: Int,
    @SerializedName("name") val name: String
) {
    /** 将当前艺人加入我的订阅
     *
     */
    fun toMySubscribe() : Subscribe {
        return Subscribe(
            itemId = this.id,
            type = 100,
            title = this.name,
            creator = "",
            coverImgUrl = this.cover,
            isMyCreated = false,
            isTop = false,
            weight = 0
        )
    }
}