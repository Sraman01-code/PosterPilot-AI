package com.opengraphlabs.posterpilot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.opengraphlabs.posterpilot.core.model.BusinessProfile

@Composable
fun SplashRoute(
    businessProfile: BusinessProfile?,
    onProfileMissing: () -> Unit,
    onProfileReady: () -> Unit
) {
    LaunchedEffect(businessProfile) {
        if (businessProfile == null) {
            onProfileMissing()
        } else {
            onProfileReady()
        }
    }
}
