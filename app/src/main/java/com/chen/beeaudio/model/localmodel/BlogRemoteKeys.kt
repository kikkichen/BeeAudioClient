package com.chen.beeaudio.model.localmodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chen.beeaudio.init.LOCAL_ROOM_BLOG_REMOTE_KEY_TABLE

@Entity(tableName = LOCAL_ROOM_BLOG_REMOTE_KEY_TABLE)
data class BlogRemoteKeys(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "bid") val id: Long,
    @ColumnInfo(name = "prev_page") val prevPage: Int?,
    @ColumnInfo(name = "next_page") val nextPage: Int?
)