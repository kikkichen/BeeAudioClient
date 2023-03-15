package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

data class Attitude(
    @SerializedName("avatar_url") val avatar_url: String,
    @SerializedName("aid") val aid: Long,
    @SerializedName("bid") val bid: Long,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("description") val description: String,
    @SerializedName("name") val name: String,
    @SerializedName("source") val source: String,
    @SerializedName("uid") val uid: Long
)