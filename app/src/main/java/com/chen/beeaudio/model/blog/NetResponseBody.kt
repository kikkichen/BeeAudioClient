package com.chen.beeaudio.model.blog

import com.google.gson.annotations.SerializedName

/**
 *  网络请求响应体
 */
data class NetResponseBody<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val message: String,
    @SerializedName("data") val data: T
)

/* 热门话题数据 */
data class TopicData(
    @SerializedName("top") val topList: List<TopicItem>,
    @SerializedName("hot") val hotList: List<TopicItem>
)

/* 话题信息 */
data class TopicItem(
    @SerializedName("query") val title: String,
    @SerializedName("rawUrl") val url: String,
    @SerializedName("desc") val icon: String
)