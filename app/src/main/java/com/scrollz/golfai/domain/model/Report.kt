package com.scrollz.golfai.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "report")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val dateTime: String,
    val elbowCornerErrorP1: Boolean = false,
    val elbowCornerErrorP7: Boolean = false,
    val kneeCornerErrorP1: Boolean = false,
    val kneeCornerErrorP7: Boolean = false,
    val headError: Boolean = false,
    val legsError: Boolean = false,
    val orientation: Int
)
