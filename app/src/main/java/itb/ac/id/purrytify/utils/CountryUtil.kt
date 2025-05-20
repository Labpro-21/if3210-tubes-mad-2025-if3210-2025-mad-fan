package itb.ac.id.purrytify.utils

import java.util.Locale

object CountryUtils {
    fun getAllCountries(): List<Country> {
        return Locale.getISOCountries().map { countryCode ->
            val locale = Locale("", countryCode)
            Country(
                code = countryCode,
                name = locale.displayCountry
            )
        }.sortedBy { it.name }
    }

    fun getCountryNameByCode(code: String): String {
        if (code.isBlank()) return "Unknown"
        return try {
            val locale = Locale("", code)
            locale.displayCountry.takeIf { it.isNotBlank() } ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

data class Country(val code: String, val name: String)