package com.scrollz.golfai.presentation.mainScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollz.golfai.data.aimodels.VideoProcessor
import com.scrollz.golfai.domain.repository.GolfAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val videoProcessor: VideoProcessor,
    private val repository: GolfAIRepository
): ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        getReports()
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.Process -> {
                viewModelScope.launch(Dispatchers.Default) {

                    videoProcessor.processVideo(event.videoUri)

                }
            }
        }
    }

    private fun getReports() {
        repository.getReports().onEach { reports ->
            _state.update { it.copy(reports = reports) }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    private fun deleteReport(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReport(id)
        }
    }

}
