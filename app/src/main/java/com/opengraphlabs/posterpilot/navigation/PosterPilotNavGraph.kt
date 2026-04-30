package com.opengraphlabs.posterpilot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.opengraphlabs.posterpilot.core.model.AppLanguage
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.data.local.BusinessProfileState
import com.opengraphlabs.posterpilot.feature.editor.EditorScreen
import com.opengraphlabs.posterpilot.feature.home.HomeScreen
import com.opengraphlabs.posterpilot.feature.onboarding.BusinessSetupScreen
import com.opengraphlabs.posterpilot.feature.onboarding.LanguageSelectionScreen
import com.opengraphlabs.posterpilot.feature.onboarding.WelcomeScreen
import com.opengraphlabs.posterpilot.feature.templates.TemplatePreviewScreen

@Composable
fun PosterPilotNavGraph(
    navController: NavHostController,
    profileState: BusinessProfileState,
    selectedLanguage: AppLanguage?,
    businessProfile: BusinessProfile?,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBusinessProfileSaved: (BusinessProfile) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = PosterPilotRoute.Splash.route
    ) {
        composable(PosterPilotRoute.Splash.route) {
            SplashRoute(
                profileState = profileState,
                onProfileMissing = {
                    navController.navigate(PosterPilotRoute.Welcome.route) {
                        popUpTo(PosterPilotRoute.Splash.route) { inclusive = true }
                    }
                },
                onProfileReady = {
                    navController.navigate(PosterPilotRoute.Home.route) {
                        popUpTo(PosterPilotRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(PosterPilotRoute.Welcome.route) {
            WelcomeScreen(
                onContinue = {
                    navController.navigate(PosterPilotRoute.LanguageSelection.route)
                }
            )
        }

        composable(PosterPilotRoute.LanguageSelection.route) {
            LanguageSelectionScreen(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                onContinue = {
                    navController.navigate(PosterPilotRoute.BusinessSetup.route)
                }
            )
        }

        composable(PosterPilotRoute.BusinessSetup.route) {
            BusinessSetupScreen(
                selectedLanguage = selectedLanguage,
                onProfileSaved = { profile ->
                    onBusinessProfileSaved(profile)
                    navController.navigate(PosterPilotRoute.Home.route) {
                        popUpTo(PosterPilotRoute.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(PosterPilotRoute.Home.route) {
            HomeScreen(
                businessProfile = businessProfile,
                onTemplateSelected = { templateId ->
                    navController.navigate(PosterPilotRoute.TemplatePreview.createRoute(templateId))
                }
            )
        }

        composable(PosterPilotRoute.TemplatePreview.route) { backStackEntry ->
            TemplatePreviewScreen(
                templateId = backStackEntry.arguments
                    ?.getString(PosterPilotRoute.TemplatePreview.TemplateIdArg)
                    .orEmpty(),
                businessProfile = businessProfile,
                onEdit = { templateId ->
                    navController.navigate(PosterPilotRoute.Editor.createRoute(templateId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(PosterPilotRoute.Editor.route) { backStackEntry ->
            EditorScreen(
                templateId = backStackEntry.arguments
                    ?.getString(PosterPilotRoute.Editor.TemplateIdArg)
                    .orEmpty(),
                businessProfile = businessProfile,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
