package com.opengraphlabs.posterpilot.navigation

sealed class PosterPilotRoute(val route: String) {
    data object Splash : PosterPilotRoute("splash")
    data object Welcome : PosterPilotRoute("welcome")
    data object LanguageSelection : PosterPilotRoute("language_selection")
    data object BusinessSetup : PosterPilotRoute("business_setup")
    data object Home : PosterPilotRoute("home")
}
