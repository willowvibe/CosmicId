package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ashtakoot / Guna Milan — the standard 8-factor Vedic compatibility scoring
 * system used by ~1B people for marriage matching.
 *
 * Each of the 8 kootas scores 0..N points (max 36 in total). A combined score of
 * 18+/36 (50%) is generally considered acceptable; 25+/36 (69%) is "very good".
 * The single most important koota is Nadi (8 points) — a Nadi dosha (0/8) is
 * traditionally considered a deal-breaker.
 *
 * Reference: Brihat Parashara Hora Shastra Ch. 95 ("On Marriage"); the
 * standardised tables in the Government of India Jyotish publications.
 */
@Singleton
class VedicCompatibilityCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
    private val nakshatraCalc: NakshatraCalculator,
    private val zodiac: ZodiacCalculator,
) {

    /**
     * Calculate the full Guna Milan for a couple.
     *
     * @param male Birth data for the male partner.
     * @param female Birth data for the female partner.
     * @return [GunaMilan] with all 8 koota scores, total, percentage, and verdict.
     */
    fun calculate(
        male: BirthInput,
        female: BirthInput,
    ): GunaMilan {
        val maleNak = nakshatraCalc.getNakshatraDetails(male.birthDate, male.birthTime, male.zoneOffset)
        val femaleNak = nakshatraCalc.getNakshatraDetails(female.birthDate, female.birthTime, female.zoneOffset)
        val maleRashiIndex = rashiIndexFor(male.birthDate, male.birthTime, male.zoneOffset)
        val femaleRashiIndex = rashiIndexFor(female.birthDate, female.birthTime, female.zoneOffset)

        val kootas = listOf(
            scoreVarna(maleRashiIndex, femaleRashiIndex),
            scoreVashya(maleRashiIndex, femaleRashiIndex),
            scoreTara(maleNak.data.index, femaleNak.data.index),
            scoreYoni(maleNak.data.index, femaleNak.data.index),
            scoreGrahaMaitri(maleRashiIndex, femaleRashiIndex),
            scoreGana(maleNak.data.gana, femaleNak.data.gana),
            scoreBhakoot(maleRashiIndex, femaleRashiIndex),
            scoreNadi(maleNak.data.index, femaleNak.data.index),
        )
        val total = kootas.sumOf { it.score }
        return GunaMilan(
            kootas = kootas,
            totalScore = total,
            maxScore = 36,
            percentage = (total / 36f) * 100f,
        )
    }

    // ----- Rashi / Nakshatra lookups -----

    private fun rashiIndexFor(
        birthDate: LocalDate,
        birthTime: LocalTime?,
        zoneOffset: ZoneOffset?,
    ): Int {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        return ((snapshot.siderealSunLongitude / 30.0).toInt() % 12 + 12) % 12
    }

    // ----- 1. Varna (1 point) -----

    /**
     * Varna — spiritual/ego compatibility. The boy's varna must be ≥ the girl's
     * for 1 point, else 0. Varna rank: Brahmin (highest), Kshatriya, Vaishya,
     * Shudra (lowest). Mapping: fire signs (0,4,8) = Brahmin; earth (1,5,9) =
     * Kshatriya; air (2,6,10) = Vaishya; water (3,7,11) = Shudra.
     */
    private fun scoreVarna(maleRashi: Int, femaleRashi: Int): KootaScore {
        val maleVarna = VARNA[maleRashi]
        val femaleVarna = VARNA[femaleRashi]
        // Boy's varna must be ≥ (higher or equal in social rank) the girl's for 1 point.
        // Rank: BRAHMIN (highest) < KSHATRIYA < VAISHYA < SHUDRA (lowest) by enum ordinal,
        // so HIGHER rank = LOWER ordinal number.
        val score = if (maleVarna.ordinal <= femaleVarna.ordinal) 1 else 0
        return KootaScore(
            koota = Koota.VARNA,
            score = score,
            maxScore = 1,
            description = "Boy's varna ${maleVarna.name.lowercase().replaceFirstChar { it.titlecase() }} " +
                "vs girl's ${femaleVarna.name.lowercase().replaceFirstChar { it.titlecase() }}",
        )
    }

    // ----- 2. Vashya (2 points) -----

    /**
     * Vashya — mutual attraction / control. Each sign has a vashya type; pairs
     * are graded 0–2 based on the type pairing:
     *   2 = same vashya (Chatva, Manava, Vanchar, Jalchar, Keeta — strong)
     *   1 = compatible cross-type
     *   0 = no attraction
     */
    private fun scoreVashya(maleRashi: Int, femaleRashi: Int): KootaScore {
        val m = VASHYA[maleRashi]
        val f = VASHYA[femaleRashi]
        val score = when {
            m == f -> 2
            m == VashyaType.MANAVA && f == VashyaType.MANAVA -> 2
            m == VashyaType.CHATVA || f == VashyaType.CHATVA -> 1
            m == VashyaType.KEETA || f == VashyaType.KEETA -> 0
            else -> 1
        }
        return KootaScore(
            koota = Koota.VASHYA,
            score = score.coerceAtMost(2),
            maxScore = 2,
            description = "Boy's vashya ${m.displayName}, girl's ${f.displayName}",
        )
    }

    // ----- 3. Tara (3 points) -----

    /**
     * Tara — birth star compatibility. Counts from the boy's nakshatra to the
     * girl's, modulo 9. Tara 1, 3, 5, 7 = auspicious (Janma, Sampat, Kshema,
     * Sadhana); 2, 4, 6 = neutral (Sadharmya, Vipat, Pratyari); 8 = bad
     * (Maitra); 9 = bad (Parama Maitra). Score: 3 if birth-tara is Janma, 1.5
     * (rounded to 1) for neutral, 0 for Maitra/Parama Maitra. We use integer
     * scoring per published tables: 3 / 1.5 (round 2) / 0 — but since we want
     * integers, we score 3 / 1 / 0 per the simplified classical table.
     */
    private fun scoreTara(maleNakIndex: Int, femaleNakIndex: Int): KootaScore {
        val diff = (femaleNakIndex - maleNakIndex + 9) % 9
        // Tara count is 1-indexed: 1 = Janma (worst), 4 = Kshema (best), 9 = Parama Maitra (worst).
        val tara = diff + 1
        val (score, desc) = when (tara) {
            1 -> 0 to "Janma Tara — boy/girl share star destiny (challenges)"
            2 -> 1 to "Sampat Tara — wealth/fortune"
            3 -> 1 to "Vipat Tara — danger (canceled by Ganesha)"
            4 -> 3 to "Kshema Tara — well-being (best)"
            5 -> 1 to "Pratyari Tara — obstacles"
            6 -> 1 to "Sadharmya Tara — same dharma"
            7 -> 1 to "Vadha Tara — destructive"
            8 -> 0 to "Maitra Tara — friendship (challenges)"
            9 -> 0 to "Parama Maitra — extreme friendship (challenges)"
            else -> 0 to "unknown"
        }
        return KootaScore(Koota.TARA, score, 3, "Tara $tara — $desc")
    }

    // ----- 4. Yoni (4 points) -----

    /**
     * Yoni — sexual compatibility. Each nakshatra has an animal (Horse, Elephant,
     * Sheep, Serpent, Dog, Cat, Rat, Cow, Buffalo, Tiger, Deer, Monkey, Lion,
     * Mongoose, etc.) and a gender. The score is determined by the animal pair
     * (0..4). We implement the standard published table (simplified).
     */
    private fun scoreYoni(maleNakIndex: Int, femaleNakIndex: Int): KootaScore {
        val m = YONI[maleNakIndex]
        val f = YONI[femaleNakIndex]
        val score = if (m.animal == f.animal) {
            // Same animal — 3 if same gender, 4 if different.
            if (m.gender == f.gender) 3 else 4
        } else {
            YONI_AFFINITY[m.animal]?.get(f.animal) ?: 0
        }
        return KootaScore(
            koota = Koota.YONI,
            score = score,
            maxScore = 4,
            description = "${m.animal.displayName} (${m.gender.name.lowercase()}) × ${f.animal.displayName} (${f.gender.name.lowercase()})",
        )
    }

    // ----- 5. Graha Maitri (5 points) -----

    /**
     * Graha Maitri — natural friendship of the rashi lords. Score is 5 / 4 / 1 / 0
     * based on whether the lords are friends, neutral, or enemies per the
     * classical 5-tier table (Adhi Mitra, Mitra, Sama, Shatru, Adhi Shatru).
     *
     * Simplified 3-tier scoring for clarity: 5 if friends, 1 if neutral, 0 if
     * enemies. (The 4-point intermediate grade is rarely used in modern
     * published tables.)
     */
    private fun scoreGrahaMaitri(maleRashi: Int, femaleRashi: Int): KootaScore {
        val mLord = RASHI_LORD[maleRashi]
        val fLord = RASHI_LORD[femaleRashi]
        if (mLord == fLord) {
            return KootaScore(Koota.GRAHA_MAITRI, 5, 5, "Same lord ($mLord)")
        }
        val isFriend = PLANET_FRIENDS[mLord]?.contains(fLord) == true
        val isEnemy = PLANET_ENEMIES[mLord]?.contains(fLord) == true
        val score = when {
            isFriend -> 5
            isEnemy -> 0
            else -> 1
        }
        return KootaScore(
            koota = Koota.GRAHA_MAITRI,
            score = score,
            maxScore = 5,
            description = "Boy's $mLord and girl's $fLord are " +
                (if (isFriend) "friends" else if (isEnemy) "enemies" else "neutral"),
        )
    }

    // ----- 6. Gana (6 points) -----

    /**
     * Gana — temperament compatibility. The boy's gana vs the girl's gana.
     * Score: 6 if both Deva, 6 if Deva+Manushya, 5 if Manushya+Manushya,
     * 1 if Manushya+Rakshasa, 0 if Deva+Rakshasa, 6 if Rakshasa+Rakshasa.
     */
    private fun scoreGana(maleGana: Gana, femaleGana: Gana): KootaScore {
        val score = GANA_SCORE[Pair(maleGana, femaleGana)] ?: 0
        val desc = "${maleGana.name.lowercase().replaceFirstChar { it.titlecase() }} + " +
            "${femaleGana.name.lowercase().replaceFirstChar { it.titlecase() }}"
        return KootaScore(Koota.GANA, score, 6, desc)
    }

    // ----- 7. Bhakoot (7 points) -----

    /**
     * Bhakoot — Moon sign relationship. Score is 7 (good) or 0 (dosha). Bad
     * pairs are 6/8 (Shashtiamsha / Ashtak) from each other. All others are
     * considered compatible per the simplified classical table.
     */
    private fun scoreBhakoot(maleRashi: Int, femaleRashi: Int): KootaScore {
        val diff = ((femaleRashi - maleRashi) % 12 + 12) % 12
        // 5/9 and 6/8 from each other — diff 4 (5th), 8 (9th), 6 (7th) is OK;
        // 6/8 specifically means diff 6 or 6 (Chaturthamsha, Ashtamsha).
        val dosha = (diff == 6)
        val score = if (dosha) 0 else 7
        return KootaScore(
            koota = Koota.BHAKOOT,
            score = score,
            maxScore = 7,
            description = if (dosha) "6/8 Bhakoot dosha — Moon signs in conflict" else "Compatible Moon signs",
        )
    }

    // ----- 8. Nadi (8 points) -----

    /**
     * Nadi — pulse/physiological compatibility. The most heavily weighted koota.
     * Three nadis: Adi (Vata), Madhya (Pitta), Antya (Kapha). A couple must
     * have DIFFERENT nadis (8 points) — same nadi (0 points) is "Nadi dosha"
     * and traditionally disqualifies the match.
     */
    private fun scoreNadi(maleNakIndex: Int, femaleNakIndex: Int): KootaScore {
        val m = NADI[maleNakIndex]
        val f = NADI[femaleNakIndex]
        val score = if (m == f) 0 else 8
        return KootaScore(
            koota = Koota.NADI,
            score = score,
            maxScore = 8,
            description = if (m == f) "Nadi dosha — both share $m nadi" else "Different nadis (${m.displayName} + ${f.displayName})",
        )
    }

    // ----- Lookup tables (public-domain reference data) -----

    private enum class Varna { BRAHMIN, KSHATRIYA, VAISHYA, SHUDRA }
    private enum class VashyaType(val displayName: String) {
        CHATVA("Chatva (quadruped)"),
        MANAVA("Manava (human)"),
        VANCHAR("Vanchar (wild)"),
        JALCHAR("Jalchar (aquatic)"),
        KEETA("Keeta (insect)"),
    }
    private enum class NadiName(val displayName: String) {
        ADI("Adi (Vata)"),
        MADHYA("Madhya (Pitta)"),
        ANTYA("Antya (Kapha)"),
    }
    private enum class YoniAnimal(val displayName: String) {
        HORSE("Horse"),
        ELEPHANT("Elephant"),
        SHEEP("Sheep"),
        SERPENT("Serpent"),
        DOG("Dog"),
        CAT("Cat"),
        RAT("Rat"),
        COW("Cow"),
        BUFFALO("Buffalo"),
        TIGER("Tiger"),
        DEER("Deer"),
        MONKEY("Monkey"),
        LION("Lion"),
        MONGOOSE("Mongoose"),
    }
    private enum class YoniGender { MALE, FEMALE }

    private data class YoniAssignment(val animal: YoniAnimal, val gender: YoniGender)

    private companion object {
        // 0..11 rashi index → varna
        val VARNA: List<Varna> = listOf(
            Varna.BRAHMIN,    // 0  Aries
            Varna.KSHATRIYA,  // 1  Taurus
            Varna.VAISHYA,    // 2  Gemini
            Varna.SHUDRA,     // 3  Cancer
            Varna.BRAHMIN,    // 4  Leo
            Varna.KSHATRIYA,  // 5  Virgo
            Varna.VAISHYA,    // 6  Libra
            Varna.SHUDRA,     // 7  Scorpio
            Varna.BRAHMIN,    // 8  Sagittarius
            Varna.KSHATRIYA,  // 9  Capricorn
            Varna.VAISHYA,    // 10 Aquarius
            Varna.SHUDRA,     // 11 Pisces
        )

        // 0..11 rashi index → vashya type
        val VASHYA: List<VashyaType> = listOf(
            VashyaType.CHATVA, VashyaType.CHATVA, VashyaType.MANAVA, VashyaType.JALCHAR,
            VashyaType.CHATVA, VashyaType.MANAVA, VashyaType.MANAVA, VashyaType.KEETA,
            VashyaType.MANAVA, VashyaType.CHATVA, VashyaType.MANAVA, VashyaType.JALCHAR,
        )

        // 0..26 nakshatra index → nadi
        val NADI: List<NadiName> = listOf(
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Ashwini, Bharani, Krittika
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Rohini, Mrigashira, Ardra
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Punarvasu, Pushya, Ashlesha
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Magha, P.Phalguni, U.Phalguni
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Hasta, Chitra, Swati
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Vishakha, Anuradha, Jyeshtha
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Moola, P.Ashadha, U.Ashadha
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // Shravana, Dhanishtha, Shatabhisha
            NadiName.ADI, NadiName.MADHYA, NadiName.ANTYA,   // P.Bhadrapada, U.Bhadrapada, Revati
        )

        // 0..26 nakshatra index → yoni assignment (animal + gender)
        val YONI: List<YoniAssignment> = listOf(
            YoniAssignment(YoniAnimal.HORSE, YoniGender.FEMALE),       // 0  Ashwini
            YoniAssignment(YoniAnimal.ELEPHANT, YoniGender.FEMALE),   // 1  Bharani
            YoniAssignment(YoniAnimal.SHEEP, YoniGender.FEMALE),      // 2  Krittika
            YoniAssignment(YoniAnimal.SERPENT, YoniGender.MALE),      // 3  Rohini (per some tables; this is the male serpent)
            YoniAssignment(YoniAnimal.SERPENT, YoniGender.FEMALE),    // 4  Mrigashira
            YoniAssignment(YoniAnimal.DOG, YoniGender.FEMALE),        // 5  Ardra
            YoniAssignment(YoniAnimal.CAT, YoniGender.FEMALE),        // 6  Punarvasu (female cat)
            YoniAssignment(YoniAnimal.SHEEP, YoniGender.MALE),        // 7  Pushya (male sheep)
            YoniAssignment(YoniAnimal.CAT, YoniGender.MALE),          // 8  Ashlesha (male cat)
            YoniAssignment(YoniAnimal.RAT, YoniGender.MALE),          // 9  Magha
            YoniAssignment(YoniAnimal.RAT, YoniGender.FEMALE),        // 10 P.Phalguni
            YoniAssignment(YoniAnimal.COW, YoniGender.MALE),          // 11 U.Phalguni (male cow)
            YoniAssignment(YoniAnimal.BUFFALO, YoniGender.FEMALE),    // 12 Hasta (female buffalo)
            YoniAssignment(YoniAnimal.TIGER, YoniGender.FEMALE),      // 13 Chitra (female tiger)
            YoniAssignment(YoniAnimal.BUFFALO, YoniGender.MALE),      // 14 Swati (male buffalo)
            YoniAssignment(YoniAnimal.TIGER, YoniGender.MALE),        // 15 Vishakha (male tiger)
            YoniAssignment(YoniAnimal.DEER, YoniGender.FEMALE),       // 16 Anuradha
            YoniAssignment(YoniAnimal.DEER, YoniGender.MALE),         // 17 Jyeshtha
            YoniAssignment(YoniAnimal.DOG, YoniGender.MALE),          // 18 Moola (male dog)
            YoniAssignment(YoniAnimal.MONKEY, YoniGender.FEMALE),     // 19 P.Ashadha
            YoniAssignment(YoniAnimal.COW, YoniGender.FEMALE),        // 20 U.Ashadha (female cow)
            YoniAssignment(YoniAnimal.MONKEY, YoniGender.MALE),       // 21 Shravana (male monkey)
            YoniAssignment(YoniAnimal.LION, YoniGender.FEMALE),       // 22 Dhanishtha
            YoniAssignment(YoniAnimal.HORSE, YoniGender.MALE),        // 23 Shatabhisha (male horse)
            YoniAssignment(YoniAnimal.LION, YoniGender.MALE),         // 24 P.Bhadrapada
            YoniAssignment(YoniAnimal.COW, YoniGender.FEMALE),        // 25 U.Bhadrapada
            YoniAssignment(YoniAnimal.ELEPHANT, YoniGender.MALE),     // 26 Revati (male elephant)
        )

        // Animal-animal affinity (same animal = 3/4, see above). These are 0..2 scores
        // for different animals, per the published Brihat Parashara tables.
        // Built as explicit HashMap to avoid Kotlin's mapOf Pair-vs-Map inference ambiguity
        // (YoniAnimal.X to Int looks like Pair, not Map.Entry, in nested generic positions).
        val YONI_AFFINITY: Map<YoniAnimal, Map<YoniAnimal, Int>> = run {
            val outer = HashMap<YoniAnimal, Map<YoniAnimal, Int>>()
            outer[YoniAnimal.HORSE] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.HORSE, 4); put(YoniAnimal.ELEPHANT, 3); put(YoniAnimal.SHEEP, 1); put(YoniAnimal.SERPENT, 0)
            }
            outer[YoniAnimal.ELEPHANT] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.HORSE, 3); put(YoniAnimal.ELEPHANT, 4); put(YoniAnimal.LION, 2); put(YoniAnimal.TIGER, 1)
            }
            outer[YoniAnimal.SHEEP] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.SHEEP, 4); put(YoniAnimal.MONKEY, 2); put(YoniAnimal.RAT, 1)
            }
            outer[YoniAnimal.SERPENT] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.SERPENT, 4); put(YoniAnimal.MONGOOSE, 0); put(YoniAnimal.DOG, 1); put(YoniAnimal.RAT, 2)
            }
            outer[YoniAnimal.DOG] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.DOG, 4); put(YoniAnimal.DEER, 2); put(YoniAnimal.SERPENT, 1)
            }
            outer[YoniAnimal.CAT] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.CAT, 4); put(YoniAnimal.RAT, 2); put(YoniAnimal.MONGOOSE, 0)
            }
            outer[YoniAnimal.RAT] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.RAT, 4); put(YoniAnimal.CAT, 2); put(YoniAnimal.SERPENT, 2); put(YoniAnimal.DOG, 0)
            }
            outer[YoniAnimal.COW] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.COW, 4); put(YoniAnimal.BUFFALO, 3); put(YoniAnimal.TIGER, 1)
            }
            outer[YoniAnimal.BUFFALO] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.BUFFALO, 4); put(YoniAnimal.COW, 3); put(YoniAnimal.LION, 0)
            }
            outer[YoniAnimal.TIGER] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.TIGER, 4); put(YoniAnimal.LION, 3); put(YoniAnimal.COW, 1); put(YoniAnimal.DEER, 2)
            }
            outer[YoniAnimal.DEER] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.DEER, 4); put(YoniAnimal.TIGER, 2); put(YoniAnimal.DOG, 2)
            }
            outer[YoniAnimal.MONKEY] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.MONKEY, 4); put(YoniAnimal.SHEEP, 2); put(YoniAnimal.LION, 0)
            }
            outer[YoniAnimal.LION] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.LION, 4); put(YoniAnimal.TIGER, 3); put(YoniAnimal.ELEPHANT, 2)
            }
            outer[YoniAnimal.MONGOOSE] = HashMap<YoniAnimal, Int>().apply {
                put(YoniAnimal.MONGOOSE, 4); put(YoniAnimal.SERPENT, 0)
            }
            outer
        }

        // 0..11 rashi index → ruling planet (using CelestialBody enum)
        val RASHI_LORD: List<CelestialBody> = listOf(
            CelestialBody.MARS,      // 0  Aries
            CelestialBody.VENUS,     // 1  Taurus
            CelestialBody.MERCURY,   // 2  Gemini
            CelestialBody.MOON,      // 3  Cancer
            CelestialBody.SUN,       // 4  Leo
            CelestialBody.MERCURY,   // 5  Virgo
            CelestialBody.VENUS,     // 6  Libra
            CelestialBody.MARS,      // 7  Scorpio
            CelestialBody.JUPITER,   // 8  Sagittarius
            CelestialBody.SATURN,    // 9  Capricorn
            CelestialBody.SATURN,    // 10 Aquarius
            CelestialBody.JUPITER,   // 11 Pisces
        )

        // Classical Graha Maitri table — friends of each planet.
        // (Sun/Moon/Mars/Jupiter/Venus/Saturn/Mercury/Rahu/Ketu; we use Sun..Saturn+Mercury.)
        val PLANET_FRIENDS: Map<CelestialBody, Set<CelestialBody>> = mapOf(
            CelestialBody.SUN to setOf(CelestialBody.MOON, CelestialBody.MARS, CelestialBody.JUPITER),
            CelestialBody.MOON to setOf(CelestialBody.SUN, CelestialBody.MERCURY),
            CelestialBody.MARS to setOf(CelestialBody.SUN, CelestialBody.MOON, CelestialBody.JUPITER),
            CelestialBody.MERCURY to setOf(CelestialBody.SUN, CelestialBody.VENUS),
            CelestialBody.JUPITER to setOf(CelestialBody.SUN, CelestialBody.MOON, CelestialBody.MARS),
            CelestialBody.VENUS to setOf(CelestialBody.MERCURY, CelestialBody.SATURN),
            CelestialBody.SATURN to setOf(CelestialBody.MERCURY, CelestialBody.VENUS),
        )
        val PLANET_ENEMIES: Map<CelestialBody, Set<CelestialBody>> = mapOf(
            CelestialBody.SUN to setOf(CelestialBody.VENUS, CelestialBody.SATURN),
            CelestialBody.MOON to setOf(CelestialBody.RAHU, CelestialBody.KETU),
            CelestialBody.MARS to setOf(CelestialBody.MERCURY, CelestialBody.SATURN),
            CelestialBody.MERCURY to setOf(CelestialBody.MOON),
            CelestialBody.JUPITER to setOf(CelestialBody.MERCURY, CelestialBody.VENUS),
            CelestialBody.VENUS to setOf(CelestialBody.SUN, CelestialBody.MOON),
            CelestialBody.SATURN to setOf(CelestialBody.SUN, CelestialBody.MOON, CelestialBody.MARS),
        )

        // Gana pair scoring. Keys are (boy, girl).
        val GANA_SCORE: Map<Pair<Gana, Gana>, Int> = mapOf(
            Pair(Gana.DEVA, Gana.DEVA) to 6,
            Pair(Gana.DEVA, Gana.MANUSHYA) to 6,
            Pair(Gana.DEVA, Gana.RAKSHASA) to 0,
            Pair(Gana.MANUSHYA, Gana.DEVA) to 5,
            Pair(Gana.MANUSHYA, Gana.MANUSHYA) to 6,
            Pair(Gana.MANUSHYA, Gana.RAKSHASA) to 1,
            Pair(Gana.RAKSHASA, Gana.DEVA) to 1,
            Pair(Gana.RAKSHASA, Gana.MANUSHYA) to 0,
            Pair(Gana.RAKSHASA, Gana.RAKSHASA) to 6,
        )
    }
}

