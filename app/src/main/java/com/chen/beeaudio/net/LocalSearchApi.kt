package com.chen.beeaudio.net

import com.chen.beeaudio.model.net.*
import retrofit2.http.GET
import retrofit2.http.Query

interface LocalSearchApi {
    /** 根据关键字搜索单曲结果 (分页)
     *  @param  keywords  搜索关键字
     *  @param  page    请求页码(默认单页容量为30)
     *  @param  type    搜索类型 ，当前值为1
     *
     */
    @GET("/play/search")
    suspend fun getSearchSongsResult(
        @Query("keywords") keywords : String,
        @Query("type") type: Int = 1,
        @Query("page") page: Int,
    ) : ResponseBody<SearchSongs>

    /** 根据关键字搜索专辑结果 (分页)
     *  @param  keywords  搜索关键字
     *  @param  page    请求页码(默认单页容量为30)
     *  @param  type    搜索类型 ，当前值为10
     *
     */
    @GET("/play/search")
    suspend fun getSearchAlbumsResult(
        @Query("keywords") keywords : String,
        @Query("type") type: Int = 10,
        @Query("page") page: Int,
    ) : ResponseBody<SearchAlbums>

    /** 根据关键字搜索艺人结果 (分页)
     *  @param  keywords  搜索关键字
     *  @param  page    请求页码(默认单页容量为30)
     *  @param  type    搜索类型 ，当前值为100
     *
     */
    @GET("/play/search")
    suspend fun getSearchArtistsResult(
        @Query("keywords") keywords : String,
        @Query("type") type: Int = 100,
        @Query("page") page: Int,
    ) : ResponseBody<SearchArtists>

    /** 根据关键字搜索歌单结果 (分页)
     *  @param  keywords  搜索关键字
     *  @param  page    请求页码(默认单页容量为30)
     *  @param  type    搜索类型 ，当前值为1000
     *
     */
    @GET("/play/search")
    suspend fun getSearchPlayListsResult(
        @Query("keywords") keywords : String,
        @Query("type") type: Int = 1000,
        @Query("page") page: Int,
    ) : ResponseBody<SearchPlayLists>

}