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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.osei.aimusicstudio.ui.theme.StudioCardSurfaceVariant
import com.osei.aimusicstudio.ui.theme.StudioGhanaGold
import com.osei.aimusicstudio.ui.theme.StudioGhanaGreen
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary
import com.osei.aimusicstudio.ui.theme.TextTertiary
import com.osei.aimusicstudio.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToCopyright: () -> Unit,
    onNavigateToContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("about_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("about_back_btn")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // App Logo
            OseiLogoBadge(size = 80.dp, showText = false)

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = AppLegalInfo.APP_NAME,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tagline / Description
            Text(
                text = AppLegalInfo.APP_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = StudioSecondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Build & Identification Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AboutInfoRow(
                        label = "Developer & Publisher",
                        value = AppLegalInfo.DEVELOPER_PUBLISHER,
                        accentColor = StudioGhanaGold
                    )
                    AboutInfoRow(
                        label = "Package Name",
                        value = AppLegalInfo.PACKAGE_NAME,
                        accentColor = StudioSecondary
                    )
                    AboutInfoRow(
                        label = "Build Version",
                        value = AppLegalInfo.formatVersion(),
                        accentColor = StudioPrimary
                    )
                    AboutInfoRow(
                        label = "Operating Mode",
                        value = "Offline-First Local SQLite (GH₵0 Budget)",
                        accentColor = StudioGhanaGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Transparent Architectural Disclosure Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardSurfaceVariant)
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = StudioTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Transparent AI Status Notice",
                            color = StudioTertiary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Osei AI Music Studio operates with full transparency. Cloud neural music synthesis (Google Lyria/Vertex AI) is currently disabled to maintain a strict GH₵0 development budget. All song arrangements, lyrics, and video project drafts are securely created and stored on-device in Room SQLite. No fake audio or mock progress is ever generated.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Links Section
            Text(
                text = "Legal & Support",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            AboutNavigationCard(
                icon = Icons.Default.Shield,
                title = "Privacy Policy",
                subtitle = "How your local files and profile data are protected",
                onClick = onNavigateToPrivacy,
                testTag = "about_privacy_link"
            )

            Spacer(modifier = Modifier.height(10.dp))

            AboutNavigationCard(
                icon = Icons.Default.Policy,
                title = "Terms & Conditions",
                subtitle = "Usage terms, media responsibility and IP guidelines",
                onClick = onNavigateToTerms,
                testTag = "about_terms_link"
            )

            Spacer(modifier = Modifier.height(10.dp))

            AboutNavigationCard(
                icon = Icons.Default.Copyright,
                title = "Copyright Notice",
                subtitle = "Intellectual property and third-party attribution",
                onClick = onNavigateToCopyright,
                testTag = "about_copyright_link"
            )

            Spacer(modifier = Modifier.height(10.dp))

            AboutNavigationCard(
                icon = Icons.Default.SupportAgent,
                title = "Contact & Support",
                subtitle = "Get assistance, feedback, or developer inquiry",
                onClick = onNavigateToContact,
                testTag = "about_contact_link"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Share App Action Button
            Button(
                onClick = { ShareHelper.shareApp(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioPrimary,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("about_share_btn")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share App")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Osei AI Music Studio",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun AboutInfoRow(
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = accentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun AboutNavigationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioCardSurface)
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            .testTag(testTag)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StudioPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StudioSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextTertiary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate to $title",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
