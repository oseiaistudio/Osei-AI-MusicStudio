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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import com.osei.aimusicstudio.ui.theme.StudioGhanaGold
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary
import com.osei.aimusicstudio.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
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
            .testTag("privacy_policy_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("privacy_back_btn")
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
            // Header summary
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
                        text = "Commitment to Data Privacy",
                        style = MaterialTheme.typography.titleSmall,
                        color = StudioSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Effective Date: October 2026\n" +
                                "Osei AI Music Studio respects your privacy and is designed with an offline-first, zero-surveillance architecture. This policy details our exact data practices based on the app's real functionality.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Section 1: Information Collected
            PrivacySectionCard(
                icon = Icons.Default.ManageAccounts,
                title = "1. Information the App Collects",
                content = "• User Profile Information: When you sign in using Google Sign-In or Firebase Authentication, the app receives your verified UID, display name, and email address. This is used solely to associate your projects with your account.\n\n" +
                        "• Music Project Metadata: Song titles, selected genres, moods, tempos, arrangement preferences, and custom lyrics that you author are saved in the app's local SQLite database.\n\n" +
                        "• Video Project Configurations: Visual prompts, aspect ratios, linked song IDs, and visual styles configured in the Video Studio are stored as local drafts."
            )

            // Section 2: Local Audio & Video Access
            PrivacySectionCard(
                icon = Icons.Default.FolderShared,
                title = "2. Local Audio & Video File Access",
                content = "• Zero-Permission Document Picker: The app never requests broad storage permissions (such as READ_EXTERNAL_STORAGE). Instead, it uses Android's Storage Access Framework (SAF) system picker.\n\n" +
                        "• Isolated Scope: The app only gains access to the specific audio or video file that you explicitly choose in the picker dialog.\n\n" +
                        "• Persistable URI Access: A persistable read-only grant is retained so that your linked media remains accessible when you reload your draft on your device. The app never scans, indexes, or modifies unselected media."
            )

            // Section 3: Cloud Synchronization
            PrivacySectionCard(
                icon = Icons.Default.CloudDone,
                title = "3. Optional Cloud Synchronization",
                content = "• Default Local Mode: Cloud sync is disabled by default. All data stays strictly on your local device.\n\n" +
                        "• User-Controlled Cloud Replication: If you toggle 'Cloud Sync' ON in Profile Settings, your saved songs and user profile are replicated to Cloud Firestore under strict security rules that isolate your records to your verified user ID.\n\n" +
                        "• Zero External AI Inference: AI music and video generation endpoints are disabled. No audio or video data is sent to external third-party generative models."
            )

            // Section 4: Permission Controls
            PrivacySectionCard(
                icon = Icons.Default.Security,
                title = "4. User Control & Permissions",
                content = "• Storage Permissions: Because the app uses the system document picker, no dangerous runtime permissions need to be granted or monitored.\n\n" +
                        "• Revoking Access: You can revoke file picker permissions or reset app data at any time via Android Settings > Apps > Osei AI Music Studio > Permissions or Storage.\n\n" +
                        "• Offline Usage: You can disconnect from the internet or switch on Airplane Mode at any time; the app remains fully operational as a local arrangement studio."
            )

            // Section 5: Data Storage & Deletion
            PrivacySectionCard(
                icon = Icons.Default.Delete,
                title = "5. Data Storage & Deletion",
                content = "• On-Device Sandboxing: All local databases (Room SQLite) and preferences are stored within the app's private sandbox directory (/data/data/com.osei.aimusicstudio/). Other applications cannot access this data.\n\n" +
                        "• In-App Deletion: You can permanently delete any song draft, video project, or audio project directly inside the app at any time.\n\n" +
                        "• Total Erasure: Clearing app storage or uninstalling the app permanently purges all local databases, cached metadata, and persistable URI grants from your device."
            )

            // Section 6: Contact Information
            PrivacySectionCard(
                icon = Icons.Default.Phone,
                title = "6. Contact & Privacy Inquiries",
                content = "If you have questions regarding this Privacy Policy or your data rights, please contact the developer:\n\n" +
                        "• Publisher: ${AppLegalInfo.DEVELOPER_PUBLISHER}\n" +
                        "• Email: ${AppLegalInfo.SUPPORT_EMAIL}\n" +
                        "• Phone / WhatsApp: ${AppLegalInfo.SUPPORT_PHONE_FORMATTED}\n" +
                        "• Location: ${AppLegalInfo.SUPPORT_LOCATION}"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PrivacySectionCard(
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
