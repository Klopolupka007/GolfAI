package com.scrollz.golfai.presentation.reportScreen

import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollz.golfai.domain.repository.GolfAIRepository
import com.scrollz.golfai.utils.Status
import com.scrollz.golfai.utils.toFineDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: GolfAIRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    init {
        getReport(savedStateHandle.get<Int>("reportID") ?: -1)
    }

    fun onEvent(event: ReportEvent) {
        when (event) {
            is ReportEvent.CloseImage -> _state.update { it.copy(isImageOpen = false) }
            is ReportEvent.OpenImage -> _state.update {
                it.copy(isImageOpen = true, openImage = event.image)
            }
        }
    }

    private fun getReport(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val report = repository.getReport(id)

            if (report != null) {
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "GolfAI Results"
                )
                val images = if (directory.exists() && directory.isDirectory) {
                    val files = directory.listFiles()
                    if (files != null && files.isNotEmpty()) {
                        files.toList().filter { it.name.contains(report.dateTime) }
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                _state.update { state ->
                    state.copy(
                        id = report.id ?: -1,
                        dateTime = report.dateTime.toFineDateTime(),
                        orientation = report.orientation,
                        elbowCornerErrorP1 = report.elbowCornerErrorP1,
                        elbowCornerErrorP7 = report.elbowCornerErrorP7,
                        kneeCornerErrorP1 = report.kneeCornerErrorP1,
                        kneeCornerErrorP7 = report.kneeCornerErrorP7,
                        headError = report.headError,
                        legsError = report.legsError,
                        images = images,
                        screenStatus = Status.Success,
                    )
                }
            } else {
                _state.update { it.copy(screenStatus = Status.Error) }
            }
        }
    }

}
