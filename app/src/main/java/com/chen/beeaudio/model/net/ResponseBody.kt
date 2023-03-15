package com.chen.beeaudio.model.net

import com.google.gson.annotations.SerializedName

/** 服务端响应体外围
 *
 */
data class ResponseBody<T> (
    @SerializedName("ok") val ok : Int,
    @SerializedName("code") val code : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : T
)
