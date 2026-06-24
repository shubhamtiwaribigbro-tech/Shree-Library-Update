package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    val maxOutputTokens: Int? = 512
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askLibraryGuru(userPrompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback rule for when key is missing or is default placeholder
            return getFallbackResponse(userPrompt)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            systemInstruction = Content(parts = listOf(Part(
                text = "You are 'Library Guru', the helpful and friendly AI assistant for Shree Library, " +
                        "located in Mau, Uttar Pradesh, India. " +
                        "Shree Library facts: " +
                        "- 125 premium ergonomic chairs/seats with real-time tracking.\n" +
                        "- AC enabled study atmosphere.\n" +
                        "- High-speed unlimited free Wi-Fi, personal locker facilities, and 24x7 active timings.\n" +
                        "- Timing: Open 24 Hours a day, 7 days a week (24x7).\n" +
                        "- Membership Plans:\n" +
                        "  1. Basic Plan: Rs 300/month, 8-hour shift access, high-speed Wi-Fi, general seating.\n" +
                        "  2. Premium Plan: Rs 750/month, 12-hour shift access, personal safe locker, ergonomic chair set.\n" +
                        "  3. VIP Plan: Rs 1000/month, 24x7 full open facility, fixed reserved desk, unlimited tea/refreshment.\n" +
                        "Respond politely, warmly, and briefly in English, Hindi, or Hinglish (max 3-4 sentences)."
            )))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            responseText ?: "Hello! I am having a small connection issue. Shree Library in Mau offers 125 seats, 24x7 access, free high-speed Wi-Fi, AC, and plans starting from ₹300/month! How can I help you today?"
        } catch (e: Exception) {
            getFallbackResponse(userPrompt)
        }
    }

    private fun getFallbackResponse(prompt: String): String {
        val lowPrompt = prompt.lowercase()
        return when {
            lowPrompt.contains("seat") || lowPrompt.contains("जगह") || lowPrompt.contains("chair") -> {
                "Shree Library Mau features 125 premium ergonomic seats with real-time status tracking dashboards. You can select and book any available slot directly from the seat map."
            }
            lowPrompt.contains("fee") || lowPrompt.contains("price") || lowPrompt.contains("fees") || lowPrompt.contains("paisa") || lowPrompt.contains("charges") -> {
                "Our affordable membership plans are:\n• Basic: ₹300/mo (8-Hour Shift)\n• Premium: ₹750/mo (12-Hour Shift + Locker)\n• VIP: ₹1000/mo (24x7 Full Access + Fixed Desk + Tea)."
            }
            lowPrompt.contains("timing") || lowPrompt.contains("time") || lowPrompt.contains("open") || lowPrompt.contains("kab") -> {
                "Shree Library is open 24x7, 365 days a year! You can study anytime of the day or night in our fully air-conditioned environment."
            }
            else -> {
                "नमस्ते! I am Library Guru. Shree Library is located in Mau, offering a premium 24x7 study space with high speed Wi-Fi, ergonomic chairs, and personalized lockers. Plans start at just ₹300/month!"
            }
        }
    }
}
