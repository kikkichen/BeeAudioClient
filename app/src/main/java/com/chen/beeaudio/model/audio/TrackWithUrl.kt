package com.chen.beeaudio.model.audio

import com.google.gson.annotations.SerializedName

data class TrackWithUrl(
    @SerializedName("id") val id : Long,
    @SerializedName("name") val name : String,
    @SerializedName("ar") val ar : List<Ar>?, // 艺人简易信息（艺人ID 与 姓名/艺名）
    @SerializedName("al") val al : Al?,    // 专辑简易信息
    @SerializedName("dt") val dt : Long?,  //  音频时长
    @SerializedName("fee") val fee : Int?,  // 付费音频表示
    @SerializedName("noCopyrightRcmd") val noCopyrightRcmd : String?, // 可播放标识
    @SerializedName("source") val source : String?,    // 音频来源
    @SerializedName("usable") val usable : Boolean?,   //  可用标识
    @SerializedName("privilege_signal") val privilegeSignal : Int?,   // 播放权限标识
    @SerializedName("url") val url : String,
    @SerializedName("md5") val md5: String?,
    @SerializedName("size") val size: Int?,
    @SerializedName("encodeType") val encodeType: String?,
)
