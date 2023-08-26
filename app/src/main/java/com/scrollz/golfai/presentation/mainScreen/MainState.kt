package com.scrollz.golfai.presentation.mainScreen

import androidx.compose.runtime.Immutable
import com.scrollz.golfai.domain.model.Report
import com.scrollz.golfai.utils.Status

@Immutable
data class MainState(
    val screenStatus: Status = Status.Loading,

    val reports: List<Report> = emptyList(),
)
