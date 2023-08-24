package com.scrollz.golfai.utils

import androidx.compose.runtime.Immutable

@Immutable
sealed class Status {
    data object Success: Status()
    data object Loading: Status()
    data object Error: Status()
}
