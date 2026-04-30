package com.opengraphlabs.posterpilot.feature.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.PosterDraft
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.renderer.TemplateRenderer
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold
import com.opengraphlabs.posterpilot.data.templates.TemplateRepository

@Composable
fun EditorScreen(
    templateId: String,
    businessProfile: BusinessProfile?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TemplateRepository(context.applicationContext) }
    var template by remember { mutableStateOf<PosterTemplate?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var draft by remember(templateId, businessProfile?.brandColorHex) {
        mutableStateOf(
            PosterDraft(
                templateId = templateId,
                themeColorHex = businessProfile?.brandColorHex
            )
        )
    }

    LaunchedEffect(templateId, repository) {
        template = repository.getTemplate(templateId)
        isLoading = false
    }

    PosterPilotScaffold {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                OutlinedButton(onClick = onBack) {
                    Text(text = "Back")
                }
            }

            if (isLoading) {
                item {
                    Text(
                        text = "Loading editor...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val loadedTemplate = template
                if (loadedTemplate == null) {
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
                            onDraftChanged = { draft = it }
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
    onDraftChanged: (PosterDraft) -> Unit
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
        TemplateRenderer(
            template = template,
            businessProfile = businessProfile,
            draft = draft,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colorPresets.forEach { colorHex ->
            FilterChip(
                selected = draft.themeColorHex == colorHex,
                onClick = { onDraftChanged(draft.copy(themeColorHex = colorHex)) },
                label = { Text(text = colorHex) }
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
        enabled = false,
        onClick = {}
    ) {
        Text(text = "Export coming in Phase 6")
    }
    Spacer(modifier = Modifier.height(28.dp))
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
