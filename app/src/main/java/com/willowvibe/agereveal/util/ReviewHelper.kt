package com.willowvibe.agereveal.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Google Play's ReviewManager.
 *
 * Trigger with [maybePromptAfterShare] after a successful share. The prompt is shown:
 *  - Only once per install (tracked via [UserPreferencesRepository.reviewPrompted])
 *  - After the user has shared at least [SHARE_THRESHOLD] times (to avoid prompting
 *    users who have barely engaged with the app).
 *
 * Google Play's API silently no-ops when:
 *  - The Play Store is not available (e.g. sideloaded APK, emulator without Play Services)
 *  - The user has already rated the app
 *  - The user was prompted too recently by Play itself
 * So there is no harm in calling this even when we cannot show a dialog.
 */
@Singleton
class ReviewHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferencesRepository,
) {
    companion object {
        const val SHARE_THRESHOLD = 1 // Prompt after the first share (per spec: first share)
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Called from a ViewModel after a share completes.
     * [activity] is required — resolve it via `context as? Activity` at the call site.
     */
    fun maybePromptAfterShare(activity: Activity?) {
        if (activity == null) return
        scope.launch {
            val shareCount = prefs.incrementShareCount()
            val alreadyPrompted = prefs.reviewPrompted.first()
            if (alreadyPrompted || shareCount < SHARE_THRESHOLD) return@launch
            requestReviewFlow(activity)
        }
    }

    private fun requestReviewFlow(activity: Activity) {
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val info = task.result
            manager.launchReviewFlow(activity, info).addOnCompleteListener {
                // Whether the user rates or dismisses, mark as prompted to avoid re-showing.
                scope.launch { prefs.markReviewPrompted() }
            }
        }
    }
}
