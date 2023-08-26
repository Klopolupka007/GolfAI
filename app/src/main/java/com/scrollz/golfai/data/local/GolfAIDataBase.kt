package com.scrollz.golfai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.scrollz.golfai.domain.model.Report

@Database(
    entities = [Report::class],
    version = 1
)

abstract class GolfAIDataBase: RoomDatabase() {
    abstract fun golfAIDao(): GolfAIDao
    companion object {
        const val DATABASE_NAME = "golf_ai"
    }
}
