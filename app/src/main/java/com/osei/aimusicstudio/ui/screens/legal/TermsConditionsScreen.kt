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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.domain.legal.AppLegalInfo
import com.osei.aimusicstudio.ui.theme.StudioBackground
import com.osei.aimusicstudio.ui.theme.StudioBorder
import com.osei.aimusicstudio.ui.theme.StudioCardSurface
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(
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
            .testTag("terms_conditions_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("terms_back_btn")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introductory Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Terms of Service Agreement",
                        style = MaterialTheme.typography.titleSmall,
                        color = StudioSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Welcome to ${AppLegalInfo.APP_NAME}. By downloading, installing, or using this application, you agree to be bound by the following terms. Please read them carefully before using the studio.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Section 1: Acceptable App Usage
            TermsSectionCard(
                icon = Icons.Default.AssignmentTurnedIn,
                title = "1. Acceptable App Usage",
                content = "• Creative Purpose: Osei AI Music Studio is designed for songwriting, musical arrangement drafting, lyrics composition, and video project planning.\n\n" +
                        "• Lawful Conduct: You agree not to use the app for any illegal purposes, including creating or distributing defamatory, infringing, or abusive content.\n\n" +
                        "• System Integrity: You must not attempt to decompile, reverse-engineer, exploit, or bypass the application's local security boundaries or data storage mechanisms."
            )

            // Section 2: Media Responsibility
            TermsSectionCard(
                icon = Icons.Default.ReportProblem,
                title = "2. User Responsibility for Imported Media",
                content = "• User Control: You alone select and import local audio files and video clips into your studio projects via the Android document picker.\n\n" +
                        "• Rights & Clearances: You represent and warrant that you own or possess all necessary rights, licenses, and clearances for any sound recording, musical work, or video file that you select or reference in this app.\n\n" +
                        "• Indemnification: Osei Studio assumes no liability for any unauthorized or infringing media files selected from your device storage."
            )

            // Section 3: Copyright Responsibility
            TermsSectionCard(
                icon = Icons.Default.Copyright,
                title = "3. Copyright & Intellectual Property",
                content = "• User-Created Content: You retain full ownership and intellectual property rights in original lyrics, song titles, and arrangement ideas that you author in the studio.\n\n" +
                        "• App Proprietary Assets: The app name, logos, UI designs, code, and branding are the exclusive property of Osei Studio.\n\n" +
                        "• Third-Party Assets: Open-source libraries and platform frameworks belong to their respective copyright holders."
            )

            // Section 4: Third-Party Service Limitations
            TermsSectionCard(
                icon = Icons.Default.Gavel,
                title = "4. Third-Party Service Limitations",
                content = "• Optional Cloud Providers: The app may interact with Google Firebase for authentication and optional cloud sync. Osei Studio is not liable for disruptions, downtime, or policy changes by third-party infrastructure providers.\n\n" +
                        "• As-Is Availability: The application is provided on an 'AS-IS' and 'AS-AVAILABLE' basis without warranties of any kind."
            )

            // Section 5: No Guarantee of Unavailable AI Generation
            TermsSectionCard(
                icon = Icons.Default.Warning,
                title = "5. No Guarantee of Unavailable AI Generation",
                content = "• Active Studio Mode: The current release functions as a local arrangement, lyrics editing, and project drafting studio under a strict GH₵0 development budget.\n\n" +
                        "• Disabled Generative Cloud APIs: Real-time neural audio and video generation through cloud foundation models (Vertex AI/Lyria) is currently deactivated. Osei Studio does not guarantee the availability, timing, or performance of external AI synthesis features.\n\n" +
                        "• No False Guarantees: The app transparently labels all created records as local drafts and will never falsely represent unrendered drafts as completed media."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TermsSectionCard(
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
