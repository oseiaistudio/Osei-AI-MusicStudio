package com.osei.aimusicstudio.legal

import android.content.Intent
import com.osei.aimusicstudio.domain.legal.AppLegalInfo
import com.osei.aimusicstudio.util.ShareHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppLegalInfoTest {

    @Test
    fun appMetadata_matchesSpecification() {
        assertEquals("Osei AI Music Studio", AppLegalInfo.APP_NAME)
        assertEquals("Create, organize and enjoy your music projects.", AppLegalInfo.APP_DESCRIPTION)
        assertEquals("com.osei.aimusicstudio", AppLegalInfo.PACKAGE_NAME)
        assertEquals("Osei Studio", AppLegalInfo.DEVELOPER_PUBLISHER)
        assertEquals("© 2026 Osei AI Music Studio. All rights reserved.", AppLegalInfo.COPYRIGHT_NOTICE)
    }

    @Test
    fun contactDetails_areConfiguredAccurately() {
        assertEquals("233245200758", AppLegalInfo.SUPPORT_PHONE_RAW)
        assertEquals("+233 24 520 0758", AppLegalInfo.SUPPORT_PHONE_FORMATTED)
        assertEquals("oseiaistudio@gmail.com", AppLegalInfo.SUPPORT_EMAIL)
        assertTrue(AppLegalInfo.SUPPORT_LOCATION.contains("Ghana"))
    }

    @Test
    fun formatVersion_formatsCorrectly() {
        val formatted = AppLegalInfo.formatVersion("1.0", 1)
        assertEquals("Version 1.0 (Build 1)", formatted)
    }

    @Test
    fun shareContent_containsRequiredText() {
        val shareText = AppLegalInfo.getShareContent()
        assertEquals(
            "Check out Osei AI Music Studio! Create and organize your music projects.",
            shareText
        )
        assertFalse(shareText.contains("http://fake") || shareText.contains("https://fake"))
    }

    @Test
    fun createSendIntent_createsValidAndroidSharesheetIntent() {
        val intent = ShareHelper.createSendIntent()

        assertNotNull(intent)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals(AppLegalInfo.APP_NAME, intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(
            "Check out Osei AI Music Studio! Create and organize your music projects.",
            intent.getStringExtra(Intent.EXTRA_TEXT)
        )
    }
}
