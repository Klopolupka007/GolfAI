package com.scrollz.golfai.domain.repository

import android.net.Uri
import com.scrollz.golfai.domain.model.Report
import com.scrollz.golfai.utils.Resource
import kotlinx.coroutines.flow.Flow

interface GolfAIRepository {
    fun getReports(): Flow<List<Report>>

    suspend fun getReport(id: Int): Report?

    suspend fun insertReport(report: Report)

    suspend fun deleteReport(id: Int)

    suspend fun processVideo(videoUri: Uri, dateTime: String): Resource<Report>

}
