package com.scrollz.golfai.presentation.reportScreen

import androidx.compose.runtime.Immutable
import com.scrollz.golfai.utils.Status
import java.io.File

@Immutable
data class ReportState(
    val screenStatus: Status = Status.Loading,

    val images: List<File> = emptyList(),
    val isImageOpen: Boolean = false,
    val openImage: File? = null,

    val id: Int = -1,
    val dateTime: String = "",
    val orientation: Int = -10,
    val elbowCornerErrorP1: Boolean = false,
    val elbowCornerErrorP7: Boolean = false,
    val kneeCornerErrorP1: Boolean = false,
    val kneeCornerErrorP7: Boolean = false,
    val headError: Boolean = false,
    val legsError: Boolean = false
)
