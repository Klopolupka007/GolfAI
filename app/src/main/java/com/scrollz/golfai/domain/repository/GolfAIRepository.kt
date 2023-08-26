package com.scrollz.golfai.domain.repository

import com.scrollz.golfai.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface GolfAIRepository {
    fun getReports(): Flow<List<Report>>

    suspend fun getReport(id: Int): Report?

    suspend fun insertReport(report: Report)

    suspend fun deleteReport(id: Int)

}
