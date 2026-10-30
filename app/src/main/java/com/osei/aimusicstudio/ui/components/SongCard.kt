package com.osei.aimusicstudio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.ui.theme.StudioBorder
import com.osei.aimusicstudio.ui.theme.StudioCardSurface
import com.osei.aimusicstudio.ui.theme.StudioCardSurfaceVariant
import com.osei.aimusicstudio.ui.theme.StudioGhanaRed
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary
import com.osei.aimusicstudio.ui.theme.TextTertiary

@Composable
fun SongCard(
    song: Song,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(StudioCardSurface, StudioCardSurfaceVariant)
                )
            )
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("song_card_${song.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val isPlayable = song.status == "completed" && song.audioUrl.isNotBlank()

            // Album art square with play action (or draft badge if audio not synthesized)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isPlayable) {
                            Brush.sweepGradient(listOf(StudioPrimary, StudioSecondary, StudioPrimary))
                        } else {
                            Brush.linearGradient(listOf(StudioCardSurfaceVariant, StudioBorder))
                        }
                    )
                    .clickable(onClick = if (isPlayable) onPlayClick else onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlayable) Icons.Default.PlayArrow else Icons.Default.MusicNote,
                    contentDescription = if (isPlayable) "Play song" else "Draft song (audio generation disabled)",
                    tint = if (isPlayable) TextPrimary else StudioSecondary,
                    modifier = Modifier.size(if (isPlayable) 28.dp else 24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & genre
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title.ifBlank { "Untitled Creation" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = song.genre,
                        color = StudioSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = song.mood,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(status = song.status)
                    if (song.isInstrumental) {
                        Text(
                            text = "Instrumental",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = song.language,
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Favorite toggle button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("favorite_btn_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "Unfavorite" else "Favorite",
                    tint = if (song.isFavorite) StudioGhanaRed else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
