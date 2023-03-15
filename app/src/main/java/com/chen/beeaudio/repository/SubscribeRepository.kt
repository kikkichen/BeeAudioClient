package com.chen.beeaudio.repository

import android.content.Context
import android.widget.Toast
import com.chen.beeaudio.model.audio.Track
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.database.SubscribeDatabase
import com.chen.beeaudio.repository.database.TrackSummaryDatabase
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Named

class SubscribeRepository @Inject constructor (
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val trackSummaryDatabase: TrackSummaryDatabase,
    private val subscribeDatabase: SubscribeDatabase
) {
    /** 向服务器同步当前用户的收藏订阅数据
     *  @param  currentUserId   当前用户ID
     *  @param  toastEvent  Toast提示框弹出事件
     */
    suspend fun syncCurrentSubscribeDataToServer(currentUserId : Long, toastEvent : (String) -> Unit) {
        /* 获取订阅数据库中全部内容 */
        val allSubscribeData : List<Subscribe> = subscribeDatabase.subscribeDao().getAllSubscribeData()
        /* 向服务器同步数据 */
        val jsonSubscribeData = Gson().toJson(allSubscribeData)
        try {
            localApi.syncMySubscribeData(uid = currentUserId, data = jsonSubscribeData)
        } catch (e : Throwable) {
            toastEvent("同步过程出现了些许错误，稍后将重试～")
            e.printStackTrace()
        }
    }

    /** 将目标歌单加入订阅
     *  @param  playlistId  目标歌单ID
     */
    suspend fun subscribeTargetPlaylist(playlistId : Long, currentUserId : Long, toastEvent: (String) -> Unit) {
        try {
            /* 获取歌单信息 */
            val response = localApi.getPlayListDetail(playListId = playlistId)
            if (response.ok == 1 && response.data.id != 0.toLong()) {
                 /* 将歌单信息作为订阅数据存入数据库 */
                subscribeDatabase.subscribeDao()
                    .addSingleSubscribeData(
                        response.data.toMySubscribe(
                            isMyPlayList = response.data.creator.userId == currentUserId
                        )
                    )
                /* 向服务器同步我的数据 */
                syncCurrentSubscribeDataToServer(currentUserId = currentUserId, toastEvent = toastEvent)
            } else {
                toastEvent("订阅失败，稍后重试~ (1)")
            }
        } catch (e : Throwable) {
            toastEvent("订阅失败，稍后重试~ (2)")
            e.printStackTrace()
        }
    }

    /** 获取全部订阅项目数据
     *
     */
    suspend fun loadAllSubscribeData() : List<Subscribe> {
        return subscribeDatabase.subscribeDao().getAllSubscribeData()
    }

    /** 获取全部订阅歌单数据
     *
     */
    suspend fun loadAllPlayListSubscribeData() : List<Subscribe> {
        return subscribeDatabase.subscribeDao().getAllPlayListSubscribeData()
    }

    /** 获取全部自建歌单数据
     *
     */
    suspend fun loadAllMyCreatedPlayList() : List<Subscribe> {
        return subscribeDatabase.subscribeDao().getAllMyCreatedPlayList()
    }

    /** 获取所有专辑订阅数据
     *
     */
    suspend fun loadAllAlbumSubscribeData() : List<Subscribe> {
        return subscribeDatabase.subscribeDao().getAllAlbumSubscribeData()
    }

    /** 获取所有艺人订阅数据
     *
     */
    suspend fun loadAllArtistSubscribeData() : List<Subscribe> {
        return subscribeDatabase.subscribeDao().getAllArtistSubscribeData()
    }

    /** 关键字查找我的订阅项目内容数据
     *
     */
    suspend fun loadSubscribeDataByKeyword(keyword: String) : List<Subscribe> {
        return subscribeDatabase.subscribeDao().searchSubscribeDataByKeyword(keyword = keyword)
    }

    /* 通过主键查找订阅数据 */
    suspend fun getSubscribeData(itemId : Long, itemType : Int) : Subscribe {
        return subscribeDatabase.subscribeDao().searchSubscribeDataByPrimaryKey(itemId = itemId, type = itemType)
    }

    /** 更改目标订阅项目的置顶状态
     *
     */
    suspend fun changeSubscribeDataTopState(originalSubscribeData : Subscribe) {
        subscribeDatabase.subscribeDao()
            .changeSubscribeDataTopState(originalSubscribeData.copy(
                weight = if (originalSubscribeData.isTop) originalSubscribeData.weight - 32768 else originalSubscribeData.weight + 32768,
                isTop = !originalSubscribeData.isTop),
            )
    }

    /** 更改订阅项目其他属性
     *
     */
    suspend fun changeSubscribeDataItem(newSubscribeData: Subscribe) {
        subscribeDatabase.subscribeDao()
            .changeSubscribeDataTopState(newSubscribeData)
    }

    /** 自增权重
     *
     */
    suspend fun IncreasingSubscribeItemWeight(track: Track) {
        /* 关联歌单自增权重 */
        if (trackSummaryDatabase.trackSummaryDao().getTrackExistInPlaylistBySongId(track.id) > 0) {
            val playListIds = trackSummaryDatabase.trackSummaryDao().getTrackSummaryListBySongId(track.id).map { it.playlistId }
            playListIds.forEach { playlistId ->
                subscribeDatabase.subscribeDao().apply {
                    val originalSubscribeData = searchSubscribeDataByPrimaryKey(itemId = playlistId, type = 1000)
                    changeSubscribeDataItem(newSubscribeData = originalSubscribeData.copy(weight = originalSubscribeData.weight + 1))
                }
            }
        }
        subscribeDatabase.subscribeDao().apply {
            /* 关联专辑自增权重 */
            if (getExistInSubscribeDatabase(itemId = track.al.id, type = 10) > 0) {
                val originalSubscribeData = searchSubscribeDataByPrimaryKey(itemId = track.al.id, type = 10)
                changeSubscribeDataItem(newSubscribeData = originalSubscribeData.copy(weight = originalSubscribeData.weight + 1))
            }
            /* 关联艺人自增权重 */
            track.ar.map { it.id }.forEach { artistId ->
                if (getExistInSubscribeDatabase(itemId = artistId, type = 100) > 0) {
                    val originalSubscribeData = searchSubscribeDataByPrimaryKey(itemId = artistId, type = 100)
                    changeSubscribeDataItem(newSubscribeData = originalSubscribeData.copy(weight = originalSubscribeData.weight + 1))
                }
            }
        }
    }

    /** 依据主键删除订阅数据
     *  @param  itemId  订阅数据条目ID
     *  @param  itemType    订阅数据类型 
     */
    suspend fun deleteSubscribeDataByPrimaryKey(itemId : Long, itemType : Int) {
        subscribeDatabase.subscribeDao().deleteSubscribeDataByPrimaryKey(itemId = itemId, itemType = itemType)
    }

    /** 清除所有数据
     *
     */
    suspend fun clearAllData() {
        subscribeDatabase.subscribeDao().clearAllSubscribeData()
    }
}