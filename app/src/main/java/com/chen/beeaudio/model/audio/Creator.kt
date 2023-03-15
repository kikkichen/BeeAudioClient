package com.chen.beeaudio.model.audio

import com.google.gson.annotations.SerializedName

/* 歌单创作用户 */
data class Creator(
    @SerializedName("userId") val userId : Long,
    @SerializedName("nickname") var nickName : String,
    @SerializedName("signatrue") val signatrue : String,
    @SerializedName("description") val description : String,
    @SerializedName("avatarUrl") val avatarUrl : String,
    @SerializedName("backgroundUrl") val backGroundUrl : String,
)