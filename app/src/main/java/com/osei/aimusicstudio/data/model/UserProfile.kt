package com.osei.aimusicstudio.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val credits: Long = 50L,
    val subscriptionTier: String = "free",
    val preferredLanguage: String = "English",
    val preferredGenre: String = "Ghanaian Highlife",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "displayName" to displayName,
            "email" to email,
            "credits" to credits,
            "subscriptionTier" to subscriptionTier,
            "preferredLanguage" to preferredLanguage,
            "preferredGenre" to preferredGenre
        )
        avatarUrl?.let { map["avatarUrl"] = it }
        return map
    }
}
