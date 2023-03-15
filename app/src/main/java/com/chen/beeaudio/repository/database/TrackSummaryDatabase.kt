package com.chen.beeaudio.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chen.beeaudio.model.localmodel.TrackSummary
import com.chen.beeaudio.repository.dao.TrackSummaryDao

@Database(entities = [TrackSummary::class], version = 1, exportSchema = false)
abstract class TrackSummaryDatabase : RoomDatabase() {
    abstract fun trackSummaryDao() : TrackSummaryDao
}