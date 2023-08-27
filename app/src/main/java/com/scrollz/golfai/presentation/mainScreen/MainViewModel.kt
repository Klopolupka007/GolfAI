package com.scrollz.golfai.presentation.mainScreen

import android.net.Uri
import android.view.Surface.OutOfResourcesException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollz.golfai.domain.repository.GolfAIRepository
import com.scrollz.golfai.utils.Resource
import com.scrollz.golfai.utils.Status
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
    private val repository: GolfAIRepository
): ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        getReports()
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.ProcessVideo -> processVideo(event.videoUri, event.dateTime)
            is MainEvent.DeleteReport -> deleteReport(event.id)
        }
    }

    private fun getReports() {
        repository.getReports().onEach { reports ->
            _state.update { it.copy(reports = reports) }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    private fun deleteReport(id: Int?) {
        id?.let {
            viewModelScope.launch(Dispatchers.IO) {
                repository.deleteReport(id)
            }
        }
    }

    private fun processVideo(videoUri: Uri, dateTime: String) {
        viewModelScope.launch {
            _state.update { it.copy(screenStatus = Status.Loading) }
            val result = repository.processVideo(videoUri, dateTime)

            if (result is Resource.Success && result.data != null) {
                repository.insertReport(result.data)
                _state.update { it.copy(screenStatus = Status.Success) }
            } else {
                if (result.error is OutOfResourcesException) {
                    _state.update { it.copy(screenStatus = Status.Success) }
                } else {
                    _state.update { it.copy(screenStatus = Status.Success) }
                }
            }
        }
    }

}
