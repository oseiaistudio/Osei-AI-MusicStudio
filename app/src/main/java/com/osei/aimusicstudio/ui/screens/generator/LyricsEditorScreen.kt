package com.osei.aimusicstudio.ui.screens.generator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.data.model.StudioConstants
import com.osei.aimusicstudio.ui.theme.StudioBackground
import com.osei.aimusicstudio.ui.theme.StudioBorder
import com.osei.aimusicstudio.ui.theme.StudioCardSurface
import com.osei.aimusicstudio.ui.theme.StudioGhanaRed
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary
import com.osei.aimusicstudio.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsEditorScreen(
    viewModel: SongGeneratorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    val scrollState = rememberScrollState()

    val structureTags = listOf("[Verse 1]", "[Chorus]", "[Verse 2]", "[Bridge]", "[Hook]", "[Outro]")
    val twiInspirations = listOf(
        "Ɔdɔ nnyɛ sika (Love isn't money)",
        "Yɛbɛkɔ so ara (We will keep moving forward)",
        "Nyame nhyira wo (God bless you)",
        "Akwaaba ma me (Welcome to me)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("lyrics_editor_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Custom Lyrics Editor",
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
            Text(
                text = "Structure Helpers",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to insert tags to structure your song sections",
                color = TextTertiary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(structureTags) { tag ->
                    SuggestionChip(
                        onClick = {
                            val newText = if (formState.lyrics.isBlank()) "$tag\n" else "${formState.lyrics}\n\n$tag\n"
                            viewModel.updateLyrics(newText)
                        },
                        label = { Text(tag, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = StudioCardSurface,
                            labelColor = StudioSecondary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = StudioBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cultural Twi inspirations
            Text(
                text = "Ghanaian Lyrical Inspiration (Twi & English)",
                style = MaterialTheme.typography.titleSmall,
                color = StudioTertiary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(twiInspirations) { phrase ->
                    SuggestionChip(
                        onClick = {
                            val cleanPhrase = phrase.substringBefore(" (")
                            val newText = if (formState.lyrics.isBlank()) "$cleanPhrase\n" else "${formState.lyrics}\n$cleanPhrase"
                            viewModel.updateLyrics(newText)
                        },
                        label = { Text(phrase, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = StudioCardSurface,
                            labelColor = TextSecondary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = StudioBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Multiline Editor Field
            OutlinedTextField(
                value = formState.lyrics,
                onValueChange = { viewModel.updateLyrics(it) },
                placeholder = {
                    Text(
                        "[Verse 1]\nEarly morning sun rising over Accra,\nHighlife horns playing from afar...\n\n[Chorus]\nOsei rhythm taking over the town,\nTurn the music up, don't let it down!",
                        color = TextTertiary
                    )
                },
                minLines = 12,
                maxLines = 20,
                isError = formState.lyricsError != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formState.lyricsError ?: "",
                            color = StudioGhanaRed,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${formState.lyrics.length}/${StudioConstants.MAX_LYRICS_LENGTH}",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioSecondary,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = StudioCardSurface,
                    unfocusedContainerColor = StudioCardSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lyrics_multiline_editor")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioPrimary,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_lyrics_done_btn")
            ) {
                Text(
                    text = "Save Lyrics & Return",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
