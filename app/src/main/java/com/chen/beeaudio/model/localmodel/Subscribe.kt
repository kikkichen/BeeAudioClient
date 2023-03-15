package com.chen.beeaudio.model.localmodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chen.beeaudio.init.LOCAL_ROOM_SUBSCRIBE_TABLE
import com.google.gson.annotations.SerializedName

/**
 *  订阅数据模型
 */
@Entity(tableName = LOCAL_ROOM_SUBSCRIBE_TABLE, primaryKeys = ["item_id", "item_type"])
data class Subscribe(
    @ColumnInfo(name = "item_id") @SerializedName("item_id") val itemId : Long,     // 项目ID
    @ColumnInfo(name = "item_type") @SerializedName("item_type") val type : Int,    // 项目类型 - （歌单 - 1000、专辑 - 10、艺人 - 100）
    @ColumnInfo(name = "title") @SerializedName("title") val title : String,                    // 标题
    @ColumnInfo(name = "creator") @SerializedName("creator") val creator: String,               // 歌单创作者 / 专辑创作艺人名称
    @ColumnInfo(name = "cover_url") @SerializedName("cover_url") val coverImgUrl : String,      // 封面
    @ColumnInfo(name = "is_my_created") @SerializedName("is_my_created") val isMyCreated : Boolean,     // 当前歌单为自建歌单标识
    @ColumnInfo(name = "is_top") @SerializedName("is_top") val isTop : Boolean,                 // 是否置顶
    @ColumnInfo(name = "weight") @SerializedName("weight") val weight : Int                     // 排序权重
)

