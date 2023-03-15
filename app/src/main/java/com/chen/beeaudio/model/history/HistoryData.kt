package com.chen.beeaudio.model.history

import com.chen.beeaudio.model.audio.Track
import com.google.gson.annotations.SerializedName

/* 历史记录网络请求响应集合 */
data class HistoryData(
    @SerializedName("play_at") val playAt: Long,
    @SerializedName("song") val songInfo: Track
)
