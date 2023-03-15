package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

data class Retweeted(
    @SerializedName("attitudes_count") val attitudes_count: Int,
    @SerializedName("avatar_url") val avatar_url: String,
    @SerializedName("bid") val bid: Long,
    @SerializedName("comments_count") val comments_count: Int,
    @SerializedName("description") val description: String,
    @SerializedName("name") val name: String,
    @SerializedName("picture_url") val picture_url: String,
    @SerializedName("post_at") val post_at: String,
    @SerializedName("reposts_count") val reposts_count: Int,
    @SerializedName("retweeted_bid") val retweeted_bid: Long,
    @SerializedName("source") val source: String,
    @SerializedName("text") val text: String,
    @SerializedName("uid") val uid: Long
)