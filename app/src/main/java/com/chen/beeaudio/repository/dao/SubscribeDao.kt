package com.chen.beeaudio.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chen.beeaudio.model.localmodel.Subscribe
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscribeDao {
    /* 查询所有订阅项目 */
    @Query("SELECT * FROM subscribe_table ORDER BY is_top ASC")
    fun getAllSubscribeData() : List<Subscribe>

    /* 查询所有歌单订阅项目 */
    @Query("SELECT * FROM subscribe_table WHERE item_type = 1000 ORDER BY is_top ASC")
    fun getAllPlayListSubscribeData() : List<Subscribe>

    /* 查询所有艺人订阅项目 */
    @Query("SELECT * FROM subscribe_table WHERE item_type = 100 ORDER BY is_top ASC")
    fun getAllArtistSubscribeData() : List<Subscribe>

    /* 查询所有专辑订阅项目 */
    @Query("SELECT * FROM subscribe_table WHERE item_type = 10 ORDER BY is_top ASC")
    fun getAllAlbumSubscribeData() : List<Subscribe>

    /* 查询我所有的自建歌单 */
    @Query("SELECT * FROM subscribe_table WHERE item_type = 1000 AND is_my_created = 1 ORDER BY is_top ASC")
    fun getAllMyCreatedPlayList() : List<Subscribe>

    /* 查询我所有的收藏歌单 */
    @Query("SELECT * FROM subscribe_table WHERE item_type = 1000 AND is_my_created = 1 ORDER BY is_top ASC")
    fun getAllMyCollectedPlayList() : List<Subscribe>

    /* 更新所有订阅数据项目 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubscribeData(listData : List<Subscribe>)

    /* 更改项目置顶状态 */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun changeSubscribeDataTopState(vararg subscribeData: Subscribe)

    /* 关键字查询订阅项目 */
    @Query("SELECT * FROM subscribe_table WHERE title LIKE :keyword Or creator LIKE :keyword ORDER BY is_top ASC")
    fun searchSubscribeDataByKeyword(keyword : String) : List<Subscribe>

    /* 查询相关类型的订阅数据是否存在与数据库中 */
    @Query("SELECT COUNT(*) FROM subscribe_table WHERE item_id = :itemId AND item_type = :type")
    suspend fun getExistInSubscribeDatabase(itemId : Long, type : Int) : Int

    /* 通过 订阅项目ID 和 订阅项目类型 查询订阅项目数据 */
    @Query("SELECT * FROM subscribe_table WHERE item_id = :itemId AND item_type = :type")
    suspend fun searchSubscribeDataByPrimaryKey(itemId : Long, type : Int) : Subscribe

    /* 插入一条订阅项目数据 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSingleSubscribeData(vararg subscribeData : Subscribe)

    /* 依据主键信息删除订阅数据信息 */
    @Query("DELETE FROM subscribe_table WHERE item_id = :itemId AND item_type = :itemType")
    suspend fun deleteSubscribeDataByPrimaryKey(itemId : Long, itemType : Int)

    /* 删除一条订阅项目数据 */
    @Delete
    suspend fun deleteSingleSubscribeData(vararg subscribeData : Subscribe)

    /* 删除所有非自建的订阅项目数据 */
    @Query("DELETE FROM subscribe_table WHERE is_my_created = 0")
    suspend fun clearAllMySubscribeData()

    /* 删除所有本地订阅项目数据 */
    @Query("DELETE FROM subscribe_table WHERE 1")
    suspend fun clearAllSubscribeData()
}