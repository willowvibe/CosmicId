package com.willowvibe.agereveal.domain

import android.content.Context
import com.willowvibe.agereveal.data.model.CelebrityMatch
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CelebrityMatchCalculator @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Returns up to [limit] celebrities who share the same month+day as [birthDate].
     * Results are sorted so that exact year matches come first, then by name.
     */
    fun findMatches(birthDate: LocalDate, limit: Int = 3): List<CelebrityMatch> {
        val json = runCatching {
            context.assets.open("celebrities.json")
                .bufferedReader()
                .use { it.readText() }
        }.getOrNull() ?: return emptyList()
        return Companion.parseMatches(json, birthDate, limit)
    }

    companion object {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        fun parseMatches(json: String, birthDate: LocalDate, limit: Int): List<CelebrityMatch> {
            val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
            val month = birthDate.monthValue
            val day = birthDate.dayOfMonth
            val userYear = birthDate.year

            val matches = mutableListOf<CelebrityMatch>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val name = obj.optString("name", "").takeIf { it.isNotEmpty() } ?: continue
                val dobStr = obj.optString("dob", "").takeIf { it.isNotEmpty() } ?: continue
                val date = runCatching { LocalDate.parse(dobStr, formatter) }.getOrNull() ?: continue
                if (date.monthValue == month && date.dayOfMonth == day) {
                    matches.add(
                        CelebrityMatch(
                            name = name,
                            birthDate = date,
                            category = obj.optString("category", "Celebrity"),
                        )
                    )
                }
            }

            return matches
                .sortedWith(
                    compareByDescending<CelebrityMatch> { it.birthDate.year == userYear }
                        .thenBy { it.birthDate.year }
                        .thenBy { it.name }
                )
                .take(limit)
        }
    }
}
