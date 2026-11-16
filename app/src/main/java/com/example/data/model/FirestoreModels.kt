package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val preferredLanguage: String = "en",
    val selectedCity: String = "Puducherry",
    val shakeToSosEnabled: Boolean = false,
    val customSosMessage: String = "EMERGENCY SOS: I need immediate help. Please check my shared location.",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TrustedContact(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "Family",
    val priority: Long = 1L,
    @get:PropertyName("isPrimary")
    @field:PropertyName("isPrimary")
    val isPrimary: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SosEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val status: String = "created",
    val triggerType: String = "button",
    val lat: Double = 11.9330,
    val lon: Double = 79.8260,
    val accuracyMeters: Double = 0.0,
    val locationLabel: String = "Puducherry",
    val message: String = "",
    val contactIds: List<String> = emptyList(),
    val deliveryNote: String = "Logged in app history; external SMS gateway not configured — call 112 for immediate dispatch.",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CitizenReport(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "",
    val description: String = "",
    val lat: Double = 11.9340,
    val lon: Double = 79.8300,
    val nearestStation: String = "Grand Bazaar PS",
    val severity: Long = 3L,
    val status: String = "received",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserFeedback(
    @DocumentId val id: String = "",
    val userId: String = "",
    val message: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
