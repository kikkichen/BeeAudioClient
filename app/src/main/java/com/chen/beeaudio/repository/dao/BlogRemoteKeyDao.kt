package com.chen.beeaudio.repository.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chen.beeaudio.model.localmodel.BlogRemoteKeys

@Dao
interface BlogRemoteKeyDao {

    @Query("SELECT * FROM blog_remote_key_table WHERE bid = :id")
    suspend fun getRemoteKeys(id: Long): BlogRemoteKeys

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllRemoteKeys(remoteKeys: List<BlogRemoteKeys>)

//    @Insert(onConflict = REPLACE)
//    suspend fun addSingleRemoteKey(remoteKey: BlogRemoteKeys)

    @Query("DELETE FROM BLOG_REMOTE_KEY_TABLE")
    suspend fun deleteAllRemoteKeys()
}