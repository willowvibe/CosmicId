package com.willowvibe.agereveal.ai

import java.time.LocalDate
import java.time.LocalTime

/**
 * Request/response models for the AI integration layer.
 *
 * All models are designed to be framework-agnostic so they can be serialized and
 * sent to remote AI services (OpenAI, Gemini, etc.) or used with on-device models.
 */

/** Context about the user needed for AI-powered astrological insights. */
data class AiUserContext(
    val name: String,
    val birthDate: LocalDate,
    val birthTime: LocalTime?,
    val westernSunSign: String,
    val westernMoonSign: String,
    val vedicRashi: String,
    val nakshatra: String,
    val nakshatraPada: String,
    val chineseZodiac: String,
    val chineseStemBranch: String,
    val currentDasha: String?,
)

/** AI-generated fortune/vibe check response. */
data class AiFortuneResponse(
    val headline: String,
    val body: String,
    val emoji: String,
    val moodKeyword: String,
)

/** AI-generated compatibility insight for two people. */
data class AiCompatibilityInsight(
    val headline: String,
    val detailedAnalysis: String,
    val strengths: List<String>,
    val challenges: List<String>,
    val cosmicAdvice: String,
)

/** AI-generated personalized transit forecast. */
data class AiTransitForecast(
    val periodLabel: String,
    val generalTheme: String,
    val career: String,
    val relationships: String,
    val health: String,
    val luckyDays: List<String>,
)
