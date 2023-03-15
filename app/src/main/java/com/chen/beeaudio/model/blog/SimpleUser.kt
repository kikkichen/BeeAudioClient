package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

data class SimpleUser(
    @SerializedName("uid") val Id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("avatar_url") val avatar: String,
    @SerializedName("created_at") val createAt: String,
    @SerializedName("follow_state") val followState : Int
)

data class SimpleUserCount(
    @SerializedName("follows") val follows : Int,
    @SerializedName("fans") val fans : Int,
    @SerializedName("friends") val friends : Int
)