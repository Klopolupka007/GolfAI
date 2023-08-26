package com.scrollz.golfai.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "report")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = -1,
    val dateTime: String,
    val directory: String
)
