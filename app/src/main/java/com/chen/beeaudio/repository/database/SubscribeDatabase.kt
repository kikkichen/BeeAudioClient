package com.chen.beeaudio.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chen.beeaudio.model.localmodel.Subscribe
import com.chen.beeaudio.repository.dao.SubscribeDao

@Database(entities = [Subscribe::class], version = 1, exportSchema = false)
abstract class SubscribeDatabase : RoomDatabase() {
    abstract fun subscribeDao() : SubscribeDao
}