package com.scrollz.golfai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.scrollz.golfai.domain.model.Report
import kotlinx.coroutines.flow.Flow

@Dao
interface GolfAIDao {
    @Query("SELECT * FROM report")
    fun getReports(): Flow<List<Report>>

    @Query("SELECT * FROM report WHERE id = :id")
    suspend fun getReport(id: Int): Report?

    @Insert(entity = Report::class)
    suspend fun insertReport(report: Report)

    @Query("DELETE FROM report WHERE id = :id")
    suspend fun deleteReport(id: Int)

}
