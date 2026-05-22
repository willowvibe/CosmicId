package com.willowvibe.agereveal.ai

/**
 * Abstraction layer for AI-powered features.
 *
 * Implementations can use remote LLMs (OpenAI, Gemini), on-device models
 * (MediaPipe, TensorFlow Lite), or fall back to the no-op implementation
 * that returns empty/default results.
 *
 * All methods are suspend functions to support network calls.
 */
interface AiService {

    /** Whether this implementation is capable of generating real AI responses. */
    val isAvailable: Boolean

    /**
     * Generate a personalized daily fortune/vibe check based on the user's
     * full astrological profile. Returns null if the service is unavailable
     * or an error occurs.
     */
    suspend fun generateFortune(context: AiUserContext): AiFortuneResponse?

    /**
     * Generate a compatibility insight for two users based on their
     * Western, Vedic, and Chinese astrological profiles.
     */
    suspend fun generateCompatibilityInsight(
        userA: AiUserContext,
        userB: AiUserContext,
    ): AiCompatibilityInsight?

    /**
     * Generate a personalized transit forecast for the given period
     * (e.g., "today", "this week", "this month").
     */
    suspend fun generateTransitForecast(
        context: AiUserContext,
        period: String,
    ): AiTransitForecast?
}
