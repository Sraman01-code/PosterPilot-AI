package com.opengraphlabs.posterpilot.data.remote.ai

import retrofit2.http.Body
import retrofit2.http.POST

interface AiCopyApi {
    @POST("ai/copy")
    suspend fun generateCopy(@Body request: AiCopyRequest): AiCopyResponse
}
