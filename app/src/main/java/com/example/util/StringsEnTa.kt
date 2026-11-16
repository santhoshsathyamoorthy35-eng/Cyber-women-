package com.example.util

enum class AppLanguage(val code: String, val displayLabel: String) {
    EN("en", "EN"),
    TA("ta", "தமிழ்")
}

object AppStrings {
    fun brandTagline(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Intelligent Safety · Real-Time Protection · Empowerment"
        AppLanguage.TA -> "அறிவார்ந்த பாதுகாப்பு · உடனடி காப்பு · பெண்கள் வலுவூட்டல்"
    }

    fun navHome(lang: AppLanguage) = if (lang == AppLanguage.EN) "Home" else "முகப்பு"
    fun navMap(lang: AppLanguage) = if (lang == AppLanguage.EN) "Safety Map" else "பாதுகாப்பு வரைபடம்"
    fun navSos(lang: AppLanguage) = if (lang == AppLanguage.EN) "Shield SOS" else "கவசம் SOS"
    fun navHub(lang: AppLanguage) = if (lang == AppLanguage.EN) "Safety Hub" else "உதவி மையம்"
    fun navProfile(lang: AppLanguage) = if (lang == AppLanguage.EN) "Profile" else "சுயவிவரம்"

    fun shieldProtocolTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Shield Protocol · Time-Risk Advisory"
        AppLanguage.TA -> "கவச நெறிமுறை · நேர-ஆபத்து எச்சரிக்கை"
    }

    fun sosHoldHint(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Hold 1.5s or tap for 5s countdown · Direct 112 & Contacts"
        AppLanguage.TA -> "1.5 வினாடி அழுத்தவும் அல்லது தட்டவும் · 112 & நம்பகமானவர்கள்"
    }

    fun callEmergency112(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Call 112 Now"
        AppLanguage.TA -> "112 அவசர அழைப்பு"
    }

    fun meenaWelcome(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "Hi, I'm Meena. Ask me about safety tips, legal rights, helplines, or area advisories."
        AppLanguage.TA -> "வணக்கம், நான் மீனா. பாதுகாப்பு வழிகள், சட்ட உரிமைகள் மற்றும் உதவி எண்கள் பற்றி கேளுங்கள்."
    }

    fun disclaimerSimulation(lang: AppLanguage) = when (lang) {
        AppLanguage.EN -> "CrimeMap 3D baseline uses 4,200 seeded synthetic incidents for Puducherry. Historical vulnerability advisories aggregate past patterns and do not predict individual crimes."
        AppLanguage.TA -> "புதுச்சேரி வரைபடம் மாதிரி தரவுகளைக் கொண்டுள்ளது. கடந்த கால நிகழ்வுகளின் அடிப்படையில் மட்டுமே ஆபத்து மண்டலங்கள் காட்டப்படுகின்றன."
    }
}
