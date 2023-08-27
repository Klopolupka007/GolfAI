package com.scrollz.golfai.presentation.reportScreen

import java.io.File

sealed class ReportEvent {
    data class OpenImage(val image: File): ReportEvent()
    data object CloseImage: ReportEvent()

}
