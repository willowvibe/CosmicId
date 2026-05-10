package com.willowvibe.agereveal.data.model

import java.time.LocalDate

/** A celebrity with matching birthday (month+day). */
data class CelebrityMatch(
    val name: String,
    /** Full birth date of the celebrity (year included for display). */
    val birthDate: LocalDate,
    val category: String,
)
