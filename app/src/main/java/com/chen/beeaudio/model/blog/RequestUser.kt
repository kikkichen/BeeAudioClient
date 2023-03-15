package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

data class RequestUser(
    @SerializedName("uid") val Uid: Long,
    @SerializedName("screen_name") val Name: String,
    @SerializedName("profile_image_url") val AvatarUrl: String,
    @SerializedName("description") val Description: String
)