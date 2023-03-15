package com.chen.beeaudio.repository.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chen.beeaudio.model.localmodel.Blog

@Dao
interface BlogDao {

    @Query("SELECT * FROM blog_table ORDER BY created_at DESC")
    fun getAllBlogs(): PagingSource<Int, Blog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBlogs(blogs: List<Blog>)

    @Query("DELETE FROM blog_table")
    suspend fun deleteAllBlogs()
}