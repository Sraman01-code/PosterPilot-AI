package com.opengraphlabs.posterpilot.feature.history

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsEvents
import com.opengraphlabs.posterpilot.core.analytics.AnalyticsTracker
import com.opengraphlabs.posterpilot.core.share.sharePoster
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold
import com.opengraphlabs.posterpilot.core.ui.PosterTopBar
import com.opengraphlabs.posterpilot.data.local.history.ExportedPosterEntity
import com.opengraphlabs.posterpilot.data.local.history.HistoryRepository
import java.io.File
import java.text.SimpleDateFormat
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

    PosterPilotScaffold(
        topBar = {
            PosterTopBar(
                eyebrow = "Archive",
                title = "Export history",
                onBack = onBack
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            shareError?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (posters.isEmpty()) {
                item {
                    Text(
                        text = "No exported posters yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(
                    items = posters,
                    key = { it.id }
                ) { poster ->
                    HistoryItem(
                        poster = poster,
                        onShare = {
                            val file = File(poster.imagePath)
                            if (file.exists()) {
                                shareError = null
                                analyticsTracker.track(
                                    event = AnalyticsEvents.HistoryShareOpened,
                                    params = mapOf("posterId" to poster.id.toString())
                                )
                                sharePoster(context, file)
                            } else {
                                shareError = "Exported PNG is no longer available on this device."
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun HistoryItem(
    poster: ExportedPosterEntity,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PosterThumbnail(imagePath = poster.imagePath)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poster.headline.ifBlank { poster.templateTitle },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = poster.templateTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${poster.category} - ${poster.format}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = poster.createdAt.formatDate(),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onShare) {
                    Text(text = "Share")
                }
            }
        }
    }
}

@Composable
private fun PosterThumbnail(imagePath: String) {
    val imageBitmap = remember(imagePath) {
        BitmapFactory.decodeFile(imagePath)?.asImageBitmap()
    }

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap == null) {
            Text(
                text = "PNG",
                style = MaterialTheme.typography.bodySmall
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

private fun Long.formatDate(): String =
    SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(this))
