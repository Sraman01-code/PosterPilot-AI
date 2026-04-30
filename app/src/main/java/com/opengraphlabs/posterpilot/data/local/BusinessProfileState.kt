package com.opengraphlabs.posterpilot.data.local

import com.opengraphlabs.posterpilot.core.model.AppLanguage
import com.opengraphlabs.posterpilot.core.model.BusinessProfile

sealed interface BusinessProfileState {
    data object Loading : BusinessProfileState

    data class Ready(
        val selectedLanguage: AppLanguage?,
        val profile: BusinessProfile?,
        val onboardingCompleted: Boolean
    ) : BusinessProfileState {
        val isComplete: Boolean = onboardingCompleted && profile != null
    }
}
