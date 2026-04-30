package com.opengraphlabs.posterpilot.feature.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.renderer.TemplateRenderer
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold
import com.opengraphlabs.posterpilot.data.templates.TemplateRepository

@Composable
fun TemplatePreviewScreen(
    templateId: String,
    businessProfile: BusinessProfile?,
    onEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TemplateRepository(context.applicationContext) }
    var template by remember { mutableStateOf<PosterTemplate?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(templateId, repository) {
        template = repository.getTemplate(templateId)
        isLoading = false
    }

    PosterPilotScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            Button(onClick = onBack) {
                Text(text = "Back")
            }

            if (isLoading) {
                Text(
                    text = "Loading template...",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                val loadedTemplate = template
                if (loadedTemplate == null) {
                    Text(
                        text = "Template not found",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    TemplatePreviewContent(
                        template = loadedTemplate,
                        businessProfile = businessProfile,
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.TemplatePreviewContent(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    onEdit: (String) -> Unit
) {
    Text(
        text = template.title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = template.category.displayName,
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = "${template.format.displayName} - ${template.format.aspectRatioLabel}",
        style = MaterialTheme.typography.bodyMedium
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = template.previewMaxHeight()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        TemplateRenderer(
            template = template,
            businessProfile = businessProfile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }

    Spacer(modifier = Modifier.weight(1f))
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEdit(template.id) }
    ) {
        Text(text = "Edit poster")
    }
}

private fun PosterTemplate.previewMaxHeight() =
    when (format) {
        PosterFormat.SQUARE_1_1 -> 300.dp
        PosterFormat.STORY_9_16 -> 340.dp
    }
