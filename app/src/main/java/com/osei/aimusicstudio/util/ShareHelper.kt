package com.osei.aimusicstudio.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.osei.aimusicstudio.domain.legal.AppLegalInfo

object ShareHelper {

    /**
     * Builds the underlying ACTION_SEND Intent with plain text.
     */
    fun createSendIntent(shareText: String = AppLegalInfo.getShareContent()): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, AppLegalInfo.APP_NAME)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
    }

    /**
     * Wraps the send intent in an Android system chooser for the Sharesheet.
     */
    fun createChooserIntent(
        context: Context,
        shareText: String = AppLegalInfo.getShareContent(),
        chooserTitle: String = "Share ${AppLegalInfo.APP_NAME}"
    ): Intent {
        val sendIntent = createSendIntent(shareText)
        return Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Launches the Android Sharesheet.
     */
    fun shareApp(context: Context, shareText: String = AppLegalInfo.getShareContent()) {
        try {
            val chooser = createChooserIntent(context, shareText)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open share menu: ${e.localizedMessage ?: "Unknown error"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
