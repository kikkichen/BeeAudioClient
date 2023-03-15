package com.chen.beeaudio.net

import com.chen.beeaudio.model.net.AuthLoginResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    /** 登陆验证
     *
     */
    @FormUrlEncoded
    @POST("/token")
    suspend fun loginAndGetToken(
        @Field("grant_type") grantType : String = "password",
        @Field("username") userName : String,
        @Field("password") password : String,
        @Field("scope") scope : String = "all"
    ) : AuthLoginResponse

    /** 更新口令
     *
     */
    @FormUrlEncoded
    @POST("token")
    suspend fun refreshToken(
        @Field("grant_type") grantType : String = "refresh_token",
        @Field("refresh_token") refreshToken : String
    ) : AuthLoginResponse
}