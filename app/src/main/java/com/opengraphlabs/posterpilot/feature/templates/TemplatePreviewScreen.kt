package com.opengraphlabs.posterpilot.feature.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.opengraphlabs.posterpilot.core.renderer.TemplateRendererPreviewFrame
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

    Scaffold(
        bottomBar = {
            val loadedTemplate = template
            if (!isLoading && loadedTemplate != null) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    onClick = { onEdit(loadedTemplate.id) }
                ) {
                    Text(text = "Edit poster")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onBack) {
                    Text(text = "Back")
                }
            }

            if (isLoading) {
                item {
                    Text(
                        text = "Loading template...",
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
                        TemplatePreviewContent(
                            template = loadedTemplate,
                            businessProfile = businessProfile
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplatePreviewContent(
    template: PosterTemplate,
    businessProfile: BusinessProfile?
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        TemplateRendererPreviewFrame(
            template = template,
            businessProfile = businessProfile,
            maxPreviewHeight = template.previewMaxHeight(),
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun PosterTemplate.previewMaxHeight() =
    when (format) {
        PosterFormat.SQUARE_1_1 -> 360.dp
        PosterFormat.STORY_9_16 -> 420.dp
    }
