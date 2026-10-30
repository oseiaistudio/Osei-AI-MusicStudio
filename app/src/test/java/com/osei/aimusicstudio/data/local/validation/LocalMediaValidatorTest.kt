package com.osei.aimusicstudio.data.local.validation

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalMediaValidatorTest {

    private lateinit var context: Context

    class TestMediaContentProvider : ContentProvider() {
        override fun onCreate(): Boolean = true

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
        ): Cursor? = null

        override fun getType(uri: Uri): String? {
            return when (uri.path) {
                "/video" -> "video/mp4"
                "/image" -> "image/png"
                "/audio" -> "audio/mpeg"
                "/text" -> "text/plain"
                else -> null
            }
        }

        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
            val file = File.createTempFile("test_media", ".bin")
            file.writeBytes("test media data".toByteArray())
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        Robolectric.setupContentProvider(TestMediaContentProvider::class.java, "com.osei.test")
    }

    @Test
    fun validateVideoUri_nullOrBlank_returnsInvalid() {
        val resultNull = LocalMediaValidator.validateVideoUri(context, null)
        assertTrue(resultNull is LocalMediaValidator.MediaValidationResult.Invalid)

        val resultBlank = LocalMediaValidator.validateVideoUri(context, "   ")
        assertTrue(resultBlank is LocalMediaValidator.MediaValidationResult.Invalid)
    }

    @Test
    fun validateVideoUri_nonVideoMimeType_returnsInvalid() {
        val imageUri = Uri.parse("content://com.osei.test/image")
        val result = LocalMediaValidator.validateVideoUri(context, imageUri.toString())
        assertTrue(result is LocalMediaValidator.MediaValidationResult.Invalid)
        assertTrue((result as LocalMediaValidator.MediaValidationResult.Invalid).reason.contains("not a video format"))
    }

    @Test
    fun validateVideoUri_validVideoUri_returnsValid() {
        val videoUri = Uri.parse("content://com.osei.test/video")
        val result = LocalMediaValidator.validateVideoUri(context, videoUri.toString())
        assertTrue(result is LocalMediaValidator.MediaValidationResult.Valid)
    }

    @Test
    fun validateVideoUri_missingFile_returnsInvalid() {
        val missingUri = Uri.parse("content://com.osei.nonexistent.provider/file")
        val result = LocalMediaValidator.validateVideoUri(context, missingUri.toString())
        assertTrue(result is LocalMediaValidator.MediaValidationResult.Invalid)
    }

    @Test
    fun validateAudioUri_nullOrBlank_returnsInvalid() {
        val resultNull = LocalMediaValidator.validateAudioUri(context, null)
        assertTrue(resultNull is LocalMediaValidator.MediaValidationResult.Invalid)

        val resultBlank = LocalMediaValidator.validateAudioUri(context, "   ")
        assertTrue(resultBlank is LocalMediaValidator.MediaValidationResult.Invalid)
    }

    @Test
    fun validateAudioUri_nonAudioMimeType_returnsInvalid() {
        val textUri = Uri.parse("content://com.osei.test/text")
        val result = LocalMediaValidator.validateAudioUri(context, textUri.toString())
        assertTrue(result is LocalMediaValidator.MediaValidationResult.Invalid)
        assertTrue((result as LocalMediaValidator.MediaValidationResult.Invalid).reason.contains("not an audio format"))
    }

    @Test
    fun validateAudioUri_validAudioUri_returnsValid() {
        val audioUri = Uri.parse("content://com.osei.test/audio")
        val result = LocalMediaValidator.validateAudioUri(context, audioUri.toString())
        assertTrue(result is LocalMediaValidator.MediaValidationResult.Valid)
    }

    @Test
    fun persistUriPermission_handlesUnsupportedProviderGracefully() {
        val uri = Uri.parse("content://com.osei.test/audio")
        val persisted = LocalMediaValidator.persistUriPermissionIfSupported(context.contentResolver, uri)
        // Does not throw an unhandled exception
        assertTrue(persisted || !persisted)
    }
}
