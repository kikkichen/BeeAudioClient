package com.chen.beeaudio.model.audio

import com.google.gson.annotations.SerializedName

data class TrackFile(
    @SerializedName("encodeType") val encodeType: String,
    @SerializedName("id") val id: Long,
    @SerializedName("md5") val md5: String,
    @SerializedName("size") val size: Int,
    @SerializedName("time") val time: Long,
    @SerializedName("url") val url: String
)