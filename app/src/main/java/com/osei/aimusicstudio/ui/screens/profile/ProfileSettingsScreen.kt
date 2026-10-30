package com.osei.aimusicstudio.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import com.osei.aimusicstudio.auth.AuthViewModel
import com.osei.aimusicstudio.core.UiState
import com.osei.aimusicstudio.data.local.preferences.StudioPreferences
import com.osei.aimusicstudio.data.model.StudioConstants
import com.osei.aimusicstudio.ui.components.PhaseIndicatorBadge
import com.osei.aimusicstudio.ui.theme.DarkGray900
import com.osei.aimusicstudio.ui.theme.StudioBackground
import com.osei.aimusicstudio.ui.theme.StudioBorder
import com.osei.aimusicstudio.ui.theme.StudioCardSurface
import com.osei.aimusicstudio.ui.theme.StudioCardSurfaceVariant
import com.osei.aimusicstudio.ui.theme.StudioGhanaGreen
import com.osei.aimusicstudio.ui.theme.StudioGhanaRed
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary
import com.osei.aimusicstudio.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    user: FirebaseUser?,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPricing: () -> Unit,
    onSignOutComplete: () -> Unit,
    studioPreferences: StudioPreferences? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = studioPreferences ?: remember { StudioPreferences(context) }
    val isCloudSyncEnabled by prefs.isCloudSyncEnabled.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()
    val scrollState = rememberScrollState()

    val profile = (profileState as? UiState.Success)?.data

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("profile_settings_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Profile & Studio Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // User Avatar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF231145), StudioCardSurface)
                        )
                    )
                    .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(StudioPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.displayName ?: "O").take(1).uppercase(),
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user?.displayName ?: "Osei Music Creator",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = user?.email ?: "Google Authenticated Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StudioGhanaGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active Session • UID: ${user?.uid?.take(8) ?: "Local"}...",
                                color = StudioGhanaGreen,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Studio Credits Balance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioTertiary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Generation Credits",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile?.credits ?: 50} Credits",
                            color = StudioTertiary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Plan: ${profile?.subscriptionTier?.replace("_", " ")?.uppercase() ?: "FREE STARTER"}",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onNavigateToPricing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioTertiary,
                            contentColor = StudioBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("manage_credits_btn")
                    ) {
                        Text("Plans", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Studio Preferences
            Text(
                text = "Default Studio Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PreferenceSelectorRow(
                    title = "Default Language",
                    value = profile?.preferredLanguage ?: "English",
                    onClick = {
                        profileViewModel.updatePreferences(
                            if (profile?.preferredLanguage == "English") "Twi" else "English",
                            profile?.preferredGenre ?: "Ghanaian Highlife"
                        )
                    }
                )
                PreferenceSelectorRow(
                    title = "Default Genre",
                    value = profile?.preferredGenre ?: "Ghanaian Highlife",
                    onClick = {
                        profileViewModel.updatePreferences(
                            profile?.preferredLanguage ?: "English",
                            if (profile?.preferredGenre == "Ghanaian Highlife") "Afrobeats" else "Ghanaian Highlife"
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Studio Persistence & Sync Mode
            Text(
                text = "Storage & Synchronization",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Local-Only Mode saves song drafts directly to Room SQLite without contacting Firestore (Budget GH₵0). Cloud sync is optional.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCloudSyncEnabled) "Cloud Sync Enabled" else "Local-Only Mode (Offline)",
                            color = if (isCloudSyncEnabled) StudioGhanaGreen else StudioSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isCloudSyncEnabled) {
                                "Changes replicate to Cloud Firestore under verified user session."
                            } else {
                                "Room Database only. Zero Firestore cloud calls are made."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isCloudSyncEnabled,
                        onCheckedChange = { prefs.setCloudSyncEnabled(it) },
                        modifier = Modifier.testTag("cloud_sync_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Backend Architecture Status (Supabase preparation)
            Text(
                text = "Backend & Media Architecture",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArchitectureStatusRow(
                        title = "Firestore Multi-User Isolation",
                        status = "Active & Enforced",
                        isLive = true
                    )
                    ArchitectureStatusRow(
                        title = "Google Sign-In Credential Manager",
                        status = "Active",
                        isLive = true
                    )
                    ArchitectureStatusRow(
                        title = "Media3 ExoPlayer Streaming",
                        status = "Active Engine",
                        isLive = true
                    )
                    ArchitectureStatusRow(
                        title = "Supabase Neural Music API",
                        status = "Prepared for Phase 2",
                        isLive = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out Button
            OutlinedButton(
                onClick = {
                    authViewModel.signOut(context) {
                        onSignOutComplete()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = StudioGhanaRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioGhanaRed.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_sign_out_btn")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Osei Studio", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PreferenceSelectorRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(StudioCardSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = TextPrimary, fontSize = 14.sp)
        Text(text = "$value (Tap to toggle)", color = StudioSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ArchitectureStatusRow(
    title: String,
    status: String,
    isLive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = TextSecondary, fontSize = 13.sp)
        Text(
            text = status,
            color = if (isLive) StudioGhanaGreen else StudioTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
