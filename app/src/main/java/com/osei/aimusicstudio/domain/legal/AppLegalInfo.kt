package com.osei.aimusicstudio.domain.legal

import android.content.Context
import com.osei.aimusicstudio.BuildConfig

/**
 * Single source of truth for application metadata, legal disclosures,
 * contact details, and version formatting.
 *
 * All contact details and copyright statements are strictly configured here
 * without any hardcoded duplicates.
 */
object AppLegalInfo {

    const val APP_NAME = "Osei AI Music Studio"
    const val APP_DESCRIPTION = "Create, organize and enjoy your music projects."
    const val PACKAGE_NAME = "com.osei.aimusicstudio"
    const val DEVELOPER_PUBLISHER = "Osei Studio"
    const val COPYRIGHT_NOTICE = "© 2026 Osei AI Music Studio. All rights reserved."

    // Contact & Support Configuration
    const val SUPPORT_PHONE_FORMATTED = "+233 24 520 0758"
    const val SUPPORT_PHONE_RAW = "233245200758"
    const val SUPPORT_EMAIL = "oseiaistudio@gmail.com"
    const val SUPPORT_LOCATION = "Accra & Kumasi, Ghana / Global"

    // Android Sharesheet Configuration
    const val SHARE_MESSAGE = "Check out Osei AI Music Studio! Create and organize your music projects."

    // Verified Play Store link placeholder (null until verified in app store)
    val VERIFIED_STORE_URL: String? = null

    /**
     * Formats the app version from Android build configuration.
     */
    fun formatVersion(versionName: String = BuildConfig.VERSION_NAME, versionCode: Int = BuildConfig.VERSION_CODE): String {
        return "Version $versionName (Build $versionCode)"
    }

    /**
     * Resolves the text to share via Android Sharesheet.
     * Uses real Play Store URL only if verified; otherwise shares the app description and name.
     */
    fun getShareContent(): String {
        return if (!VERIFIED_STORE_URL.isNullOrBlank()) {
            "$SHARE_MESSAGE Download here: $VERIFIED_STORE_URL"
        } else {
            SHARE_MESSAGE
        }
    }
}
