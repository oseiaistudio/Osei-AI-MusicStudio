package com.osei.aimusicstudio.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.core.UiState
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.model.StudioConstants
import com.osei.aimusicstudio.data.model.VideoProject
import com.osei.aimusicstudio.ui.components.SongCard
import com.osei.aimusicstudio.ui.screens.video.VideoStudioViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySongsScreen(
    libraryViewModel: LibraryViewModel,
    videoViewModel: VideoStudioViewModel? = null,
    onNavigateBack: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onNavigateToVideoStudio: () -> Unit = {},
    onNavigateToSongDetails: (String) -> Unit,
    onPlaySong: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val libraryUiState by libraryViewModel.libraryUiState.collectAsState()
    val filterState by libraryViewModel.filterState.collectAsState()
    val videoProjects = videoViewModel?.videoProjects?.collectAsState()?.value ?: emptyList()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("my_songs_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "My Studio Projects",
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
            actions = {
                IconButton(
                    onClick = if (selectedTabIndex == 0) onNavigateToGenerator else onNavigateToVideoStudio,
                    modifier = Modifier.testTag("library_new_song_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (selectedTabIndex == 0) "New Song" else "New Video",
                        tint = StudioSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
        )

        // Studio Project Tabs: Audio Tracks vs Music Videos
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = StudioBackground,
            contentColor = StudioPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = StudioPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audio Tracks", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = StudioPrimary,
                unselectedContentColor = TextTertiary,
                modifier = Modifier.testTag("tab_audio_tracks")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Music Videos (${videoProjects.size})", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = StudioSecondary,
                unselectedContentColor = TextTertiary,
                modifier = Modifier.testTag("tab_music_videos")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTabIndex == 0) {
            // AUDIO TRACKS VIEW
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = { libraryViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search songs by title or lyrics...", color = TextTertiary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (filterState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { libraryViewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
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
                        .testTag("library_search_input")
                )
            }

            // Genre & Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = filterState.selectedGenre == "All" && !filterState.favoritesOnly,
                        onClick = { libraryViewModel.setGenreFilter("All") },
                        label = { Text("All Songs") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioPrimary,
                            selectedLabelColor = TextPrimary,
                            containerColor = StudioCardSurface,
                            labelColor = TextSecondary
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = filterState.favoritesOnly,
                        onClick = { libraryViewModel.toggleFavoritesOnly() },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (filterState.favoritesOnly) StudioGhanaRed else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Favorites")
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioCardSurfaceVariant,
                            selectedLabelColor = StudioGhanaRed,
                            containerColor = StudioCardSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("library_filter_favorites")
                    )
                }

                items(StudioConstants.GENRES) { genre ->
                    val isSelected = filterState.selectedGenre == genre
                    FilterChip(
                        selected = isSelected,
                        onClick = { libraryViewModel.setGenreFilter(if (isSelected) "All" else genre) },
                        label = { Text(genre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSecondary,
                            selectedLabelColor = StudioBackground,
                            containerColor = StudioCardSurface,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Audio Content
            when (val state = libraryUiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = StudioPrimary)
                    }
                }
                is UiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = StudioSecondary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (filterState.searchQuery.isNotBlank()) "No matching songs found" else "No songs in studio library yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (filterState.searchQuery.isNotBlank()) "Try searching for a different keyword or genre filter." else "Draft your first Ghanaian or international AI audio track now.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onNavigateToGenerator,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioPrimary,
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_library_create_btn")
                            ) {
                                Text("Open Audio Studio")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.data, key = { it.id }) { song ->
                            SongCard(
                                song = song,
                                onClick = { onNavigateToSongDetails(song.id) },
                                onPlayClick = { onPlaySong(song) },
                                onFavoriteToggle = { libraryViewModel.toggleFavorite(song) }
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading songs: ${state.message}",
                            color = StudioGhanaRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // MUSIC VIDEOS VIEW
            if (videoProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = StudioSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No video projects created yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Draft your visual scenes, select aspect ratios, and choose Afrofuturism 3D presets in the Video Studio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToVideoStudio,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioSecondary,
                                contentColor = StudioBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_video_projects_btn")
                        ) {
                            Text("Open Video Studio")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(videoProjects, key = { it.id }) { project ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(StudioCardSurface)
                                .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                                .testTag("library_video_card_${project.id}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = project.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${project.visualStyle} • ${project.aspectRatio}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StudioSecondary
                                        )
                                    }

                                    IconButton(
                                        onClick = { videoViewModel?.deleteProject(project.id) },
                                        modifier = Modifier.testTag("delete_library_video_${project.id}")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextTertiary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Prompt: \"${project.prompt.take(80)}...\"",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                if (!project.linkedSongTitle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Soundtrack: ${project.linkedSongTitle}",
                                        fontSize = 11.sp,
                                        color = TextTertiary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

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
                                                "Playable video: ${project.localVideoUri}"
                                            } else {
                                                "Draft blueprint only • No video synthesized (AI generation disabled)"
                                            },
                                            fontSize = 11.sp,
                                            color = if (project.isPlayableVideo) StudioSecondary else TextTertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
