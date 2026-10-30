package com.osei.aimusicstudio.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SecureAuthSessionTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var secureAuth: SecureAuthSession

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.osei.aimusicstudio")
                    .setProjectId("demo-no-project")
                    .setApiKey("fake-api-key")
                    .build()
            )
        } else {
            FirebaseApp.getInstance()
        }
        auth = FirebaseAuth.getInstance(app)
        auth.signOut()
        secureAuth = SecureAuthSession(auth)
    }

    @Test
    fun verifySession_unauthenticatedUser_returnsUnauthenticated() = runBlocking {
        auth.signOut()
        val result = secureAuth.verifySession()
        assertTrue(result is IdentityVerificationResult.Unauthenticated)
    }

    @Test
    fun requireVerifiedUid_unauthenticatedUser_throwsSecurityException() = runBlocking {
        auth.signOut()
        try {
            secureAuth.requireVerifiedUid()
            fail("Expected SecurityException to be thrown when user is not authenticated")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Access denied") == true)
        }
    }

    @Test
    fun isVerified_unauthenticatedUser_returnsFalse() = runBlocking {
        auth.signOut()
        assertFalse(secureAuth.isVerified())
    }

    @Test
    fun withVerifiedUser_unauthenticated_failsWithSecurityException() = runBlocking {
        auth.signOut()
        val result = secureAuth.withVerifiedUser { session ->
            session.uid
        }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
