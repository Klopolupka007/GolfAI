package com.scrollz.golfai.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toFineDateTime(): String {
    val inputFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val date = inputFormat.parse(this)
    return date?.let { outputFormat.format(it) } ?: ""
}

fun String.toLastNumber(): String = this.drop(this.indexOfLast { it == '_' } + 1)
