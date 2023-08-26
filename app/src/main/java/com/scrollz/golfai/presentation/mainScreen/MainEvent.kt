package com.scrollz.golfai.presentation.mainScreen

import android.net.Uri

sealed class MainEvent {
    data class Process(val videoUri: Uri): MainEvent()
}
