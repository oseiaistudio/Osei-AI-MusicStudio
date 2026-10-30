package com.osei.aimusicstudio.ui.screens.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.osei.aimusicstudio.data.local.validation.LocalMediaValidator
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.data.model.VideoProject
import com.osei.aimusicstudio.ui.theme.DarkGray900
import com.osei.aimusicstudio.ui.theme.StudioBackground
import com.osei.aimusicstudio.ui.theme.StudioBorder
import com.osei.aimusicstudio.ui.theme.StudioCardSurface
import com.osei.aimusicstudio.ui.theme.StudioCardSurfaceVariant
import com.osei.aimusicstudio.ui.theme.StudioGhanaGold
import com.osei.aimusicstudio.ui.theme.StudioGhanaRed
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary
import com.osei.aimusicstudio.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VideoStudioScreen(
    viewModel: VideoStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    val videoProjects by viewModel.videoProjects.collectAsState()
    val availableSongs by viewModel.availableSongs.collectAsState()
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    var showSongPickerDropdown by remember { mutableStateOf(false) }
    var audioPickError by remember { mutableStateOf<String?>(null) }
    var videoPickError by remember { mutableStateOf<String?>(null) }
    var previewingProject by remember { mutableStateOf<VideoProject?>(null) }
    var playbackErrorMessage by remember { mutableStateOf<String?>(null) }

    // Safe system document picker for local audio with permission persistence
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            LocalMediaValidator.persistUriPermissionIfSupported(context.contentResolver, uri)
            val validation = LocalMediaValidator.validateAudioUri(context, uri.toString())
            if (validation is LocalMediaValidator.MediaValidationResult.Valid) {
                viewModel.setCustomAudioUri(uri.toString())
                audioPickError = null
            } else if (validation is LocalMediaValidator.MediaValidationResult.Invalid) {
                audioPickError = validation.reason
            }
        }
    }

    // Safe system document picker for local video with permission persistence
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            LocalMediaValidator.persistUriPermissionIfSupported(context.contentResolver, uri)
            val validation = LocalMediaValidator.validateVideoUri(context, uri.toString())
            if (validation is LocalMediaValidator.MediaValidationResult.Valid) {
                viewModel.setCustomVideoUri(uri.toString())
                videoPickError = null
            } else if (validation is LocalMediaValidator.MediaValidationResult.Invalid) {
                videoPickError = validation.reason
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("video_studio_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "AI Video Studio",
                    style = MaterialTheme.typography.titleLarge,
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Disabled Generation Notice Banner (Truthful, Budget GH₵0)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGray900)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .testTag("video_studio_disabled_banner")
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = StudioSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Video Generation Disabled • Free Dev Mode",
                            style = MaterialTheme.typography.labelLarge,
                            color = StudioSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Video prompts, visual styles, and aspect ratio settings are saved as genuine local drafts in your studio database (Budget GH₵0). No real AI video synthesis endpoints are called.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (formState.savedDraftMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioCardSurfaceVariant)
                        .border(1.dp, StudioSecondary, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StudioSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formState.savedDraftMessage ?: "",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (audioPickError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = audioPickError ?: "",
                    color = StudioGhanaRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (videoPickError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = videoPickError ?: "",
                    color = StudioGhanaRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Video Project Title
            Text(
                text = "Video Project Title *",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = formState.title,
                onValueChange = { viewModel.updateTitle(it) },
                placeholder = { Text("e.g. Accra Highlife Cyberpunk Visualizer", color = TextTertiary) },
                isError = formState.titleError != null,
                supportingText = formState.titleError?.let { error ->
                    { Text(text = error, color = StudioGhanaRed) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioPrimary,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = StudioCardSurface,
                    unfocusedContainerColor = StudioCardSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Visual Prompt
            Text(
                text = "Scene Description & Visual Prompt *",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = formState.prompt,
                onValueChange = { viewModel.updatePrompt(it) },
                placeholder = {
                    Text(
                        "Describe scene aesthetics, camera motion, lighting (e.g. slow zoom into vintage Ghanaian highlife dancehall, neon lighting, warm 1970s film grain)...",
                        color = TextTertiary
                    )
                },
                minLines = 3,
                maxLines = 6,
                isError = formState.promptError != null,
                supportingText = formState.promptError?.let { error ->
                    { Text(text = error, color = StudioGhanaRed) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioPrimary,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = StudioCardSurface,
                    unfocusedContainerColor = StudioCardSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_prompt_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Visual Style Selection
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = StudioGhanaGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Visual Style Preset",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                VideoProject.VISUAL_STYLES.forEach { style ->
                    val isSelected = formState.visualStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateVisualStyle(style) },
                        label = { Text(style) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioPrimary,
                            selectedLabelColor = TextPrimary,
                            containerColor = StudioCardSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("visual_style_$style")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Aspect Ratio Selection
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = null,
                    tint = StudioTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aspect Ratio",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VideoProject.ASPECT_RATIOS.forEach { (ratio, label) ->
                    val isSelected = formState.aspectRatio == ratio
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateAspectRatio(ratio) },
                        label = { Text(ratio) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSecondary,
                            selectedLabelColor = StudioBackground,
                            containerColor = StudioCardSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("aspect_ratio_$ratio")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Audio Track Link (Optional)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    tint = StudioSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Soundtrack Audio Link (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    if (formState.linkedSongTitle != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Linked Audio Project:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                                Text(
                                    text = formState.linkedSongTitle ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StudioSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.unlinkSong() }) {
                                Icon(Icons.Default.Close, contentDescription = "Unlink", tint = TextTertiary)
                            }
                        }
                    } else if (formState.localAudioUri != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Local Audio:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                                Text(
                                    text = formState.localAudioUri ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioTertiary,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { viewModel.setCustomAudioUri(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextTertiary)
                            }
                        }
                    } else {
                        Text(
                            text = "Sync visual transitions with a Ghanaian Highlife rhythm or local soundtrack.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box {
                            OutlinedButton(
                                onClick = { showSongPickerDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pick_linked_song_btn")
                            ) {
                                Text("Link Studio Song (${availableSongs.size})", fontSize = 12.sp)
                            }

                            DropdownMenu(
                                expanded = showSongPickerDropdown,
                                onDismissRequest = { showSongPickerDropdown = false }
                            ) {
                                if (availableSongs.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No audio projects found in studio") },
                                        onClick = { showSongPickerDropdown = false }
                                    )
                                } else {
                                    availableSongs.forEach { song ->
                                        DropdownMenuItem(
                                            text = { Text("${song.title} (${song.genre})") },
                                            onClick = {
                                                viewModel.linkSong(song)
                                                showSongPickerDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    audioPickerLauncher.launch(arrayOf("audio/*"))
                                } catch (e: Exception) {
                                    audioPickError = "Device picker error: ${e.message}"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("pick_custom_audio_file_btn")
                        ) {
                            Text("Pick Audio File", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5b. Local Video Reference (Optional)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = StudioSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Existing Local Video Reference (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    if (formState.localVideoUri != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Selected Local Video:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                                Text(
                                    text = formState.localVideoUri ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioSecondary,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { viewModel.setCustomVideoUri(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Video", tint = TextTertiary)
                            }
                        }
                    } else {
                        Text(
                            text = "Link an authentic local video file from your device to pair with this project draft. No synthetic videos are fabricated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            try {
                                videoPickerLauncher.launch(arrayOf("video/*"))
                            } catch (e: Exception) {
                                videoPickError = "Device picker error: ${e.message}"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("pick_custom_video_file_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Local Video File", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Action Button: Save Video Draft
            Button(
                onClick = { viewModel.saveVideoDraft() },
                enabled = formState.title.isNotBlank() && formState.prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioSecondary,
                    contentColor = StudioBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_video_draft_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Video Project Draft",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 7. Locally Saved Video Projects List
            Text(
                text = "Saved Video Projects (${videoProjects.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (videoProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioCardSurface)
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No saved video projects yet",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Draft your visual scenes above and tap \"Save Video Project Draft\".",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    videoProjects.forEach { project ->
                        VideoProjectCard(
                            project = project,
                            onDelete = { viewModel.deleteProject(project.id) },
                            onPreviewVideo = {
                                val validation = LocalMediaValidator.validateVideoUri(context, project.localVideoUri)
                                if (validation is LocalMediaValidator.MediaValidationResult.Valid) {
                                    previewingProject = project
                                    playbackErrorMessage = null
                                } else if (validation is LocalMediaValidator.MediaValidationResult.Invalid) {
                                    playbackErrorMessage = "Cannot play video: ${validation.reason}"
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Real media player dialog for local video playback
        if (previewingProject != null) {
            AlertDialog(
                onDismissRequest = { previewingProject = null },
                title = {
                    Text(
                        text = previewingProject?.title ?: "Local Video Playback",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        if (playbackErrorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkGray900)
                                    .border(1.dp, StudioGhanaRed, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = playbackErrorMessage ?: "",
                                    color = StudioGhanaRed,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        val controller = MediaController(ctx)
                                        controller.setAnchorView(this)
                                        setMediaController(controller)
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            start()
                                        }
                                        setOnErrorListener { _, what, extra ->
                                            playbackErrorMessage = "Video playback failed (what=$what, extra=$extra). Format or codec may be unsupported."
                                            true
                                        }
                                        try {
                                            setVideoURI(Uri.parse(previewingProject?.localVideoUri))
                                        } catch (e: Exception) {
                                            playbackErrorMessage = "Failed to load video URI: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Playing authentic local file: ${previewingProject?.localVideoUri}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { previewingProject = null }) {
                        Text("Close", color = StudioSecondary)
                    }
                },
                containerColor = StudioCardSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (playbackErrorMessage != null && previewingProject == null) {
            AlertDialog(
                onDismissRequest = { playbackErrorMessage = null },
                title = {
                    Text("Video Playback Error", color = StudioGhanaRed, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(playbackErrorMessage ?: "", color = TextPrimary)
                },
                confirmButton = {
                    TextButton(onClick = { playbackErrorMessage = null }) {
                        Text("OK", color = StudioSecondary)
                    }
                },
                containerColor = StudioCardSurface
            )
        }
    }
}

@Composable
private fun VideoProjectCard(
    project: VideoProject,
    onDelete: () -> Unit,
    onPreviewVideo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioCardSurface)
            .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("video_project_card_${project.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioCardSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = project.status.uppercase(),
                            color = StudioGhanaGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_video_project_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Video Project",
                        tint = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${project.visualStyle} • ${project.aspectRatio}",
                style = MaterialTheme.typography.bodySmall,
                color = StudioSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prompt: \"${project.prompt.take(60)}...\"",
                fontSize = 11.sp,
                color = TextSecondary
            )

            if (!project.linkedSongTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Soundtrack: ${project.linkedSongTitle}",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Truthful Preview Indicator (Task 3: Preview support only when a real local video file exists)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkGray900)
                    .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (project.isPlayableVideo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (project.isPlayableVideo) StudioSecondary else TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (project.isPlayableVideo) {
                            "Real local video file linked: ${project.localVideoUri}"
                        } else {
                            "Draft blueprint only • No video synthesized (AI generation disabled)"
                        },
                        fontSize = 11.sp,
                        color = if (project.isPlayableVideo) StudioSecondary else TextTertiary
                    )
                }
            }

            if (project.isPlayableVideo) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onPreviewVideo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioSecondary,
                        contentColor = StudioBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("preview_video_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Local Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
