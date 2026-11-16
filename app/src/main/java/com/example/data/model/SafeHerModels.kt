package com.example.data.model

enum class HelplineRegion(val labelEn: String, val labelTa: String) {
    NATIONAL("National (All India)", "தேசிய உதவி எண்கள்"),
    PUDUCHERRY("Puducherry UT", "புதுச்சேரி"),
    TAMIL_NADU("Tamil Nadu", "தமிழ்நாடு"),
    OTHER("Other State / NGO", "பிற மாநிலம் / தொண்டு நிறுவனங்கள்")
}

data class HelplineEntry(
    val id: String,
    val name: String,
    val number: String,
    val category: String,
    val region: HelplineRegion,
    val description: String,
    val isEmergencyStrip: Boolean = false
)

data class OrganizationEntry(
    val id: String,
    val name: String,
    val type: String, // "Government" or "NGO"
    val region: HelplineRegion,
    val location: String,
    val description: String,
    val badge: String // "Gov" or "NGO"
)

data class LegalRightEntry(
    val id: String,
    val title: String,
    val summary: String,
    val quickHelpline: String? = null
)

data class CaseStudyEntry(
    val id: String,
    val tag: String,
    val title: String,
    val summary: String
)

data class LearningModule(
    val id: String,
    val badgeId: String,
    val emoji: String,
    val titleEn: String,
    val titleTa: String,
    val tag: String,
    val shortDescEn: String,
    val meenaTipEn: String,
    val meenaTipTa: String,
    val keyPointsEn: List<String>,
    val rememberBox: String,
    val diagramType: String // "swimsuit", "pants", "cyber", "circle", "consent", "secrets"
)

data class QuizQuestion(
    val id: Int,
    val sceneEmoji: String,
    val questionEn: String,
    val questionTa: String,
    val correctLabel: String, // "Good Touch", "Bad Touch", "Not Safe"
    val explanationEn: String
)

data class SafeZoneEntry(
    val id: String,
    val name: String,
    val region: HelplineRegion,
    val zoneType: String, // "Police", "One Stop Centre", "Hospital"
    val phone: String,
    val details: String,
    val lat: Double?,
    val lon: Double?
)
