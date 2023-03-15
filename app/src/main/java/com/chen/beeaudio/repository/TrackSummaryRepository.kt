package com.chen.beeaudio.repository

import android.content.Context
import android.widget.Toast
import com.chen.beeaudio.model.localmodel.TrackSummary
import com.chen.beeaudio.net.LocalApi
import com.chen.beeaudio.repository.database.TrackSummaryDatabase
import javax.inject.Inject
import javax.inject.Named

class TrackSummaryRepository @Inject constructor(
    @Named("LocalServer")
    private val localApi: LocalApi,
    private val trackSummaryDatabase: TrackSummaryDatabase
) {
    /** 更新 当前曲目是否为我“喜欢”的曲目 标识
     *  @param  songId  查询目标曲目ID
     *  @param  onMyFavoriteTrackStateChangeEvent   变更我的默认喜爱歌单事件
     */
    suspend fun updateCurrentTrackIsMyFavoriteSignal(
        songId : Long,
        onMyFavoriteTrackStateChangeEvent : (List<TrackSummary>) -> Unit
    ) {
        val summaryList = trackSummaryDatabase.trackSummaryDao().getTrackSummaryListBySongId(songId = songId)
        /* 查询我默认喜爱歌单是否包含该曲目 */
        onMyFavoriteTrackStateChangeEvent(summaryList)
    }

    /** 将当前曲目进行 收藏/取消收藏  到默认歌单
     *  @param  songId  执行目标曲目ID
     *  @param  currentUserId   当前操作用户ID
     *  @param  myFavoritePlaylistId    我的默认喜爱歌单ID
     *  @param  toastEvent  Toast提示框弹出事件
     */
    suspend fun dealWithCollectMyFavoritePlayList(
        songId: Long,
        currentUserId : Long,
        myFavoritePlaylistId : Long,
        toastEvent: (String) -> Unit
    ) {
        val summaryRecord = trackSummaryDatabase.trackSummaryDao().getSingleTrackSummary(songId = songId, playlistId = myFavoritePlaylistId)
        try {
            if (summaryRecord.songId == songId) {
                /* 查找到记录，执行取消添加到默认收藏歌单逻辑 */
                try {
                    /* 向服务端提交本次取消收藏记录 */
                    val actionResult = localApi.editSongFromMyMultipleCreatedPlaylist(uid = currentUserId, sid = songId, addPlayListIds = emptyList(), removePlayListIds = listOf(myFavoritePlaylistId)).data
                    if (actionResult) {
                        /* 服务端成功执行移除我的默认喜爱收藏逻辑， 从而进一步执行本地的数据库记录移除逻辑 */
                        trackSummaryDatabase.trackSummaryDao().deleteSingleTrackSummary(summaryRecord)
                    } else {
                        toastEvent("网络繁忙，请稍后重试～")
                    }
                } catch (e : Throwable) {
                    toastEvent("⚠ 移除收藏操作有误，请稍后重试～")
                }
            } else {
                /* 未查找到记录， 执行收藏逻辑 */
                try {
                    val actionResult = localApi.addSongIntoMyCreatedPlaylist(uid = currentUserId, pid = myFavoritePlaylistId, sid = songId).data
                    if (actionResult) {
                        /* 服务端成功执行把该曲目添加到我的默认喜爱收藏逻辑，从而进一步执行本地等等数据库记录添加逻辑 */
                        trackSummaryDatabase.trackSummaryDao().addSingleTrackSummary(TrackSummary(songId = songId, playlistId = myFavoritePlaylistId))
                        toastEvent("成功添加到我喜欢的曲目 ~")
                    } else {
                        toastEvent("网络繁忙，请稍后重试～")
                    }
                } catch (e : Throwable) {
                    toastEvent("⚠ 添加到我的喜欢歌单有误，请稍后重试～")
                }
            }
        } catch (e : Throwable) {
            when (e) {
                is java.lang.NullPointerException -> {
                    /* 未查找到记录， 执行收藏逻辑 */
                    try {
                        val actionResult = localApi.addSongIntoMyCreatedPlaylist(uid = currentUserId, pid = myFavoritePlaylistId, sid = songId).data
                        if (actionResult) {
                            /* 服务端成功执行把该曲目添加到我的默认喜爱收藏逻辑，从而进一步执行本地等等数据库记录添加逻辑 */
                            trackSummaryDatabase.trackSummaryDao().addSingleTrackSummary(TrackSummary(songId = songId, playlistId = myFavoritePlaylistId))
                            toastEvent("成功添加到我喜欢的曲目 ~")
                        } else {
                            toastEvent("网络繁忙，请稍后重试～")
                        }
                    } catch (e : Throwable) {
                        toastEvent("⚠ 添加到我的喜欢歌单有误，请稍后重试～")
                    }
                }
                else -> {
                    e.printStackTrace()
                }
            }
        }
    }

    /** 获取目标自建歌单列表的曲目收录数量
     *  @param  playlistId  目标曲目ID
     */
    suspend fun getAmountOfCreatedPlayList(playlistId : Long) : Int {
        return trackSummaryDatabase.trackSummaryDao().getAmountOfPlaylist(playlistId = playlistId)
    }

    /** 删除目标歌单的所有曲目条目记录
     *  @param  playlistId  目标歌单的ID
     */
    suspend fun deleteTrackSummaryOfTargetPlaylist(playlistId: Long) {
        trackSummaryDatabase.trackSummaryDao().deleteTrackSummaryWithPlaylist(playlistId = playlistId)
    }
}