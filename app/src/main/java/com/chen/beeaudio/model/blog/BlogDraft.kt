package com.chen.beeaudio.model.blog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*  草稿类 */
@Entity(tableName = "blog_draft")
data class BlogDraft(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "time") var wroteTime : String,
    @ColumnInfo(name = "context") var wroteContext : String
)