package com.opengraphlabs.posterpilot.data.local

import com.opengraphlabs.posterpilot.core.model.AppLanguage
import com.opengraphlabs.posterpilot.core.model.BusinessProfile

data class InMemoryBusinessState(
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val businessProfile: BusinessProfile? = null
)
