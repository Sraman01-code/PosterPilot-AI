package com.opengraphlabs.posterpilot.data.local.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exported_posters")
data class ExportedPosterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: String,
    val templateTitle: String,
    val category: String,
    val format: String,
    val imagePath: String,
    val headline: String,
    val caption: String,
    val cta: String,
    val createdAt: Long
)
