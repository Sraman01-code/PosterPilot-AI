package com.opengraphlabs.posterpilot.data.remote.ai

import com.opengraphlabs.posterpilot.BuildConfig
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AiCopyRepository(
    baseUrl: String = BuildConfig.AI_BASE_URL
) {
    private val normalizedBaseUrl = baseUrl.trim()
    private val api: AiCopyApi? = if (normalizedBaseUrl.isBlank()) {
        null
    } else {
        runCatching {
            Retrofit.Builder()
                .baseUrl(normalizedBaseUrl.withTrailingSlash())
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(8, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(8, TimeUnit.SECONDS)
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AiCopyApi::class.java)
        }.getOrNull()
    }

    suspend fun generateCopy(
        template: PosterTemplate,
        businessProfile: BusinessProfile?
    ): AiCopyResult {
        val request = AiCopyRequest(
            language = businessProfile?.language?.apiCode ?: "en",
            posterCategory = template.category.name,
            businessName = businessProfile?.businessName.orEmpty().ifBlank { "Your Business" },
            businessCategory = businessProfile?.category?.displayName.orEmpty().ifBlank { "Local Business" },
            occasion = template.title,
            offerText = "",
            tone = "promotional"
        )

        val configuredApi = api ?: return AiCopyResult(
            copy = mockCopy(request),
            source = AiCopySource.Mock
        )

        return AiCopyResult(
            copy = configuredApi.generateCopy(request),
            source = AiCopySource.Backend
        )
    }

    private fun mockCopy(request: AiCopyRequest): AiCopyResponse =
        AiCopyResponse(
            headline = "${request.businessName} Special Offer",
            caption = "Create beautiful posts for ${request.businessName} with fresh deals for your customers.",
            cta = "Contact us now"
        )

    private fun String.withTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}
