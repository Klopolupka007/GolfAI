package com.scrollz.golfai.data.repository

import com.scrollz.golfai.data.local.GolfAIDataBase
import com.scrollz.golfai.domain.model.Report
import com.scrollz.golfai.domain.repository.GolfAIRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GolfAIRepositoryImpl @Inject constructor(
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
}
