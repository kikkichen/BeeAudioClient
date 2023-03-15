package com.chen.beeaudio.model.net

import com.google.gson.annotations.SerializedName

/**
 *  用户音频项目订阅数据
 */
data class SubscribeResponse(
    @SerializedName("uid") val uid : Long,
    @SerializedName("subscribe_data") val subscribeData : String,
)
