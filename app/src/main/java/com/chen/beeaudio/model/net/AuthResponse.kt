package com.chen.beeaudio.model.net

import com.google.gson.annotations.SerializedName

/* 登陆成功反馈 */
data class AuthLoginResponse(
    @SerializedName("access_token") val accessToken : String,
    @SerializedName("expires_in") val expiresIn : Int,
    @SerializedName("refresh_token") val refreshToken : String,
    @SerializedName("scope") val scope : String,
    @SerializedName("token_type") val tokenType : String
)

/* 注册成功反馈 */
data class AuthRegister(
    @SerializedName("uid") val uid : Long,
    @SerializedName("name") val name : String,
    @SerializedName("description") val description : String,
    @SerializedName("avatar_url") val avatarUrl : String,
    @SerializedName("created_at") val createdAt : String,
    @SerializedName("password") val password : String,
    @SerializedName("user_type") val userType : Int
)

