package com.osei.aimusicstudio.ui.screens.legal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.domain.legal.AppLegalInfo
import com.osei.aimusicstudio.ui.components.OseiLogoBadge
import com.osei.aimusicstudio.ui.theme.StudioBackground
import com.osei.aimusicstudio.ui.theme.StudioBorder
import com.osei.aimusicstudio.ui.theme.StudioCardSurface
import com.osei.aimusicstudio.ui.theme.StudioGhanaGold
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyrightScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("copyright_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Copyright & IP",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("copyright_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = StudioBackground
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            OseiLogoBadge(size = 72.dp, showText = false)

            Text(
                text = AppLegalInfo.COPYRIGHT_NOTICE,
                style = MaterialTheme.typography.titleMedium,
                color = StudioGhanaGold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Developer / Publisher: ${AppLegalInfo.DEVELOPER_PUBLISHER}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notice 1: Proprietary Rights & Redistribution Restrictions
            CopyrightCard(
                icon = Icons.Default.Lock,
                title = "Proprietary Rights & Brand Protection",
                content = "The Osei AI Music Studio name, custom Ghana-inspired branding, graphic design assets, visual layouts, and underlying compiled code are protected by copyright, trademark, and intellectual property laws.\n\n" +
                        "Users must not copy, modify, distribute, sell, reverse engineer, or redistribute this application or its proprietary branding without express written permission from Osei Studio."
            )

            // Notice 2: User Creative Ownership
            CopyrightCard(
                icon = Icons.Default.Palette,
                title = "User Creative Ownership",
                content = "Osei Studio makes no claim of ownership over user-created music, song titles, custom lyrics, vocal ideas, or artistic arrangements authored in this application.\n\n" +
                        "You retain 100% intellectual property ownership of your original artistic contributions created using this studio."
            )

            // Notice 3: Third-Party Libraries & Open Source
            CopyrightCard(
                icon = Icons.Default.LibraryMusic,
                title = "Third-Party Libraries & Attribution",
                content = "Osei AI Music Studio proudly utilizes industry-standard open-source Android libraries and components, which remain the copyright of their respective authors:\n\n" +
                        "• Android Jetpack & Compose (Apache 2.0 - Google LLC)\n" +
                        "• AndroidX Media3 ExoPlayer (Apache 2.0 - Google LLC)\n" +
                        "• Room SQLite Persistence (Apache 2.0 - Google LLC)\n" +
                        "• Kotlin & Coroutines (Apache 2.0 - JetBrains s.r.o.)\n" +
                        "• Material Design Icons & Symbols (Apache 2.0)\n" +
                        "• Firebase Android SDK (Apache 2.0 - Google LLC)\n\n" +
                        "All third-party trademarks and logos belong to their respective registered owners."
            )

            // Notice 4: Imported Audio & Video Content
            CopyrightCard(
                icon = Icons.Default.Business,
                title = "Imported Audio & Video Content",
                content = "This app enables users to import external audio and video files via the device's document picker. Osei Studio does not own, license, or host external sound recordings or video files selected by users.\n\n" +
                        "Users are strictly responsible for respecting the copyright and licenses of any external media brought into the studio."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CopyrightCard(
    icon: ImageVector,
    title: String,
    content: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioCardSurface)
            .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = StudioSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
