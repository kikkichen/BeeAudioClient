package com.chen.beeaudio.repository.dao

import androidx.room.*
import com.chen.beeaudio.model.blog.BlogDraft
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {

    /* 插入一条草稿项目 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSingleDraft(vararg draft: BlogDraft)

    /* 删除一条草稿项目 */
    @Delete
    suspend fun deleteSingleDraft(vararg draft: BlogDraft)

    /* 查询所有草稿项目 */
    @Query("SELECT * FROM blog_draft")
    fun queryAllDrafts(): Flow<List<BlogDraft>>

    /* 清空草稿数据库内容 */
    @Query("DELETE FROM blog_draft")
    suspend fun clearAllDrafts()
}