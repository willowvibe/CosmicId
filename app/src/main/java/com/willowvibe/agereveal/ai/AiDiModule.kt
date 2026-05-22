package com.willowvibe.agereveal.ai

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds the [AiService] interface.
 *
 * To swap in a real AI backend:
 * 1. Create a new class implementing [AiService] (e.g., `OpenAiServiceImpl`)
 * 2. Replace [NoOpAiServiceImpl] in the @Binds method below
 * 3. No changes needed in any consumer code
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiDiModule {

    @Binds
    @Singleton
    abstract fun bindAiService(impl: NoOpAiServiceImpl): AiService
}
