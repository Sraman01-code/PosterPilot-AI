package com.opengraphlabs.posterpilot.feature.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsEvents
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsTracker
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.renderer.TemplateRenderer
import com.opengraphlabs.posterpilot.core.theme.EyebrowStyle
import com.opengraphlabs.posterpilot.core.ui.PosterTopBar
import com.opengraphlabs.posterpilot.data.templates.TemplateRepository

@Composable
fun TemplatePreviewScreen(
    templateId: String,
    businessProfile: BusinessProfile?,
    analyticsTracker: AnalyticsTracker,
    onEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TemplateRepository(context.applicationContext) }
    var template by remember { mutableStateOf<PosterTemplate?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(templateId, repository) {
        runCatching {
            repository.getTemplate(templateId)
        }.onSuccess { loadedTemplate ->
            template = loadedTemplate
            loadError = null
            loadedTemplate?.let {
                analyticsTracker.track(
                    event = AnalyticsEvents.TemplateOpened,
                    params = mapOf(
                        "templateId" to it.id,
                        "category" to it.category.name,
                        "format" to it.format.name
                    )
                )
            }
        }.onFailure { throwable ->
            template = null
            loadError = throwable.message ?: "We couldn't open this template. Please try again."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            PosterTopBar(
                eyebrow = template?.category?.displayName ?: "Template",
                title = template?.title ?: "Preview",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val loadedTemplate = template
            if (!isLoading && loadedTemplate != null) {
                ActionBar(onEdit = { onEdit(loadedTemplate.id) })
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CenteredMessage("Loading template…")
                loadError != null -> CenteredMessage(loadError ?: "We couldn't open this template. Please try again.")
                template == null -> CenteredMessage("Template not found.")
                else -> PreviewBody(
                    template = template!!,
                    businessProfile = businessProfile
                )
            }
        }
    }
}

@Composable
private fun PreviewBody(
    template: PosterTemplate,
    businessProfile: BusinessProfile?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = template.format.aspectRatioLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Text(
                text = template.format.displayName.uppercase(),
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))

        PosterStage(template = template, businessProfile = businessProfile)

        Spacer(modifier = Modifier.height(28.dp))

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
                text = "ABOUT THIS POSTER",
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = template.title,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Tap Edit to update the headline, caption, and call to action. Your business name and brand colour are applied automatically.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
        )

        Spacer(modifier = Modifier.height(24.dp))
        FactRow(label = "FORMAT", value = "${template.format.displayName} (${template.format.aspectRatioLabel})")
        FactRow(label = "CATEGORY", value = template.category.displayName)
        FactRow(label = "BRAND COLOUR", value = businessProfile?.brandColorHex ?: "Default")

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun PosterStage(
    template: PosterTemplate,
    businessProfile: BusinessProfile?
) {
    val aspect = when (template.format) {
        PosterFormat.SQUARE_1_1 -> 1f
        PosterFormat.STORY_9_16 -> 9f / 16f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .let {
                    when (template.format) {
                        PosterFormat.SQUARE_1_1 -> it.fillMaxWidth().aspectRatio(aspect)
                        PosterFormat.STORY_9_16 -> it.fillMaxWidth(0.78f).aspectRatio(aspect)
                    }
                }
                .clip(RoundedCornerShape(18.dp))
        ) {
            TemplateRenderer(
                template = template,
                businessProfile = businessProfile,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}

@Composable
private fun ActionBar(onEdit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "READY",
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Customise this poster",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Button(
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                onClick = onEdit
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "→",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
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
