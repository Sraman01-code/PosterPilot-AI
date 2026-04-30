package com.opengraphlabs.posterpilot.data.remote.ai

data class AiCopyRequest(
    val language: String,
    val posterCategory: String,
    val businessName: String,
    val businessCategory: String,
    val occasion: String,
    val offerText: String,
    val tone: String
)

data class AiCopyResponse(
    val headline: String,
    val caption: String,
    val cta: String
)

data class AiCopyResult(
    val copy: AiCopyResponse,
    val source: AiCopySource
)

enum class AiCopySource {
    Backend,
    Mock
}
