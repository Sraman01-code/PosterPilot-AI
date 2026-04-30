package com.opengraphlabs.posterpilot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.opengraphlabs.posterpilot.core.model.AppLanguage
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.navigation.PosterPilotNavGraph

@Composable
fun PosterPilotApp() {
    val navController = rememberNavController()
    var selectedLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var businessProfile by remember { mutableStateOf<BusinessProfile?>(null) }

    PosterPilotNavGraph(
        navController = navController,
        selectedLanguage = selectedLanguage,
        businessProfile = businessProfile,
        onLanguageSelected = { selectedLanguage = it },
        onBusinessProfileSaved = { businessProfile = it }
    )
}
