package com.scrollz.golfai.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toFineDateTime(): String {
    val inputFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val date = inputFormat.parse(this)
    return date?.let { outputFormat.format(it) } ?: ""
}

fun String.toLastNumber(): String {
    val l = this.indexOfLast { it == '_' }
    val r = this.indexOfLast { it == '.' }
    return this.slice(l+1 until r)
}
