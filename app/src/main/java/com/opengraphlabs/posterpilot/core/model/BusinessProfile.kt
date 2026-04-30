package com.opengraphlabs.posterpilot.core.model

data class BusinessProfile(
    val businessName: String,
    val category: BusinessCategory,
    val phone: String,
    val language: AppLanguage
)
