package com.osei.aimusicstudio.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osei.aimusicstudio.ui.theme.StudioPrimary
import com.osei.aimusicstudio.ui.theme.StudioSecondary
import com.osei.aimusicstudio.ui.theme.StudioTertiary
import com.osei.aimusicstudio.ui.theme.TextPrimary
import com.osei.aimusicstudio.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OseiLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    showText: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2f * 0.8f

                // Outer neon sound ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            StudioPrimary,
                            StudioSecondary,
                            StudioTertiary,
                            StudioPrimary
                        )
                    ),
                    radius = radius,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Sound wave spikes around the circle
                val barCount = 16
                for (i in 0 until barCount) {
                    val angle = (i.toFloat() / barCount.toFloat()) * (2 * PI.toFloat())
                    val barHeight = (4.dp.toPx() + (sin(angle * 3 + pulse * 4) * 6.dp.toPx()).coerceAtLeast(0f))
                    val start = Offset(
                        center.x + (radius - 2.dp.toPx()) * cos(angle),
                        center.y + (radius - 2.dp.toPx()) * sin(angle)
                    )
                    val end = Offset(
                        center.x + (radius + barHeight) * cos(angle),
                        center.y + (radius + barHeight) * sin(angle)
                    )
                    drawLine(
                        color = if (i % 2 == 0) StudioSecondary else StudioPrimary,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Inner core 'O' emblem
                drawCircle(
                    color = Color(0xFF13132B),
                    radius = radius * 0.65f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(StudioSecondary.copy(alpha = 0.5f), Color.Transparent),
                        center = center,
                        radius = radius * 0.65f
                    ),
                    radius = radius * 0.65f
                )
            }

            // Bold stylised "O" center
            Text(
                text = "O",
                color = TextPrimary,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Black
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "OSEI",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Text(
                text = "AI MUSIC STUDIO",
                color = StudioSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
        }
    }
}
