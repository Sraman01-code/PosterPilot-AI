package com.opengraphlabs.posterpilot.feature.history

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsEvents
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsTracker
import com.opengraphlabs.posterpilot.core.share.sharePoster
import com.opengraphlabs.posterpilot.core.theme.EyebrowStyle
import com.opengraphlabs.posterpilot.core.ui.PosterTopBar
import com.opengraphlabs.posterpilot.data.local.history.ExportedPosterEntity
import com.opengraphlabs.posterpilot.data.local.history.HistoryRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    analyticsTracker: AnalyticsTracker,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { HistoryRepository(context.applicationContext) }
    val posters by repository.observeRecent().collectAsState(initial = emptyList())
    var shareError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        analyticsTracker.track(AnalyticsEvents.HistoryOpened)
    }

    Scaffold(
        topBar = {
            PosterTopBar(
                eyebrow = "Archive",
                title = "Your exports",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (posters.isEmpty()) {
                EmptyArchive()
            } else {
                ArchiveBody(
                    posters = posters,
                    shareError = shareError,
                    onShare = { poster ->
                        val file = File(poster.imagePath)
                        if (file.exists()) {
                            shareError = null
                            analyticsTracker.track(
                                event = AnalyticsEvents.HistoryShareOpened,
                                params = mapOf("posterId" to poster.id.toString())
                            )
                            sharePoster(context, file)
                        } else {
                            shareError = "That PNG is no longer on this device."
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ArchiveBody(
    posters: List<ExportedPosterEntity>,
    shareError: String?,
    onShare: (ExportedPosterEntity) -> Unit
) {
    val sections = remember(posters) { groupByDate(posters) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 8.dp,
            bottom = 32.dp
        )
    ) {
        item {
            ArchiveOverview(count = posters.size)
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (shareError != null) {
            item {
                ErrorBanner(message = shareError)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        sections.forEach { section ->
            item(key = "header_${section.label}") {
                SectionHeader(label = section.label, count = section.posters.size)
                Spacer(modifier = Modifier.height(14.dp))
            }

            val rows = section.posters.chunked(2)
            items(
                items = rows,
                key = { it.joinToString("_") { p -> p.id.toString() } }
            ) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    row.forEach { poster ->
                        Box(modifier = Modifier.weight(1f)) {
                            ArchiveTile(
                                poster = poster,
                                onShare = { onShare(poster) }
                            )
                        }
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "spacer_${section.label}") {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ArchiveOverview(count: Int) {
    Column {
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
                text = "PRESS ARCHIVE",
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (count == 1) "1 poster on file." else "$count posters on file.",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tap any tile to share again. Long-press the press, anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.size(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = count.toString(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun ArchiveTile(
    poster: ExportedPosterEntity,
    onShare: () -> Unit
) {
    val aspect = if (poster.format.contains("STORY")) 9f / 16f else 1f
    val outline = MaterialTheme.colorScheme.outline
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onShare)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .clip(RoundedCornerShape(20.dp))
                .background(surface)
                .border(1.dp, outline, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            PosterThumbnail(imagePath = poster.imagePath)
            ShareBadge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = poster.headline.ifBlank { poster.templateTitle },
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = poster.createdAt.formatTime(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun PosterThumbnail(imagePath: String) {
    val imageBitmap = remember(imagePath) {
        runCatching { BitmapFactory.decodeFile(imagePath)?.asImageBitmap() }.getOrNull()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap == null) {
            Text(
                text = "PNG MISSING",
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        } else {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Exported poster thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ShareBadge(modifier: Modifier = Modifier) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.92f))
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(
                width = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.5f, h * 0.78f)
                lineTo(w * 0.5f, h * 0.18f)
                moveTo(w * 0.22f, h * 0.42f)
                lineTo(w * 0.5f, h * 0.14f)
                lineTo(w * 0.78f, h * 0.42f)
            }
            drawPath(path = path, color = onBg, style = stroke)
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = "TROUBLE",
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun EmptyArchive() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val ink = MaterialTheme.colorScheme.onBackground
            Canvas(modifier = Modifier.size(28.dp)) {
                val w = size.width
                val h = size.height
                val stroke = Stroke(
                    width = 1.4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
                val box = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.16f, h * 0.36f)
                    lineTo(w * 0.16f, h * 0.84f)
                    lineTo(w * 0.84f, h * 0.84f)
                    lineTo(w * 0.84f, h * 0.36f)
                    moveTo(w * 0.08f, h * 0.20f)
                    lineTo(w * 0.92f, h * 0.20f)
                    lineTo(w * 0.92f, h * 0.36f)
                    lineTo(w * 0.08f, h * 0.36f)
                    close()
                    moveTo(w * 0.40f, h * 0.50f)
                    lineTo(w * 0.60f, h * 0.50f)
                }
                drawPath(path = box, color = ink, style = stroke)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "QUIET ARCHIVE",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nothing pressed yet.",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Export a poster and it will live here, ready to share again whenever you need it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private data class ArchiveSection(
    val label: String,
    val posters: List<ExportedPosterEntity>
)

private fun groupByDate(posters: List<ExportedPosterEntity>): List<ArchiveSection> {
    if (posters.isEmpty()) return emptyList()
    val now = Calendar.getInstance()
    val today = startOfDay(now.timeInMillis)
    val yesterday = today - DAY_MS
    val weekAgo = today - 6 * DAY_MS

    val buckets = linkedMapOf<String, MutableList<ExportedPosterEntity>>(
        "Today" to mutableListOf(),
        "Yesterday" to mutableListOf(),
        "This week" to mutableListOf(),
        "Earlier" to mutableListOf()
    )

    posters.forEach { poster ->
        val key = when {
            poster.createdAt >= today -> "Today"
            poster.createdAt >= yesterday -> "Yesterday"
            poster.createdAt >= weekAgo -> "This week"
            else -> "Earlier"
        }
        buckets.getValue(key).add(poster)
    }

    return buckets.entries
        .filter { it.value.isNotEmpty() }
        .map { ArchiveSection(label = it.key, posters = it.value) }
}

private fun startOfDay(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private const val DAY_MS = 24L * 60L * 60L * 1000L

private fun Long.formatTime(): String {
    val now = System.currentTimeMillis()
    val today = startOfDay(now)
    return if (this >= today) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
    } else {
        SimpleDateFormat("d MMM · h:mm a", Locale.getDefault()).format(Date(this))
    }
}
