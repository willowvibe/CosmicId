package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rich metadata for the 27 Nakshatras (lunar mansions) of Vedic astrology.
 *
 * Each nakshatra spans 13°20′ (360°/27) of the sidereal ecliptic and has:
 *  - A ruling planet (lord) — used for Vimshottari Dasha period assignment.
 *  - A presiding deity — the spiritual entity who governs that mansion.
 *  - A Gana (temperament) — Deva (divine/good), Manushya (human/neutral),
 *    or Rakshasa (demonic/strong) — used in Ashtakoot / Guna Milan.
 *  - A symbol — the object or form the mansion represents.
 *  - A start/end degree — the boundary of the mansion on the sidereal ecliptic.
 *
 * Ruling planets follow the Vimshottari Dasha sequence (Ketu→Venus→Sun→Moon→Mars→
 * Rahu→Jupiter→Saturn→Mercury) repeated three times, covering all 27 nakshatras.
 *
 * Source: classical Jyotish references (Brihat Parashara Hora Shastra; standardised in
 * the published Lahiri ephemeris).
 */
@Singleton
// open for BirthChartSubChart exception-isolation test; do not subclass in production.
open class NakshatraMetadata @Inject constructor() {

    /**
     * Look up the metadata for a nakshatra by its [index] in 0..26.
     */
    fun forIndex(index: Int): NakshatraData {
        val safe = ((index % 27) + 27) % 27
        return NAKSHATRA_TABLE[safe]
    }

    /**
     * Look up the metadata for the nakshatra containing a given sidereal longitude
     * (0..360). Returns the same fields as [forIndex].
     */
    open fun forLongitude(siderealLongitude: Double): NakshatraData {
        val arc = 360.0 / 27.0
        val idx = ((siderealLongitude / arc).toInt() % 27 + 27) % 27
        return forIndex(idx)
    }

