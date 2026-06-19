package com.streamapp.ui.util

// Country code to regional indicator emoji conversion
fun getCountryFlag(countryCode: String?): String {
    if (countryCode.isNullOrBlank() || countryCode.length != 2) return "🌍"
    
    val code = countryCode.uppercase()
    val codePoints = IntArray(2)
    codePoints[0] = 0x1F1E6 + (code[0] - 'A')
    codePoints[1] = 0x1F1E6 + (code[1] - 'A')
    return String(codePoints, 0, 2)
}

// Common team country mappings
fun getTeamCountryCode(teamName: String): String {
    return when (teamName.lowercase()) {
        // Cricket
        "india" -> "IN"
        "afghanistan" -> "AF"
        "england" -> "GB"
        "new zealand" -> "NZ"
        "south africa" -> "ZA"
        "pakistan" -> "PK"
        "australia" -> "AU"
        "bangladesh" -> "BD"
        "west indies" -> "WI"
        "ireland" -> "IE"
        "sri lanka" -> "LK"
        
        // Football
        "portugal" -> "PT"
        "dr congo" -> "CD"
        "france" -> "FR"
        "germany" -> "DE"
        "spain" -> "ES"
        "italy" -> "IT"
        "netherlands" -> "NL"
        "belgium" -> "BE"
        "argentina" -> "AR"
        "brazil" -> "BR"
        "mexico" -> "MX"
        
        // Default
        else -> "UN"
    }
}
