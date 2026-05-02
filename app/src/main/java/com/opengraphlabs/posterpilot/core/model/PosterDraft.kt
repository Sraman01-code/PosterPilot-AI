package com.opengraphlabs.posterpilot.core.model

data class PosterDraft(
    val templateId: String,
    val headline: String = "",
    val caption: String = "",
    val cta: String = "",
    val themeColorHex: String? = null,
    val logoScale: Float = 1f,
    val logoOffsetX: Float = 0f,
    val logoOffsetY: Float = 0f
) {
    companion object {
        const val DefaultHeadline = "Your headline here"
        const val DefaultCaption = "Add a short supporting line."
        const val DefaultCta = "Visit today"

        fun defaultsFor(category: PosterCategory): PosterCopyDefaults = when (category) {
            PosterCategory.FESTIVAL -> PosterCopyDefaults(
                headline = "Festive greetings",
                caption = "Wishing you joy, prosperity, and warm celebrations.",
                cta = "Visit today"
            )
            PosterCategory.SALE_OFFER -> PosterCopyDefaults(
                headline = "Special offer",
                caption = "Selected items at special prices — for a limited time.",
                cta = "Shop now"
            )
            PosterCategory.NEW_ARRIVAL -> PosterCopyDefaults(
                headline = "Just arrived",
                caption = "Fresh picks now in store.",
                cta = "See in store"
            )
            PosterCategory.THANK_YOU -> PosterCopyDefaults(
                headline = "Thank you",
                caption = "Grateful for your continued support.",
                cta = "See you again"
            )
        }
    }
}

data class PosterCopyDefaults(
    val headline: String,
    val caption: String,
    val cta: String
)
