package com.willowvibe.agereveal.domain

import android.net.Uri
import java.time.LocalDate
import java.time.LocalTime
import java.util.Base64

/**
 * Generates and parses `agereveal://profile/...` deep links for viral profile sharing.
 *
 * Payload is a Base64-URL-encoded JSON blob containing birthDate, name, and optional birthTime.
 */
object ProfileDeepLinkGenerator {

    fun generate(birthDate: LocalDate, name: String = "", birthTime: LocalTime? = null): String {
        val payload = buildString {
            append("""{"d":"${birthDate}","n":"${name}"""")
            if (birthTime != null) append(""","t":"${birthTime}"""")
            append("}")
        }
        val encoded = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.toByteArray(Charsets.UTF_8))
        return "agereveal://profile/$encoded"
    }

    data class ParsedProfile(
        val birthDate: LocalDate,
        val name: String,
        val birthTime: LocalTime?,
    )

    fun parse(uri: Uri): ParsedProfile? {
        val encoded = uri.lastPathSegment ?: return null
        return runCatching {
            val json = Base64.getUrlDecoder().decode(encoded).toString(Charsets.UTF_8)
            val date = extractValue(json, "d")?.let { LocalDate.parse(it) } ?: return null
            val name = extractValue(json, "n") ?: ""
            val time = extractValue(json, "t")?.let { LocalTime.parse(it) }
            ParsedProfile(birthDate = date, name = name, birthTime = time)
        }.getOrNull()
    }

    private fun extractValue(json: String, key: String): String? {
        val regex = """"$key":"([^"]*+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }
}
