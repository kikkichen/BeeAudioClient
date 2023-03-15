package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("avatar_url") val avatar_url: String,
    @SerializedName("be_like") val be_like: Int,
    @SerializedName("bid") val bid: Long,
    @SerializedName("cid") val cid: Long,
    @SerializedName("description") val description: String,
    @SerializedName("name") val name: String,
    @SerializedName("post_at") val post_at: String,
    @SerializedName("root_id") val root_id: Long,
    @SerializedName("source") val source: String,
    @SerializedName("text") val text: String,
    @SerializedName("uid") val uid: Long
)