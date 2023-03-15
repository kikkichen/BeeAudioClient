package com.chen.beeaudio.model.localmodel

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chen.beeaudio.init.LOCAL_ROOM_BLOG_TABLE
import javax.annotation.Nullable

/**
 *  单条博文主题内容 - Room
 */
@Entity(tableName = LOCAL_ROOM_BLOG_TABLE)
data class Blog(
    @PrimaryKey @ColumnInfo(name = "id") val Id: Long,
    @ColumnInfo(name = "created_at") val Created: Long,
    @ColumnInfo(name = "text") val Text: String,
    @ColumnInfo(name = "source") val Source: String,
    @ColumnInfo(name = "reposts_count") val ReportCounts: Int,
    @ColumnInfo(name = "comments_count") val CommentCounts: Int,
    @ColumnInfo(name = "attitudes_count") val Attitudes: Int,
    @ColumnInfo(name = "pic_urls") val UrlGroup: String,
    // User 属性
    @ColumnInfo(name = "uid") val Uid: Long,
    @ColumnInfo(name = "screen_name") val UserName: String,
    @ColumnInfo(name = "profile_image_url") val UserAvatar: String,
    @ColumnInfo(name = "user_description") val UserDescription: String,
    @ColumnInfo(name = "media_url") val MediaUrl: String,


    @Embedded val RetweetedStatus: RetweetedBlog
)

/**
 *  被推转微博 - Room
 *
 */
data class RetweetedBlog(
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_id") @Nullable val Id: Long,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_created_at") @Nullable val Created: Long,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_text") @Nullable val Text: String,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_reposts_count") @Nullable val ReportCounts: Int,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_comments_count") @Nullable val CommentCounts: Int,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_attitudes_count") @Nullable val Attitudes: Int,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_pic_urls") @Nullable val UrlGroup: String,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_media_url") @Nullable val MediaUrl: String,
    // Retweeted User 属性
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_uid") @Nullable val Uid: Long,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_screen_name") @Nullable val UserName: String,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_profile_image_url") @Nullable val UserAvatar: String,
    @SuppressLint("KotlinNullnessAnnotation") @ColumnInfo(name = "retweeted_user_description") @Nullable val UserDescription: String,
)