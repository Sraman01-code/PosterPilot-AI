package com.opengraphlabs.posterpilot.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.model.AppLanguage
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold

@Composable
fun LanguageSelectionScreen(
    selectedLanguage: AppLanguage?,
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit
) {
    PosterPilotScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choose language",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = selectedLanguage == language,
                        onClick = { onLanguageSelected(language) },
                        label = { Text(text = language.displayName) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedLanguage != null,
                onClick = onContinue
            ) {
                Text(text = "Continue")
            }
        }
    }
}