    private val NAKSHATRA_TABLE: List<NakshatraData> = listOf(
        // 0 — Ashwini
        NakshatraData(
            index = 0,
            name = "Ashwini",
            nameHangul = "अश्विनी",
            lord = CelestialBody.KETU,
            deity = "Ashwini Kumaras",
            deityHangul = "अश्विनी कुमार",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Horse head",
            symbolEmoji = "🐎",
            startDegree = 0.0,
            endDegree = 13.3333,
        ),
        // 1 — Bharani
        NakshatraData(
            index = 1,
            name = "Bharani",
            nameHangul = "भरणी",
            lord = CelestialBody.VENUS,
            deity = "Yama",
            deityHangul = "यम",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya (인간)",
            symbol = "Yoni",
            symbolEmoji = "♋",
            startDegree = 13.3333,
            endDegree = 26.6667,
        ),
        // 2 — Krittika
        NakshatraData(
            index = 2,
            name = "Krittika",
            nameHangul = "कृत्तिका",
            lord = CelestialBody.SUN,
            deity = "Agni",
            deityHangul = "अग्नि",
            gana = Gana.RAKSHASA,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Razor / Flame",
            symbolEmoji = "🔥",
            startDegree = 26.6667,
            endDegree = 40.0,
        ),
        // 3 — Rohini
        NakshatraData(
            index = 3,
            name = "Rohini",
            nameHangul = "रोहिणी",
            lord = CelestialBody.MOON,
            deity = "Brahma / Prajapati",
            deityHangul = "ब्रह्मा",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya (인간)",
            symbol = "Chariot / Cart",
            symbolEmoji = "🛒",
            startDegree = 40.0,
            endDegree = 53.3333,
        ),
        // 4 — Mrigashira
        NakshatraData(
            index = 4,
            name = "Mrigashira",
            nameHangul = "मृगशिरा",
            lord = CelestialBody.MARS,
            deity = "Soma (Moon)",
            deityHangul = "सोम",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Deer head",
            symbolEmoji = "🦌",
            startDegree = 53.3333,
            endDegree = 66.6667,
        ),
        // 5 — Ardra
        NakshatraData(
            index = 5,
            name = "Ardra",
            nameHangul = "आर्द्रा",
            lord = CelestialBody.RAHU,
            deity = "Rudra",
            deityHangul = "रुद्र",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya (인간)",
            symbol = "Tear drop",
            symbolEmoji = "💧",
            startDegree = 66.6667,
            endDegree = 80.0,
        ),
        // 6 — Punarvasu
        NakshatraData(
            index = 6,
            name = "Punarvasu",
            nameHangul = "पुनर्वसु",
            lord = CelestialBody.JUPITER,
            deity = "Aditi",
            deityHangul = "अदिति",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Bow & quiver",
            symbolEmoji = "🏹",
            startDegree = 80.0,
            endDegree = 93.3333,
        ),
        // 7 — Pushya
        NakshatraData(
            index = 7,
            name = "Pushya",
            nameHangul = "पुष्य",
            lord = CelestialBody.SATURN,
            deity = "Brihaspati",
            deityHangul = "बृहस्पति",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Lotus / flower",
            symbolEmoji = "🪷",
            startDegree = 93.3333,
            endDegree = 106.6667,
        ),
        // 8 — Ashlesha
        NakshatraData(
            index = 8,
            name = "Ashlesha",
            nameHangul = "आश्लेषा",
            lord = CelestialBody.MERCURY,
            deity = "Naga (Serpents)",
            deityHangul = "नाग",
            gana = Gana.RAKSHASA,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Serpent",
            symbolEmoji = "🐍",
            startDegree = 106.6667,
            endDegree = 120.0,
        ),
        // 9 — Magha
        NakshatraData(
            index = 9,
            name = "Magha",
            nameHangul = "मघा",
            lord = CelestialBody.KETU,
            deity = "Pitrs (Ancestors)",
            deityHangul = "पितृ",
            gana = Gana.RAKSHASA,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Throne",
            symbolEmoji = "🪑",
            startDegree = 120.0,
            endDegree = 133.3333,
        ),
        // 10 — Purva Phalguni
        NakshatraData(
            index = 10,
            name = "Purva Phalguni",
            nameHangul = "पूर्व फाल्गुनी",
            lord = CelestialBody.VENUS,
            deity = "Bhaga",
            deityHangul = "भग",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya (인간)",
            symbol = "Fig / fruit",
            symbolEmoji = "🪴",
            startDegree = 133.3333,
            endDegree = 146.6667,
        ),
        // 11 — Uttara Phalguni
        NakshatraData(
            index = 11,
            name = "Uttara Phalguni",
            nameHangul = "उत्तर फाल्गुनी",
            lord = CelestialBody.SUN,
            deity = "Aryaman",
            deityHangul = "अर्यमन्",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya (인간)",
            symbol = "Bed / hammock",
            symbolEmoji = "🛏️",
            startDegree = 146.6667,
            endDegree = 160.0,
        ),
        // 12 — Hasta
        NakshatraData(
            index = 12,
            name = "Hasta",
            nameHangul = "हस्त",
            lord = CelestialBody.MOON,
            deity = "Savitar (Sun)",
            deityHangul = "सवितृ",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Hand / palm",
            symbolEmoji = "🤚",
            startDegree = 160.0,
            endDegree = 173.3333,
        ),
        // 13 — Chitra
        NakshatraData(
            index = 13,
            name = "Chitra",
            nameHangul = "चित्रा",
            lord = CelestialBody.MARS,
            deity = "Vishwakarma (Tvashtar)",
            deityHangul = "विश्वकर्मा",
            gana = Gana.RAKSHASA,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Pearl / jewel",
            symbolEmoji = "💎",
            startDegree = 173.3333,
            endDegree = 186.6667,
        ),
        // 14 — Swati
        NakshatraData(
            index = 14,
            name = "Swati",
            nameHangul = "स्वाति",
            lord = CelestialBody.RAHU,
            deity = "Vayu (Wind)",
            deityHangul = "वायु",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Coral / young plant",
            symbolEmoji = "🌱",
            startDegree = 186.6667,
            endDegree = 200.0,
        ),
        // 15 — Vishakha
        NakshatraData(
            index = 15,
            name = "Vishakha",
            nameHangul = "विशाखा",
            lord = CelestialBody.JUPITER,
            deity = "Indra-Agni",
            deityHangul = "इन्द्र-अग्नि",
            gana = Gana.RAKSHASA,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Archway / potter's wheel",
            symbolEmoji = "🌀",
            startDegree = 200.0,
            endDegree = 213.3333,
        ),
        // 16 — Anuradha
        NakshatraData(
            index = 16,
            name = "Anuradha",
            nameHangul = "अनुराधा",
            lord = CelestialBody.SATURN,
            deity = "Mitra",
            deityHangul = "मित्र",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Lotus",
            symbolEmoji = "🪷",
            startDegree = 213.3333,
            endDegree = 226.6667,
        ),
        // 17 — Jyeshtha
        NakshatraData(
            index = 17,
            name = "Jyeshtha",
            nameHangul = "ज्येष्ठा",
            lord = CelestialBody.MERCURY,
            deity = "Indra",
            deityHangul = "इन्द्र",
            gana = Gana.RAKSHASA,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Umbrella / earring",
            symbolEmoji = "☂️",
            startDegree = 226.6667,
            endDegree = 240.0,
        ),
        // 18 — Moola
        NakshatraData(
            index = 18,
            name = "Moola",
            nameHangul = "मूला",
            lord = CelestialBody.KETU,
            deity = "Nirriti (Alakshmi)",
            deityHangul = "निर्ऋति",
            gana = Gana.RAKSHASA,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Root / bunch of roots",
            symbolEmoji = "🌿",
            startDegree = 240.0,
            endDegree = 253.3333,
        ),
        // 19 — Purva Ashadha
        NakshatraData(
            index = 19,
            name = "Purva Ashadha",
            nameHangul = "पूर्वाषाढ़ा",
            lord = CelestialBody.VENUS,
            deity = "Apas (Water)",
            deityHangul = "आपः",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya (인간)",
            symbol = "Fan / winnowing basket",
            symbolEmoji = "🪭",
            startDegree = 253.3333,
            endDegree = 266.6667,
        ),
        // 20 — Uttara Ashadha
        NakshatraData(
            index = 20,
            name = "Uttara Ashadha",
            nameHangul = "उत्तराषाढ़ा",
            lord = CelestialBody.SUN,
            deity = "Vishvedevas (Universal gods)",
            deityHangul = "विश्वेदेवाः",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya (인간)",
            symbol = "Elephant tusk",
            symbolEmoji = "🐘",
            startDegree = 266.6667,
            endDegree = 280.0,
        ),
        // 21 — Shravana
        NakshatraData(
            index = 21,
            name = "Shravana",
            nameHangul = "श्रवण",
            lord = CelestialBody.MOON,
            deity = "Vishnu",
            deityHangul = "विष्णु",
            gana = Gana.DEVA,
            ganaHangul = "Deva (신)",
            symbol = "Ear / three footprints",
            symbolEmoji = "👂",
            startDegree = 280.0,
            endDegree = 293.3333,
        ),
        // 22 — Dhanishtha
        NakshatraData(
            index = 22,
            name = "Dhanishtha",
            nameHangul = "धनिष्ठा",
            lord = CelestialBody.MARS,
            deity = "Ashta Vasus (Eight Vasus)",
            deityHangul = "अष्ट वसु",
            gana = GANA_R,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Drum / flute",
            symbolEmoji = "🥁",
            startDegree = 293.3333,
            endDegree = 306.6667,
        ),
        // 23 — Shatabhisha
        NakshatraData(
            index = 23,
            name = "Shatabhisha",
            nameHangul = "शतभिषा",
            lord = CelestialBody.RAHU,
            deity = "Varuna (God of cosmic waters)",
            deityHangul = "वरुण",
            gana = GANA_R,
            ganaHangul = "Rakshasa (라크샤사)",
            symbol = "Empty circle / 100 stars",
            symbolEmoji = "⭕",
            startDegree = 306.6667,
            endDegree = 320.0,
        ),
        // 24 — Purva Bhadrapada
        NakshatraData(
            index = 24,
            name = "Purva Bhadrapada",
            nameHangul = "पूर्व भाद्रपद",
            lord = CelestialBody.JUPITER,
            deity = "Aja Ekapada",
            deityHangul = "अज एकपाद",
            gana = GANA_M,
            ganaHangul = "Manushya (인간)",
            symbol = "Sword / two front legs of funeral cot",
            symbolEmoji = "⚔️",
            startDegree = 320.0,
            endDegree = 333.3333,
        ),
        // 25 — Uttara Bhadrapada
        NakshatraData(
            index = 25,
            name = "Uttara Bhadrapada",
            nameHangul = "उत्तर भाद्रपद",
            lord = CelestialBody.SATURN,
            deity = "Ahir Budhnya (Serpent of the deep)",
            deityHangul = "अहिर्बुध्न्य",
            gana = GANA_M,
            ganaHangul = "Manushya (인간)",
            symbol = "Back legs of funeral cot / twin serpent",
            symbolEmoji = "🐍",
            startDegree = 333.3333,
            endDegree = 346.6667,
        ),
        // 26 — Revati
        NakshatraData(
            index = 26,
            name = "Revati",
            nameHangul = "रेवती",
            lord = CelestialBody.MERCURY,
            deity = "Pushan (Nourisher)",
            deityHangul = "पूषन्",
            gana = GANA_D,
            ganaHangul = "Deva (신)",
            symbol = "Fish / drum",
            symbolEmoji = "🐟",
            startDegree = 346.6667,
            endDegree = 360.0,
        ),
    )

    private companion object {
        // Shorthand aliases to keep the table above compact.
        // (Not `const val` — Kotlin's `const` is restricted to primitive types and String.)
        val GANA_D = Gana.DEVA
        val GANA_M = Gana.MANUSHYA
        val GANA_R = Gana.RAKSHASA
    }
}

/**
 * Immutable description of a single Nakshatra.
 */
data class NakshatraData(
    val index: Int,
    val name: String,
    val nameHangul: String,
    val lord: CelestialBody,
    val deity: String,
    val deityHangul: String,
    val gana: Gana,
    val ganaHangul: String,
    val symbol: String,
    val symbolEmoji: String,
    val startDegree: Double,
    val endDegree: Double,
) {
    /**
     * Human-readable one-line summary for UI display.
     */
    fun displayLabel(): String = "$symbolEmoji $name — ${lord.displayName} — $deity"
}

/**
 * Gana — the temperament of a Nakshatra, used in Ashtakoot / Guna Milan compatibility.
 */
enum class Gana {
    DEVA,      // Divine, gentle, refined
    MANUSHYA,  // Human, balanced, neutral
    RAKSHASA,  // Demonic, strong, assertive
}
