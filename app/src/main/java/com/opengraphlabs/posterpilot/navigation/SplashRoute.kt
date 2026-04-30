package com.opengraphlabs.posterpilot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.opengraphlabs.posterpilot.data.local.BusinessProfileState

@Composable
fun SplashRoute(
    profileState: BusinessProfileState,
    onProfileMissing: () -> Unit,
    onProfileReady: () -> Unit
) {
    LaunchedEffect(profileState) {
        if (profileState is BusinessProfileState.Ready) {
            if (profileState.isComplete) {
                onProfileReady()
            } else {
                onProfileMissing()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading PosterPilot AI",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
