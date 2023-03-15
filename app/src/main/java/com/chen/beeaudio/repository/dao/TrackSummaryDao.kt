package com.chen.beeaudio.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chen.beeaudio.model.localmodel.TrackSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackSummaryDao {
    /* 插入一条曲目概要数据 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSingleTrackSummary(vararg trackSummary: TrackSummary)

    /* 插入多条曲目概要数据 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMultipleTrackSummary(trackSummaryList: List<TrackSummary>)

    /* 查询某 自建/收藏 歌单的曲目列表 */
    @Query("SELECT * FROM track_summary_table WHERE playlist_id = :playlistId")
    fun getTrackSummaryListByPlaylistId(playlistId : Long) : List<TrackSummary>

    /* 查询某 曲目 存在 自建/收藏 中的记录 */
    @Query("SELECT * FROM track_summary_table WHERE song_id = :songId")
    fun getTrackSummaryListBySongId(songId : Long) : List<TrackSummary>

    /* 查询某一条曲目概要记录 */
    @Query("SELECT * FROM track_summary_table WHERE song_id = :songId AND playlist_id = :playlistId")
    fun getSingleTrackSummary(songId : Long, playlistId : Long) : TrackSummary

    /* 查询某自建歌单的收录曲目数量 */
    @Query("SELECT COUNT(*) FROM track_summary_table WHERE playlist_id = :playlistId")
    fun getAmountOfPlaylist(playlistId : Long) : Int

    /* 通过曲目ID查询曲目的相关歌单存在记录 */
    @Query("SELECT COUNT(*) FROM track_summary_table WHERE song_id = :songId")
    fun getTrackExistInPlaylistBySongId(songId: Long) : Int

    /* 查询某曲目在自建歌单中存在的记录 */
    @Query("SELECT COUNT(*) FROM track_summary_table WHERE song_id = :songId AND playlist_id = :playlistId")
    fun getTrackExistInPlaylist(songId : Long, playlistId : Long) : Int

    /* 删除一条曲目概要数据 */
    @Delete
    suspend fun deleteSingleTrackSummary(vararg trackSummary: TrackSummary)

    /* 删除某个 自建/收藏 歌单 */
    @Query("DELETE FROM track_summary_table WHERE playlist_id = :playlistId")
    suspend fun deleteTrackSummaryWithPlaylist(playlistId : Long)

    /* 删除所有数据 */
    @Query("DELETE FROM track_summary_table WHERE 1")
    suspend fun clearAllTrackSummary()
}