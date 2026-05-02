package com.willowvibe.agereveal.data.model

/**
 * Observer location for precise astronomical calculations.
 *
 * @param latitude Degrees north of the equator (-90 to 90)
 * @param longitude Degrees east of the prime meridian (-180 to 180)
 * @param label Optional human-readable label (e.g. "Mumbai, India")
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String = "",
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be in [-90, 90]" }
        require(longitude in -180.0..180.0) { "Longitude must be in [-180, 180]" }
    }
}
