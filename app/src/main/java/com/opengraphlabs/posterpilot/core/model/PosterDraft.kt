package com.opengraphlabs.posterpilot.core.model

data class PosterDraft(
    val templateId: String,
    val headline: String = DefaultHeadline,
    val caption: String = DefaultCaption,
    val cta: String = DefaultCta,
    val themeColorHex: String? = null,
    val logoScale: Float = 1f,
    val logoOffsetX: Float = 0f,
    val logoOffsetY: Float = 0f
) {
    companion object {
        const val DefaultHeadline = "Special Offer Today"
        const val DefaultCaption = "Create beautiful posts for your business in seconds."
        const val DefaultCta = "Contact us now"
    }
}
