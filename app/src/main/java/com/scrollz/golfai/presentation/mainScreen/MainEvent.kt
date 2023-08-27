package com.scrollz.golfai.presentation.mainScreen

import android.net.Uri

sealed class MainEvent {
    data class ProcessVideo(val videoUri: Uri, val dateTime: String): MainEvent()
    data class DeleteReport(val id: Int?): MainEvent()
}
