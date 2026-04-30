package com.opengraphlabs.posterpilot.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.opengraphlabs.posterpilot.core.model.PosterCategory
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold
import com.opengraphlabs.posterpilot.data.templates.TemplateRepository

@Composable
fun HomeScreen(
    businessProfile: BusinessProfile?,
    onTemplateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TemplateRepository(context.applicationContext) }
    var templates by remember { mutableStateOf(emptyList<PosterTemplate>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(repository) {
        templates = repository.loadTemplates()
        isLoading = false
    }

    PosterPilotScaffold {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "PosterPilot AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = businessProfile?.let {
                        "Templates for ${it.businessName}"
                    } ?: "Templates",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (isLoading) {
                item {
                    Text(
                        text = "Loading templates...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                PosterCategory.entries.forEach { category ->
                    val categoryTemplates = templates.filter { it.category == category }
                    if (categoryTemplates.isNotEmpty()) {
                        item {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(
                            items = categoryTemplates,
                            key = { it.id }
                        ) { template ->
                            TemplateCard(
                                template = template,
                                onClick = { onTemplateSelected(template.id) }
                            )
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
private fun TemplateCard(
    template: PosterTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = template.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${template.format.displayName} · ${template.format.aspectRatioLabel}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
