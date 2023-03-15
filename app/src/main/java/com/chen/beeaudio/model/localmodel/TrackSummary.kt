package com.chen.beeaudio.model.localmodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chen.beeaudio.init.LOCAL_ROOM_TRACK_SUMMARY_TABLE

/** 用于储存我的 收藏/自建 歌单曲目
 *
 */
@Entity(tableName = LOCAL_ROOM_TRACK_SUMMARY_TABLE, primaryKeys = ["song_id", "playlist_id"])
data class TrackSummary(
    @ColumnInfo(name = "song_id") val songId : Long,
    @ColumnInfo(name = "playlist_id") val playlistId : Long,
)