package com.chen.beeaudio.model.audio

import com.chen.beeaudio.model.localmodel.TrackSummary
import com.google.gson.annotations.SerializedName

//data class SongInfo(
//
//)

data class Track(
    @SerializedName("id") val id : Long,
    @SerializedName("name") val name : String,
    @SerializedName("ar") val ar : List<Ar>, // 艺人简易信息（艺人ID 与 姓名/艺名）
    @SerializedName("al") val al : Al,    // 专辑简易信息
    @SerializedName("dt") val dt : Long,  //  音频时长
    @SerializedName("fee") val fee : Int,  // 付费音频表示
    @SerializedName("noCopyrightRcmd") val noCopyrightRcmd : String?, // 可播放标识
    @SerializedName("source") val source : String?,    // 音频来源
    @SerializedName("usable") val usable : Boolean,   //  可用标识
    @SerializedName("privilege_signal") val privilegeSignal : Int?,   // 播放权限标识
) {
    /** 转换为track集合形式
     *  @param  trackFile   与该track音频对应的文件详细信息类型对象
     */
    fun mapToWithUrl(trackFile: TrackFile): TrackWithUrl {
        return TrackWithUrl(
            id = this.id,
            name = this.name,
            ar = this.ar,
            al = this.al,
            dt = this.dt,
            fee = this.fee,
            noCopyrightRcmd = this.noCopyrightRcmd,
            source = this.source,
            usable = this.usable,
            privilegeSignal = this.privilegeSignal,
            url = trackFile.url,
            md5 = trackFile.md5,
            size = trackFile.size,
            encodeType = trackFile.encodeType
        )
    }

    /** 转换为储存在本地的概要信息形式
     *  @param  playListId  收录该曲目的歌单ID
     */
    fun mapToTrackSummary(playListId: Long) : TrackSummary {
        return TrackSummary(
            songId = this.id,
            playlistId = playListId
        )
    }
}

/* 简易专辑信息 */
data class Al(
    @SerializedName("id") val id : Long,
    @SerializedName("name") val name : String,
    @SerializedName("picUrl") val picUrl : String?,
    @SerializedName("pic") val pic : Long?,
)

/* 简易艺人西信息 */
data class Ar(
    @SerializedName("id") val id : Long,
    @SerializedName("name") val name : String,
)

/* 只留存音频ID的Track信息 */
data class TrackId(
    val id : Long,
)