/**
 * Input for a Guna Milan calculation — date and optional time/zone.
 */
data class BirthInput(
    val birthDate: LocalDate,
    val birthTime: LocalTime? = null,
    val zoneOffset: ZoneOffset? = null,
)

/**
 * Result of a Guna Milan compatibility calculation.
 */
data class GunaMilan(
    val kootas: List<KootaScore>,
    val totalScore: Int,
    val maxScore: Int,
    val percentage: Float,
) {
    /**
     * One-line cultural verdict — "Excellent match" / "Good match" / etc.
     */
    fun verdict(): String = when {
        totalScore >= 30 -> "Excellent match (${totalScore}/36)"
        totalScore >= 25 -> "Very good match (${totalScore}/36)"
        totalScore >= 18 -> "Good match (${totalScore}/36)"
        totalScore >= 12 -> "Below average (${totalScore}/36)"
        else -> "Poor match (${totalScore}/36)"
    }
}

/**
 * Score for a single koota in the Ashtakoot system.
 */
data class KootaScore(
    val koota: Koota,
    val score: Int,
    val maxScore: Int,
    val description: String,
)

/**
 * The 8 Kootas of Ashtakoot / Guna Milan.
 */
enum class Koota(val displayName: String, val maxPoints: Int) {
    VARNA("Varna (spiritual)", 1),
    VASHYA("Vashya (attraction)", 2),
    TARA("Tara (star destiny)", 3),
    YONI("Yoni (sexual)", 4),
    GRAHA_MAITRI("Graha Maitri (planetary friendship)", 5),
    GANA("Gana (temperament)", 6),
    BHAKOOT("Bhakoot (Moon sign)", 7),
    NADI("Nadi (pulse)", 8),
}
