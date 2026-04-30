package com.opengraphlabs.posterpilot.data.local.history

import android.content.Context
import com.opengraphlabs.posterpilot.core.model.PosterDraft
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.data.local.AppDatabaseProvider
import kotlinx.coroutines.flow.Flow
import java.io.File

class HistoryRepository(context: Context) {
    private val dao = AppDatabaseProvider.get(context).exportedPosterDao()

    fun observeRecent(): Flow<List<ExportedPosterEntity>> =
        dao.observeRecent()

    suspend fun saveExportedPoster(
        template: PosterTemplate,
        draft: PosterDraft,
        imageFile: File
    ) {
        dao.insert(
            ExportedPosterEntity(
                templateId = template.id,
                templateTitle = template.title,
                category = template.category.displayName,
                format = template.format.aspectRatioLabel,
                imagePath = imageFile.absolutePath,
                headline = draft.headline,
                caption = draft.caption,
                cta = draft.cta,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
