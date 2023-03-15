package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

/**
 *  单条博文主题内容
 */
data class RequestBlog(
    @SerializedName("bid") val Id: Long,
    @SerializedName("post_at") val Created: String,
    @SerializedName("text") val Text: String,
    @SerializedName("source") val Source: String,
    @SerializedName("reposts_count") val ReportCounts: Int,
    @SerializedName("comments_count") val CommentCounts: Int,
    @SerializedName("attitudes_count") val Attitudes: Int,
    @SerializedName("user") val User: RequestUser,
    @SerializedName("pic_urls") val UrlGroup: List<PicUrl> = emptyList(),
    @SerializedName("retweeted_status") val RetweetedStatus: RetweetedBlog,
    @SerializedName("media_url") val MediaUrl : String
)

/**
 *  被推转微博
 *
 */
data class RetweetedBlog(
    @SerializedName("bid") val Id: Long,
    @SerializedName("post_at") val Created: String,
    @SerializedName("text") val Text: String,
    @SerializedName("pic_urls") val UrlGroup: List<PicUrl> = emptyList(),
    @SerializedName("user") val User: RequestUser,
    @SerializedName("reposts_count") val ReportCounts: Int,
    @SerializedName("comments_count") val CommentCounts: Int,
    @SerializedName("attitudes_count") val Attitudes: Int,
    @SerializedName("media_url") val MediaUrl : String
)

/**
 *  单条缩略图链接
 */
data class PicUrl(
    @SerializedName("thumbnail_pic") val url : String,
)