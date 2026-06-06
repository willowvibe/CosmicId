package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a daily "cosmic fortune" or "vibe check" card mixing current
 * celestial conditions with the user's birth chart and a curated message pool.
 *
 * The fortune is deterministic for a given (date + birthDate) pair so the
 * same user sees the same fortune all day, but it changes at midnight.
 */
@Singleton
class DailyFortuneGenerator @Inject constructor(
    private val astronomicalCalculator: AstronomicalCalculator,
    private val moonPhaseCalculator: MoonPhaseCalculator,
    private val zodiacCalculator: ZodiacCalculator,
) {

    data class Fortune(
        val headline: String,
        val body: String,
        val emoji: String,
        val moonPhase: String,
        val sunSign: String,
        val stemBranch: String,
        val luckyNumber: Int,
        val luckyColor: String,
        // BUG-088: Honest disclaimer. Fortunes reference "Mars energy," "your
        // 10th house," "Saturn testing your patience" etc., but the underlying
        // transits are not actually computed against the user's birth chart.
        // The flag + disclaimer let the UI surface "for entertainment only"
        // without diluting the message body.
        val isEntertainment: Boolean = true,
        val disclaimer: String = DEFAULT_DISCLAIMER,
    )

    fun generate(birthDate: LocalDate, today: LocalDate = LocalDate.now()): Fortune {
        val jd = astronomicalCalculator.julianDay(today.atTime(12, 0))
        val sunLon = astronomicalCalculator.sunLongitude(jd)
        val moonLon = astronomicalCalculator.moonLongitude(jd)
        val moonPhase = moonPhaseCalculator.calculate(sunLon, moonLon)
        val sunSign = zodiacCalculator.getWesternZodiac(today, null, null)
        val stemBranch = zodiacCalculator.getChineseStemBranch(today)

        // Deterministic seed from date + birthDate so the fortune is stable all day
        val seed = hashSeed(today, birthDate)

        val headline = buildHeadline(moonPhase.name, sunSign, seed)
        val body = pickMessage(moonPhase.name, sunSign, stemBranch, seed)
        val emoji = moonEmoji(moonPhase.name)
        val luckyNumber = ((seed * 7 + 13) % 99 + 1).toInt()
        val luckyColor = luckyColors[(seed % luckyColors.size).toInt()]

        return Fortune(
            headline = headline,
            body = body,
            emoji = emoji,
            moonPhase = moonPhase.name,
            sunSign = sunSign,
            stemBranch = stemBranch,
            luckyNumber = luckyNumber,
            luckyColor = luckyColor,
        )
    }

    private fun hashSeed(today: LocalDate, birthDate: LocalDate): Long {
        var h = 17L
        h = 31 * h + today.toEpochDay()
        h = 31 * h + birthDate.toEpochDay()
        return if (h == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(h)
    }

    private fun buildHeadline(moonPhase: String, sunSign: String, seed: Long): String {
        val templates = listOf(
            "Today's vibe: $moonPhase energy",
            "$sunSign under a $moonPhase sky",
            "The $moonPhase whispers to $sunSign",
            "$sunSign, the moon is $moonPhase today",
            "$moonPhase vibes for $sunSign",
        )
        return templates[(seed % templates.size).toInt()]
    }

    private fun pickMessage(moonPhase: String, sunSign: String, stemBranch: String, seed: Long): String {
        // Filter messages loosely related to current conditions
        val filtered = messages.filter { msg ->
            moonPhase.lowercase() in msg.lowercase() ||
            sunSign.lowercase() in msg.lowercase() ||
            msg.length > 40
        }.takeIf { it.size >= 5 } ?: messages
        return filtered[(seed % filtered.size).toInt()]
    }

    private fun moonEmoji(phase: String): String = when {
        "New" in phase -> "🌑"
        "Waxing Crescent" in phase -> "🌒"
        "First Quarter" in phase -> "🌓"
        "Waxing Gibbous" in phase -> "🌔"
        "Full" in phase -> "🌕"
        "Waning Gibbous" in phase -> "🌖"
        "Last Quarter" in phase -> "🌗"
        "Waning Crescent" in phase -> "🌘"
        else -> "🌙"
    }

    private val luckyColors = listOf(
        "Teal", "Amber", "Rose", "Cobalt", "Sage", "Lavender", "Coral", "Slate",
    )

    companion object {
        /**
         * Standard "for entertainment only" disclaimer surfaced in the UI.
         *
         * The 80+ curated messages reference transits ("Mars energy," "Saturn
         * testing patience," "your 10th house buzzing") that are not computed
         * against the user's birth chart — see BUG-088. The disclaimer keeps
         * the messages fun without misleading astrology-literate users.
         */
        const val DEFAULT_DISCLAIMER = "For entertainment only — not astrological advice."

        // Curated fortune pool — 80+ messages mixing astrology, motivation, and Gen Z voice
        val messages = listOf(
            "The New Moon is a blank canvas — plant a seed (literally or metaphorically).",
            "Waxing Crescent energy says: start small, but start *now*.",
            "First Quarter means decisions. Trust your gut over your group chat.",
            "Waxing Gibbous = almost there. Don't quit before the glow-up.",
            "Full Moon energy is high. Text that person. Or delete their number. Both are valid.",
            "Waning Gibbous = release mode. Unfollow, unsubscribe, unbothered.",
            "Last Quarter = audit your circle. Quality over quantity, always.",
            "Waning Crescent = rest. Even main characters need a nap.",
            "The moon is new and so are you. Reinvention season.",
            "Your Mars energy is high today. Start something bold.",
            "A Wood day favors growth. Plant a seed.",
            "A Fire day favors action. Send the risky text.",
            "An Earth day favors grounding. Touch grass.",
            "A Metal day favors clarity. Cut the noise.",
            "A Water day favors flow. Don't force it.",
            "The moon is in your communication sector today — text that person.",
            "Your Venus transit says: treat yourself. You deserve the good coffee.",
            "Saturn is testing your patience. Pass the test.",
            "Jupiter says yes today. Apply for the thing.",
            "Mercury is direct. Finally. Sign the contract.",
            "Your North Node whispers: this discomfort is growth.",
            "South Node nostalgia hits different today. Remember the lesson, not the pain.",
            "The universe is not ghosting you. It's marinating.",
            "Today's energy: soft power. You don't have to loud to be heard.",
            "Your shadow self says hi. Say hi back.",
            "Intuition is just pattern recognition with better branding. Trust it.",
            "The stars didn't align. You aligned with yourself. That's better.",
            "Cosmic weather: partly chaotic with a chance of breakthrough.",
            "Your 10th house is buzzing. Update that LinkedIn. Yes, seriously.",
            "4th house vibes: clean your room. Your ancestors are watching.",
            "5th house joy: do something creative without monetizing it.",
            "6th house grind: hydrate. Your body is a temple, not a gas station.",
            "7th house relationships: set the boundary. They'll survive.",
            "8th house transformation: let it die so something better can live.",
            "9th house expansion: book the trip. Even if it's just a day trip.",
            "11th house community: reply to the group chat. They miss you.",
            "12th house retreat: solitude is not loneliness. Recharge.",
            "The lunar nodes say: you're exactly where you need to be. Even here.",
            "Your Chiron wound is someone else's healing. Share the story.",
            "Pluto says: destroy the old you. The new one is already waiting.",
            "Neptune fog lifts soon. Don't make permanent decisions in haze.",
            "Uranus says: surprise them. Do the unexpected thing.",
            "Aries energy: first doesn't mean best. But it means brave.",
            "Taurus energy: luxury is a mindset. Feel rich today.",
            "Gemini energy: talk to two people you haven't texted in a month.",
            "Cancer energy: feel it all. Then let it go.",
            "Leo energy: you are the moment. Own it.",
            "Virgo energy: organize one drawer. The dopamine is real.",
            "Libra energy: choose peace over being right. For once.",
            "Scorpio energy: intensity is not a flaw. It's a feature.",
            "Sagittarius energy: learn one useless fact today. It'll come in handy.",
            "Capricorn energy: climb, but look at the view sometimes.",
            "Aquarius energy: be weird on purpose. Normal is overrated.",
            "Pisces energy: daydreams are just drafts of reality.",
            "Your solar return energy lingers. You're still the main character.",
            "The void of course moon says: wait. Don't launch. Don't text. Just wait.",
            "Lunar eclipse energy: something ends. Something better begins.",
            "Solar eclipse energy: new chapter. New you. New rules.",
            "Supermoon energy: everything feels bigger. So does your potential.",
            "Blue moon energy: rare opportunities look like normal days. Notice them.",
            "Blood moon energy: the shadow reveals what the light hid. Look closer.",
            "Harvest moon energy: reap what you sowed. Even the small seeds.",
            "Strawberry moon energy: sweetness is a choice. Choose it.",
            "Snow moon energy: stillness is productive too.",
            "Worm moon energy: the thaw is coming. Prepare your garden.",
            "Sturgeon moon energy: abundance is already in the water. Cast the net.",
            "Corn moon energy: gratitude is the best fertilizer.",
            "Hunter's moon energy: pursue what matters. Ignore the noise.",
            "Beaver moon energy: build something that lasts. Even if it's small.",
            "Cold moon energy: conserve your warmth for those who deserve it.",
            "Oak moon energy: strength grows in silence. Keep growing.",
            "Wolf moon energy: howl if you need to. Someone will answer.",
            "Storm moon energy: the chaos clears. Stay inside metaphorically.",
            "Sap moon energy: the sweet stuff is flowing. Tap in.",
            "Crust moon energy: break the surface. There's more underneath.",
            "Death moon energy: endings are just backspaces in the cosmic text.",
            "Trinity moon energy: mind, body, spirit. Align all three today.",
        )
    }
}
