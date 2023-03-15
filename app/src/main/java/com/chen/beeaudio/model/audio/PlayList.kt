package com.chen.beeaudio.model.audio

import com.chen.beeaudio.model.localmodel.Subscribe
import com.google.gson.annotations.SerializedName
import javax.annotation.Nullable

/* 歌单类型 */
data class PlayList(
    @SerializedName("id") val id : Long,
    @SerializedName("name") var name : String,
    @SerializedName("coverImgUrl") val coverImageUrl : String,
    @SerializedName("userId") var userId : Long,
    @SerializedName("createTime") val createTime : Long,
    @SerializedName("description") val description : String,
    @SerializedName("tags") @Nullable val tags : List<String>?,
    @SerializedName("creator") val creator : Creator,
    @SerializedName("tracks") @Nullable val tracks : List<Track>?,
    @SerializedName("trackIds") @Nullable val trackIds : List<TrackId>?
) {
    /** 将该歌单加入订阅，获取该歌单的订阅对象
     *  @param  isMyPlayList    是否为当前用户的自建歌单
     */
    fun toMySubscribe(isMyPlayList : Boolean) : Subscribe {
        return Subscribe(
            itemId = this.id,
            type = 1000,
            title = this.name,
            creator = this.creator.nickName,
            coverImgUrl = this.coverImageUrl,
            isMyCreated = isMyPlayList,
            isTop = false,      // 默认为false
            weight = 0
        )
    }
}
