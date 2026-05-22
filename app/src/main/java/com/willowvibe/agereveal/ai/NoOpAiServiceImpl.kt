package com.willowvibe.agereveal.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op implementation of [AiService] that returns null for all requests.
 *
 * This is the default implementation injected until a real AI backend is integrated.
 * When switching to a real implementation, replace this binding in the Hilt module
 * without changing any consumer code.
 */
@Singleton
class NoOpAiServiceImpl @Inject constructor() : AiService {

    override val isAvailable: Boolean = false

    override suspend fun generateFortune(context: AiUserContext): AiFortuneResponse? = null

    override suspend fun generateCompatibilityInsight(
        userA: AiUserContext,
        userB: AiUserContext,
    ): AiCompatibilityInsight? = null

    override suspend fun generateTransitForecast(
        context: AiUserContext,
        period: String,
    ): AiTransitForecast? = null
}
