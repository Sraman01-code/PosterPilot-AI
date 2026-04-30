package com.opengraphlabs.posterpilot.feature.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import com.opengraphlabs.posterpilot.core.ui.PosterTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsEvents
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsTracker
import com.opengraphlabs.posterpilot.core.export.PosterBitmapExporter
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.PosterDraft
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.renderer.TemplateRendererPreviewFrame
import com.opengraphlabs.posterpilot.core.share.sharePoster
import com.opengraphlabs.posterpilot.core.ui.ColorSwatch
import com.opengraphlabs.posterpilot.data.local.history.HistoryRepository
import com.opengraphlabs.posterpilot.data.remote.ai.AiCopyRepository
import com.opengraphlabs.posterpilot.data.remote.ai.AiCopySource
import com.opengraphlabs.posterpilot.data.templates.TemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private sealed interface ExportState {
    data object Idle : ExportState
    data object Exporting : ExportState
    data class Success(val file: File) : ExportState
    data class Error(val message: String) : ExportState
}

private sealed interface AiCopyState {
    data object Idle : AiCopyState
    data object Loading : AiCopyState
    data class Success(val message: String) : AiCopyState
    data class Error(val message: String) : AiCopyState
}

@Composable
fun EditorScreen(
    templateId: String,
    businessProfile: BusinessProfile?,
    analyticsTracker: AnalyticsTracker,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { TemplateRepository(context.applicationContext) }
    val exporter = remember { PosterBitmapExporter(context.applicationContext) }
    val historyRepository = remember { HistoryRepository(context.applicationContext) }
    val aiCopyRepository = remember { AiCopyRepository() }
    var template by remember { mutableStateOf<PosterTemplate?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var exportState by remember { mutableStateOf<ExportState>(ExportState.Idle) }
    var aiCopyState by remember { mutableStateOf<AiCopyState>(AiCopyState.Idle) }
    var draft by remember(templateId, businessProfile?.brandColorHex) {
        mutableStateOf(
            PosterDraft(
                templateId = templateId,
                themeColorHex = businessProfile?.brandColorHex
            )
        )
    }

    LaunchedEffect(templateId, repository) {
        runCatching {
            repository.getTemplate(templateId)
        }.onSuccess { loadedTemplate ->
            template = loadedTemplate
            loadError = null
            loadedTemplate?.let {
                analyticsTracker.track(
                    event = AnalyticsEvents.EditorOpened,
                    params = mapOf(
                        "templateId" to it.id,
                        "category" to it.category.name,
                        "format" to it.format.name
                    )
                )
            }
        }.onFailure { throwable ->
            template = null
            loadError = throwable.message ?: "Unable to load editor."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            PosterTopBar(
                eyebrow = template?.category?.displayName ?: "Editor",
                title = template?.title ?: "Edit poster",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                item {
                    Text(
                        text = "Loading editor...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val loadedTemplate = template
                if (loadError != null) {
                    item {
                        Text(
                            text = loadError ?: "Unable to load editor.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else if (loadedTemplate == null) {
                    item {
                        Text(
                            text = "Template not found",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    item {
                        EditorContent(
                            template = loadedTemplate,
                            businessProfile = businessProfile,
                            draft = draft,
                            exportState = exportState,
                            aiCopyState = aiCopyState,
                            onDraftChanged = {
                                draft = it
                                exportState = ExportState.Idle
                                aiCopyState = AiCopyState.Idle
                            },
                            onGenerateAiCopy = {
                                analyticsTracker.track(
                                    event = AnalyticsEvents.AiCopyRequested,
                                    params = mapOf(
                                        "templateId" to loadedTemplate.id,
                                        "category" to loadedTemplate.category.name
                                    )
                                )
                                aiCopyState = AiCopyState.Loading
                                coroutineScope.launch {
                                    val result = runCatching {
                                        aiCopyRepository.generateCopy(
                                            template = loadedTemplate,
                                            businessProfile = businessProfile
                                        )
                                    }

                                    result
                                        .onSuccess { aiResult ->
                                            draft = draft.copy(
                                                headline = aiResult.copy.headline,
                                                caption = aiResult.copy.caption,
                                                cta = aiResult.copy.cta
                                            )
                                            aiCopyState = when (aiResult.source) {
                                                AiCopySource.Backend -> AiCopyState.Success("AI copy applied.")
                                                AiCopySource.Mock -> AiCopyState.Success("Sample copy applied.")
                                            }
                                            analyticsTracker.track(
                                                event = AnalyticsEvents.AiCopySuccess,
                                                params = mapOf(
                                                    "templateId" to loadedTemplate.id,
                                                    "source" to aiResult.source.name
                                                )
                                            )
                                        }
                                        .onFailure { throwable ->
                                            val message = throwable.message
                                                ?: "AI copy failed. You can keep editing manually."
                                            aiCopyState = AiCopyState.Error(message)
                                            analyticsTracker.track(
                                                event = AnalyticsEvents.AiCopyFailed,
                                                params = mapOf(
                                                    "templateId" to loadedTemplate.id,
                                                    "message" to message
                                                )
                                            )
                                        }
                                }
                            },
                            onExport = {
                                analyticsTracker.track(
                                    event = AnalyticsEvents.ExportStarted,
                                    params = mapOf(
                                        "templateId" to loadedTemplate.id,
                                        "format" to loadedTemplate.format.name
                                    )
                                )
                                exportState = ExportState.Exporting
                                coroutineScope.launch {
                                    exportState = runCatching {
                                        val file = withContext(Dispatchers.IO) {
                                            val exportedFile = exporter.export(
                                                template = loadedTemplate,
                                                businessProfile = businessProfile,
                                                draft = draft
                                            )
                                            historyRepository.saveExportedPoster(
                                                template = loadedTemplate,
                                                draft = draft,
                                                imageFile = exportedFile
                                            )
                                            exportedFile
                                        }
                                        analyticsTracker.track(
                                            event = AnalyticsEvents.ExportSuccess,
                                            params = mapOf(
                                                "templateId" to loadedTemplate.id,
                                                "format" to loadedTemplate.format.name
                                            )
                                        )
                                        ExportState.Success(file)
                                    }.getOrElse { throwable ->
                                        val message = throwable.message ?: "Export failed"
                                        analyticsTracker.track(
                                            event = AnalyticsEvents.ExportFailed,
                                            params = mapOf(
                                                "templateId" to loadedTemplate.id,
                                                "message" to message
                                            )
                                        )
                                        ExportState.Error(message)
                                    }
                                }
                            },
                            onShare = { file ->
                                analyticsTracker.track(
                                    event = AnalyticsEvents.ShareSheetOpened,
                                    params = mapOf("templateId" to loadedTemplate.id)
                                )
                                sharePoster(context, file)
                            },
                            onCreateAnother = onBack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorContent(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    draft: PosterDraft,
    exportState: ExportState,
    aiCopyState: AiCopyState,
    onDraftChanged: (PosterDraft) -> Unit,
    onGenerateAiCopy: () -> Unit,
    onExport: () -> Unit,
    onShare: (File) -> Unit,
    onCreateAnother: () -> Unit
) {
    val colorPresets = listOf("#F7B733", "#10B981", "#2563EB", "#DC2626", "#7C3AED")

    Text(
        text = "Edit poster",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Preview updated live",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        TemplateRendererPreviewFrame(
            template = template,
            businessProfile = businessProfile,
            draft = draft,
            maxPreviewHeight = template.editorPreviewMaxHeight(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = aiCopyState !is AiCopyState.Loading,
        onClick = onGenerateAiCopy
    ) {
        Text(
            text = if (aiCopyState is AiCopyState.Loading) {
                "Generating AI copy..."
            } else {
                "Generate AI Copy"
            }
        )
    }
    when (aiCopyState) {
        AiCopyState.Idle -> Unit
        AiCopyState.Loading -> Text(
            text = "Requesting headline, caption, and CTA...",
            style = MaterialTheme.typography.bodyMedium
        )
        is AiCopyState.Success -> Text(
            text = aiCopyState.message,
            style = MaterialTheme.typography.bodyMedium
        )
        is AiCopyState.Error -> Text(
            text = aiCopyState.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.headline,
        onValueChange = { onDraftChanged(draft.copy(headline = it)) },
        label = { Text(text = "Headline") },
        maxLines = 2
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.caption,
        onValueChange = { onDraftChanged(draft.copy(caption = it)) },
        label = { Text(text = "Caption") },
        maxLines = 3
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.cta,
        onValueChange = { onDraftChanged(draft.copy(cta = it)) },
        label = { Text(text = "CTA") },
        maxLines = 1
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Theme color",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        colorPresets.forEach { colorHex ->
            ColorSwatch(
                colorHex = colorHex,
                selected = draft.themeColorHex == colorHex,
                onClick = { onDraftChanged(draft.copy(themeColorHex = colorHex)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    LogoSlider(
        label = "Logo scale",
        value = draft.logoScale,
        valueRange = 0.5f..2f,
        onValueChange = { onDraftChanged(draft.copy(logoScale = it)) }
    )
    LogoSlider(
        label = "Logo X offset",
        value = draft.logoOffsetX,
        valueRange = -240f..240f,
        onValueChange = { onDraftChanged(draft.copy(logoOffsetX = it)) }
    )
    LogoSlider(
        label = "Logo Y offset",
        value = draft.logoOffsetY,
        valueRange = -240f..240f,
        onValueChange = { onDraftChanged(draft.copy(logoOffsetY = it)) }
    )

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = exportState !is ExportState.Exporting,
        onClick = onExport
    ) {
        Text(
            text = if (exportState is ExportState.Exporting) {
                "Exporting..."
            } else {
                "Export PNG"
            }
        )
    }

    when (exportState) {
        ExportState.Idle -> Unit
        ExportState.Exporting -> Text(
            text = "Creating PNG...",
            style = MaterialTheme.typography.bodyMedium
        )
        is ExportState.Error -> Text(
            text = exportState.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        is ExportState.Success -> ExportSuccessActions(
            file = exportState.file,
            onShare = onShare,
            onCreateAnother = onCreateAnother
        )
    }

    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
private fun ExportSuccessActions(
    file: File,
    onShare: (File) -> Unit,
    onCreateAnother: () -> Unit
) {
    Text(
        text = "PNG exported successfully.",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onShare(file) }
    ) {
        Text(text = "Share")
    }
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCreateAnother
    ) {
        Text(text = "Create another")
    }
}

@Composable
private fun LogoSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            text = "$label: ${value.formatForLabel()}",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

private fun Float.formatForLabel(): String =
    "%.1f".format(this)

private fun PosterTemplate.editorPreviewMaxHeight() =
    when (format) {
        PosterFormat.SQUARE_1_1 -> 280.dp
        PosterFormat.STORY_9_16 -> 300.dp
    }

