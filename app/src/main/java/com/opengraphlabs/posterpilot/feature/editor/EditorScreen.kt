package com.opengraphlabs.posterpilot.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsEvents
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsTracker
import com.opengraphlabs.posterpilot.core.export.PosterBitmapExporter
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.PosterDraft
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.renderer.TemplateRenderer
import com.opengraphlabs.posterpilot.core.share.sharePoster
import com.opengraphlabs.posterpilot.core.theme.EyebrowStyle
import com.opengraphlabs.posterpilot.core.ui.PosterTopBar
import com.opengraphlabs.posterpilot.data.local.history.HistoryRepository
import com.opengraphlabs.posterpilot.data.remote.ai.AiCopyRepository
import com.opengraphlabs.posterpilot.data.remote.ai.AiCopySource
import com.opengraphlabs.posterpilot.data.templates.TemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val BrandColorPresets = listOf(
    "#E8A33D",
    "#B71C3A",
    "#0F766E",
    "#1D4ED8",
    "#7C3AED",
    "#0E0E16"
)

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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val loadedTemplate = template
            if (!isLoading && loadedTemplate != null && loadError == null) {
                ExportBar(
                    exportState = exportState,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CenterText("Setting the press…")
                loadError != null -> CenterText(loadError ?: "Unable to load editor.")
                template == null -> CenterText("Template not found")
                else -> EditorBody(
                    template = template!!,
                    businessProfile = businessProfile,
                    draft = draft,
                    aiCopyState = aiCopyState,
                    onDraftChanged = {
                        draft = it
                        exportState = ExportState.Idle
                        aiCopyState = AiCopyState.Idle
                    },
                    onGenerateAiCopy = {
                        val loaded = template ?: return@EditorBody
                        analyticsTracker.track(
                            event = AnalyticsEvents.AiCopyRequested,
                            params = mapOf(
                                "templateId" to loaded.id,
                                "category" to loaded.category.name
                            )
                        )
                        aiCopyState = AiCopyState.Loading
                        coroutineScope.launch {
                            val result = runCatching {
                                aiCopyRepository.generateCopy(
                                    template = loaded,
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
                                            "templateId" to loaded.id,
                                            "source" to aiResult.source.name
                                        )
                                    )
                                }
                                .onFailure { throwable ->
                                    val message = throwable.message
                                        ?: "AI copy failed. Keep editing manually."
                                    aiCopyState = AiCopyState.Error(message)
                                    analyticsTracker.track(
                                        event = AnalyticsEvents.AiCopyFailed,
                                        params = mapOf(
                                            "templateId" to loaded.id,
                                            "message" to message
                                        )
                                    )
                                }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorBody(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    draft: PosterDraft,
    aiCopyState: AiCopyState,
    onDraftChanged: (PosterDraft) -> Unit,
    onGenerateAiCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PreviewStage(template = template, businessProfile = businessProfile, draft = draft)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            CopySection(
                draft = draft,
                aiCopyState = aiCopyState,
                onDraftChanged = onDraftChanged,
                onGenerateAiCopy = onGenerateAiCopy
            )
            BrandSection(
                draft = draft,
                onDraftChanged = onDraftChanged
            )
            LogoSection(
                draft = draft,
                onDraftChanged = onDraftChanged
            )
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun PreviewStage(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    draft: PosterDraft
) {
    val aspect = when (template.format) {
        PosterFormat.SQUARE_1_1 -> 1f
        PosterFormat.STORY_9_16 -> 9f / 16f
    }
    val stageHeight = when (template.format) {
        PosterFormat.SQUARE_1_1 -> 240.dp
        PosterFormat.STORY_9_16 -> 300.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = stageHeight, max = stageHeight)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let {
                    when (template.format) {
                        PosterFormat.SQUARE_1_1 -> it
                        PosterFormat.STORY_9_16 -> it
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(aspect, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                TemplateRenderer(
                    template = template,
                    businessProfile = businessProfile,
                    draft = draft,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun CopySection(
    draft: PosterDraft,
    aiCopyState: AiCopyState,
    onDraftChanged: (PosterDraft) -> Unit,
    onGenerateAiCopy: () -> Unit
) {
    SectionHeader(label = "Copy", helper = aiHelperText(aiCopyState))
    Spacer(modifier = Modifier.height(14.dp))

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = aiCopyState !is AiCopyState.Loading,
        shape = RoundedCornerShape(14.dp),
        onClick = onGenerateAiCopy
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            Text(
                text = if (aiCopyState is AiCopyState.Loading) "Composing copy…" else "Generate AI copy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))
    FieldLabel("Headline")
    EditorTextField(
        value = draft.headline,
        onValueChange = { onDraftChanged(draft.copy(headline = it)) },
        placeholder = "Big festival hook",
        maxLines = 2
    )
    Spacer(modifier = Modifier.height(14.dp))
    FieldLabel("Caption")
    EditorTextField(
        value = draft.caption,
        onValueChange = { onDraftChanged(draft.copy(caption = it)) },
        placeholder = "Soft second line",
        maxLines = 3
    )
    Spacer(modifier = Modifier.height(14.dp))
    FieldLabel("Call-to-action")
    EditorTextField(
        value = draft.cta,
        onValueChange = { onDraftChanged(draft.copy(cta = it)) },
        placeholder = "Visit today",
        maxLines = 1
    )
}

@Composable
private fun BrandSection(
    draft: PosterDraft,
    onDraftChanged: (PosterDraft) -> Unit
) {
    Spacer(modifier = Modifier.height(28.dp))
    SectionHeader(label = "Brand")
    Spacer(modifier = Modifier.height(14.dp))
    FieldLabel("Theme colour")
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BrandColorPresets.forEach { colorHex ->
            EditorBrandSwatch(
                colorHex = colorHex,
                selected = draft.themeColorHex.equals(colorHex, ignoreCase = true),
                onClick = { onDraftChanged(draft.copy(themeColorHex = colorHex)) }
            )
        }
    }
}

@Composable
private fun LogoSection(
    draft: PosterDraft,
    onDraftChanged: (PosterDraft) -> Unit
) {
    Spacer(modifier = Modifier.height(28.dp))
    SectionHeader(label = "Logo")
    Spacer(modifier = Modifier.height(8.dp))
    EditorSlider(
        label = "Scale",
        valueLabel = "%.2fx".format(draft.logoScale),
        value = draft.logoScale,
        valueRange = 0.5f..2f,
        onValueChange = { onDraftChanged(draft.copy(logoScale = it)) }
    )
    EditorSlider(
        label = "X offset",
        valueLabel = "%+.0f".format(draft.logoOffsetX),
        value = draft.logoOffsetX,
        valueRange = -240f..240f,
        onValueChange = { onDraftChanged(draft.copy(logoOffsetX = it)) }
    )
    EditorSlider(
        label = "Y offset",
        valueLabel = "%+.0f".format(draft.logoOffsetY),
        value = draft.logoOffsetY,
        valueRange = -240f..240f,
        onValueChange = { onDraftChanged(draft.copy(logoOffsetY = it)) }
    )
}

@Composable
private fun SectionHeader(label: String, helper: String? = null) {
    Spacer(modifier = Modifier.height(20.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 1.dp)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Text(
            text = label.uppercase(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!helper.isNullOrBlank()) {
            Box(modifier = Modifier.weight(1f))
            Text(
                text = helper,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun EditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLines: Int
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        },
        shape = RoundedCornerShape(14.dp),
        maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun EditorBrandSwatch(colorHex: String, selected: Boolean, onClick: () -> Unit) {
    val swatchColor = remember(colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
            .getOrDefault(Color.LightGray)
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(swatchColor)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Text(
                text = "✓",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun EditorSlider(
    label: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(modifier = Modifier.weight(1f))
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun ExportBar(
    exportState: ExportState,
    onExport: () -> Unit,
    onShare: (File) -> Unit,
    onCreateAnother: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            when (exportState) {
                is ExportState.Success -> SuccessActions(
                    file = exportState.file,
                    onShare = onShare,
                    onCreateAnother = onCreateAnother
                )
                else -> PrimaryExportRow(exportState = exportState, onExport = onExport)
            }
        }
    }
}

@Composable
private fun PrimaryExportRow(
    exportState: ExportState,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exportEyebrow(exportState),
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = exportSubtitle(exportState),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }
        Button(
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(18.dp),
            enabled = exportState !is ExportState.Exporting,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ),
            onClick = onExport
        ) {
            Text(
                text = if (exportState is ExportState.Exporting) "Exporting…" else "Export PNG",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SuccessActions(
    file: File,
    onShare: (File) -> Unit,
    onCreateAnother: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Text(
            text = "POSTER PRINTED",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ),
            onClick = { onShare(file) }
        ) {
            Text(text = "Share", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(16.dp),
            onClick = onCreateAnother
        ) {
            Text(text = "Create another", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CenterText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

private fun aiHelperText(state: AiCopyState): String? =
    when (state) {
        AiCopyState.Idle -> null
        AiCopyState.Loading -> "Composing"
        is AiCopyState.Success -> state.message
        is AiCopyState.Error -> state.message
    }

private fun exportEyebrow(state: ExportState): String =
    when (state) {
        ExportState.Idle -> "READY"
        ExportState.Exporting -> "WORKING"
        is ExportState.Error -> "TROUBLE"
        is ExportState.Success -> "DONE"
    }

private fun exportSubtitle(state: ExportState): String =
    when (state) {
        ExportState.Idle -> "Export PNG"
        ExportState.Exporting -> "Saving image…"
        is ExportState.Error -> state.message
        is ExportState.Success -> "Poster ready"
    }
