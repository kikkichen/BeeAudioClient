package com.chen.beeaudio.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chen.beeaudio.model.blog.BlogDraft
import com.chen.beeaudio.repository.dao.DraftDao

@Database(entities = [BlogDraft::class], version = 1, exportSchema = false)
abstract class DraftDatabase : RoomDatabase() {

    abstract fun draftDao() : DraftDao

}