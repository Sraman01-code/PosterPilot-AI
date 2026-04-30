package com.opengraphlabs.posterpilot.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsEvents
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsTracker
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.PosterCategory
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.renderer.TemplateRenderer
import com.opengraphlabs.posterpilot.core.theme.EyebrowStyle
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold
import com.opengraphlabs.posterpilot.core.ui.PosterTopBar
import com.opengraphlabs.posterpilot.data.templates.TemplateRepository

@Composable
fun HomeScreen(
    businessProfile: BusinessProfile?,
    analyticsTracker: AnalyticsTracker,
    onTemplateSelected: (String) -> Unit,
    onHistorySelected: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TemplateRepository(context.applicationContext) }
    var templates by remember { mutableStateOf(emptyList<PosterTemplate>()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository) {
        analyticsTracker.track(AnalyticsEvents.HomeViewed)
        runCatching {
            repository.loadTemplates()
        }.onSuccess { loadedTemplates ->
            templates = loadedTemplates
            loadError = null
        }.onFailure { throwable ->
            templates = emptyList()
            loadError = throwable.message ?: "Unable to load templates."
        }
        isLoading = false
    }

    PosterPilotScaffold(
        topBar = {
            PosterTopBar(
                eyebrow = "PosterPilot · Studio",
                title = businessProfile?.businessName ?: "Studio",
                actions = {
                    ArchiveAction(onClick = onHistorySelected)
                }
            )
        },
        horizontalPadding = 0.dp
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GreetingHero(businessProfile = businessProfile)
                Spacer(modifier = Modifier.height(36.dp))
            }

            when {
                isLoading -> item { LoadingState() }
                loadError != null -> item { ErrorState(message = loadError ?: "") }
                templates.isEmpty() -> item { EmptyState() }
                else -> {
                    PosterCategory.entries.forEach { category ->
                        val categoryTemplates = templates.filter { it.category == category }
                        if (categoryTemplates.isNotEmpty()) {
                            item(key = "section_${category.name}") {
                                CategorySection(
                                    category = category,
                                    templates = categoryTemplates,
                                    businessProfile = businessProfile,
                                    onTemplateSelected = onTemplateSelected
                                )
                                Spacer(modifier = Modifier.height(36.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun GreetingHero(businessProfile: BusinessProfile?) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
                text = "TONIGHT'S CANVAS",
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Pick a poster.\nMake it ",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 44.sp
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "yours",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = ".",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(28.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Text(
                text = businessProfile?.businessName?.let {
                    "Templates tuned for $it. Tap one to preview, edit copy, share to status."
                } ?: "Templates for India's local shops. Tap one to preview, edit copy, share.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun CategorySection(
    category: PosterCategory,
    templates: List<PosterTemplate>,
    businessProfile: BusinessProfile?,
    onTemplateSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Text(
            text = category.displayName.uppercase(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onBackground
        )
        Box(modifier = Modifier.weight(1f))
        Text(
            text = "${templates.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = category.tagline(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(modifier = Modifier.height(18.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = templates, key = { it.id }) { template ->
            TemplateTile(
                template = template,
                businessProfile = businessProfile,
                onClick = { onTemplateSelected(template.id) }
            )
        }
    }
}

@Composable
private fun TemplateTile(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    onClick: () -> Unit
) {
    val tileWidth = when (template.format) {
        PosterFormat.SQUARE_1_1 -> 220.dp
        PosterFormat.STORY_9_16 -> 156.dp
    }
    val tileAspect = when (template.format) {
        PosterFormat.SQUARE_1_1 -> 1f
        PosterFormat.STORY_9_16 -> 9f / 16f
    }

    Column(
        modifier = Modifier
            .width(tileWidth)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(tileAspect)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            TemplateRenderer(
                template = template,
                businessProfile = businessProfile,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = template.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${template.format.displayName} · ${template.format.aspectRatioLabel}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun ArchiveAction(onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val fg = MaterialTheme.colorScheme.onBackground
    val surface = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(surface)
            .border(1.dp, outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(
                width = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            val outer = Path().apply {
                moveTo(w * 0.18f, h * 0.30f)
                lineTo(w * 0.82f, h * 0.30f)
                lineTo(w * 0.82f, h * 0.82f)
                lineTo(w * 0.18f, h * 0.82f)
                close()
            }
            drawPath(outer, color = fg, style = stroke)
            val flap = Path().apply {
                moveTo(w * 0.18f, h * 0.30f)
                lineTo(w * 0.18f, h * 0.18f)
                lineTo(w * 0.82f, h * 0.18f)
                lineTo(w * 0.82f, h * 0.30f)
            }
            drawPath(flap, color = fg, style = stroke)
            drawLine(
                color = fg,
                start = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.55f),
                end = androidx.compose.ui.geometry.Offset(w * 0.64f, h * 0.55f),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 2.dp)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Setting the press…",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "TROUBLE",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "QUIET STUDIO",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "No templates loaded.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun PosterCategory.tagline(): String =
    when (this) {
        PosterCategory.FESTIVAL -> "Festival mood."
        PosterCategory.SALE_OFFER -> "Move stock fast."
        PosterCategory.NEW_ARRIVAL -> "Fresh on shelves."
        PosterCategory.THANK_YOU -> "A small thank you."
    }
