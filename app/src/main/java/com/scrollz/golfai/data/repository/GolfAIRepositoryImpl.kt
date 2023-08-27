package com.scrollz.golfai.data.repository

import android.net.Uri
import com.scrollz.golfai.data.aimodels.VideoProcessor
import com.scrollz.golfai.data.local.GolfAIDataBase
import com.scrollz.golfai.domain.model.Report
import com.scrollz.golfai.domain.repository.GolfAIRepository
import com.scrollz.golfai.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GolfAIRepositoryImpl @Inject constructor(
    private val videoProcessor: VideoProcessor,
    private val db: GolfAIDataBase
): GolfAIRepository {

    private val dao = db.golfAIDao()

    override fun getReports(): Flow<List<Report>> {
        return dao.getReports()
    }

    override suspend fun getReport(id: Int): Report? {
        return dao.getReport(id)
    }

    override suspend fun insertReport(report: Report) {
        dao.insertReport(report)
    }

    override suspend fun deleteReport(id: Int) {
        dao.deleteReport(id)
    }

    override suspend fun processVideo(videoUri: Uri, dateTime: String): Resource<Report> {
        return try {
            Resource.Success(videoProcessor.processVideo(videoUri, dateTime))
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
