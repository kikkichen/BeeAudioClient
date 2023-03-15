package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

data class RequestUserDetail(
    @SerializedName("avatar_url") val avatar_url: String,
    @SerializedName("birthday") val birthday: String,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("description") val description: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("uid") val uid: Long,
    @SerializedName("user_type") val user_type: Int
)

