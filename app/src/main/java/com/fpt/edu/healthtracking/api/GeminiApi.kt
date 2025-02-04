package com.fpt.edu.healthtracking.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApi {
    @POST("{modelName}:generateContent")
    suspend fun generateContent(
        @Query("key") key: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)