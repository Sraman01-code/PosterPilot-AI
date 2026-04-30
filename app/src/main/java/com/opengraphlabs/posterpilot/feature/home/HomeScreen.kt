package com.opengraphlabs.posterpilot.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold

@Composable
fun HomeScreen(businessProfile: BusinessProfile?) {
    PosterPilotScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = businessProfile?.let {
                    "Ready to create posters for ${it.businessName}."
                } ?: "Business setup is not complete.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Poster ideas will appear here in Phase 2.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
