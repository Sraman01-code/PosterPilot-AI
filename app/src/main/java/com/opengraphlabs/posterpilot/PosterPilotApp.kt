package com.opengraphlabs.posterpilot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.opengraphlabs.posterpilot.data.local.BusinessProfileState
import com.opengraphlabs.posterpilot.data.local.BusinessProfileStore
import com.opengraphlabs.posterpilot.navigation.PosterPilotNavGraph
import kotlinx.coroutines.launch

@Composable
fun PosterPilotApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val businessProfileStore = remember {
        BusinessProfileStore(context.applicationContext)
    }
    val profileState: BusinessProfileState by businessProfileStore.state.collectAsState(
        initial = BusinessProfileState.Loading
    )
    val readyState = profileState as? BusinessProfileState.Ready

    PosterPilotNavGraph(
        navController = navController,
        profileState = profileState,
        selectedLanguage = readyState?.selectedLanguage,
        businessProfile = readyState?.profile,
        onLanguageSelected = { language ->
            coroutineScope.launch {
                businessProfileStore.saveLanguage(language)
            }
        },
        onBusinessProfileSaved = { profile ->
            coroutineScope.launch {
                businessProfileStore.saveProfile(profile)
            }
        }
    )
}
