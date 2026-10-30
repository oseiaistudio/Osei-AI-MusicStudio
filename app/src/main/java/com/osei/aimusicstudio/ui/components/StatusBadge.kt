package com.osei.aimusicstudio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.ui.theme.StudioGhanaGreen
import com.osei.aimusicstudio.ui.theme.StudioGhanaRed
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "completed" -> Triple(StudioGhanaGreen.copy(alpha = 0.15f), StudioGhanaGreen, "READY")
        "generating" -> Triple(StudioSecondary.copy(alpha = 0.15f), StudioSecondary, "GENERATING")
        "queued" -> Triple(StudioTertiary.copy(alpha = 0.15f), StudioTertiary, "QUEUED")
        "failed" -> Triple(StudioGhanaRed.copy(alpha = 0.15f), StudioGhanaRed, "FAILED")
        else -> Triple(StudioPrimary.copy(alpha = 0.15f), StudioPrimary, status.uppercase())
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun PhaseIndicatorBadge(
    modifier: Modifier = Modifier,
    label: String = "PHASE 1 FOUNDATION"
) {
    Box(
        modifier = modifier
            .background(StudioPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .border(1.dp, StudioSecondary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = StudioSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
