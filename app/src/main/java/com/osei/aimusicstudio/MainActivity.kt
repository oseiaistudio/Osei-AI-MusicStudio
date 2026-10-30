package com.osei.aimusicstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.osei.aimusicstudio.auth.AuthViewModel
import com.osei.aimusicstudio.ui.navigation.AppNavigation
import com.osei.aimusicstudio.ui.theme.OseiAIMusicStudioTheme
import com.osei.aimusicstudio.ui.theme.StudioBackground

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            // Initiate silent auto sign-in non-blocking after first composition
            LaunchedEffect(Unit) {
                authViewModel.initAutoSignIn(this@MainActivity)
            }

            OseiAIMusicStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioBackground
                ) {
                    AppNavigation(authViewModel = authViewModel)
                }
            }
        }
    }
}
