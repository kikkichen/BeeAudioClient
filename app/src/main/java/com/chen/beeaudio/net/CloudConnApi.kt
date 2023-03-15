package com.chen.beeaudio.net

import com.chen.beeaudio.model.blog.NetResponseBody
import com.chen.beeaudio.model.blog.TopicData
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 云析API
 */
interface CloudConnApi {

    /**
     *  获取热搜话题
     *  @param  api API类型
     *  @param  key 云析API 个人用户 key
     */
    @GET("api.php")
    suspend fun getCloudConnData(
        @Query("api") api : Int,
        @Query("key") key : String
    ) : NetResponseBody<List<TopicData>>
}