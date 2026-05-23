package com.willowvibe.agereveal.domain.model

/**
 * Celestial body used in astrological and astronomical calculations.
 * Consolidates [AstronomicalCalculator.Planet] (for orbital elements) and
 * [PlanetAgeCalculator.Planet] (for display) into a single source of truth.
 */
enum class CelestialBody(
    val displayName: String,
    val emoji: String,
    val orbitalPeriodEarthYears: Double?,
    val meanAphelionAU: Double?,
    val meanPerihelionAU: Double?,
) {
    /**
     * Mercury - innermost planet.
     * Orbital period: 0.2408 years
     * Aphelion: 0.4667 AU, Perihelion: 0.3075 AU
     */
    MERCURY(
        displayName = "Mercury",
        emoji = "🫁",
        orbitalPeriodEarthYears = 0.2408,
        meanAphelionAU = 0.4667,
        meanPerihelionAU = 0.3075,
    ),

    /**
     * Venus - Earth's sister planet.
     * Orbital period: 0.6152 years
     * Aphelion: 0.7282 AU, Perihelion: 0.7184 AU
     */
    VENUS(
        displayName = "Venus",
        emoji = "💋",
        orbitalPeriodEarthYears = 0.6152,
        meanAphelionAU = 0.7282,
        meanPerihelionAU = 0.7184,
    ),

    /**
     * Mars - the Red Planet.
     * Orbital period: 1.8809 years
     * Aphelion: 1.666 AU, Perihelion: 1.381 AU
     */
    MARS(
        displayName = "Mars",
        emoji = "🚀",
        orbitalPeriodEarthYears = 1.8809,
        meanAphelionAU = 1.666,
        meanPerihelionAU = 1.381,
    ),

    /**
     * Jupiter - the Gas Giant.
     * Orbital period: 11.8626 years
     * Aphelion: 5.455 AU, Perihelion: 4.950 AU
     */
    JUPITER(
        displayName = "Jupiter",
        emoji = "🥇",
        orbitalPeriodEarthYears = 11.8626,
        meanAphelionAU = 5.455,
        meanPerihelionAU = 4.950,
    ),

    /**
     * Saturn - the Ringed Planet.
     * Orbital period: 29.4571 years
     * Aphelion: 10.12 AU, Perihelion: 9.04 AU
     */
    SATURN(
        displayName = "Saturn",
        emoji = "🟂",
        orbitalPeriodEarthYears = 29.4571,
        meanAphelionAU = 10.12,
        meanPerihelionAU = 9.04,
    ),

    /**
     * Uranus - the Ice Giant.
     * Orbital period: 84.0205 years
     * Aphelion: 20.11 AU, Perihelion: 18.29 AU
     */
    URANUS(
        displayName = "Uranus",
        emoji = "🧊",
        orbitalPeriodEarthYears = 84.0205,
        meanAphelionAU = 20.11,
        meanPerihelionAU = 18.29,
    ),

    /**
     * Neptune - the Windy Planet.
     * Orbital period: 164.8 years
     * Aphelion: 30.33 AU, Perihelion: 29.81 AU
     */
    NEPTUNE(
        displayName = "Neptune",
        emoji = "🐠",
        orbitalPeriodEarthYears = 164.8,
        meanAphelionAU = 30.33,
        meanPerihelionAU = 29.81,
    ),

    /**
     * Pluto - the Dwarf Planet (still beloved!).
     * Orbital period: 248.0 years
     * Aphelion: 49.31 AU, Perihelion: 29.66 AU
     */
    PLUTO(
        displayName = "Pluto",
        emoji = "🥽",
        orbitalPeriodEarthYears = 248.0,
        meanAphelionAU = 49.31,
        meanPerihelionAU = 29.66,
    );

    companion object {
        /** Get all celestial bodies in order from the Sun outward. */
        val all: List<CelestialBody> by lazy { values().toList() }
    }
}
