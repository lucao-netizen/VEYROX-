package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.VeyroxBrightBlue
import com.example.ui.theme.VeyroxPrimaryBlue
import com.example.ui.theme.VeyroxTheme

@Composable
fun VeyroxGeometricLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showGlow: Boolean = true
) {
    val primaryBlue = VeyroxPrimaryBlue
    val brightBlue = VeyroxBrightBlue

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        if (showGlow) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryBlue.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.52f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.5f, h * 0.52f)
            )
        }

        // Left outer blade
        val leftBlade = Path().apply {
            moveTo(w * 0.12f, h * 0.18f)
            lineTo(w * 0.30f, h * 0.18f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.45f, h * 0.86f)
            close()
        }
        drawPath(
            path = leftBlade,
            brush = Brush.linearGradient(
                colors = listOf(primaryBlue, Color(0xFF1F4EC4)),
                start = Offset(w * 0.12f, h * 0.18f),
                end = Offset(w * 0.50f, h * 0.86f)
            )
        )

        // Right outer blade
        val rightBlade = Path().apply {
            moveTo(w * 0.88f, h * 0.18f)
            lineTo(w * 0.70f, h * 0.18f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.55f, h * 0.86f)
            close()
        }
        drawPath(
            path = rightBlade,
            brush = Brush.linearGradient(
                colors = listOf(brightBlue, primaryBlue),
                start = Offset(w * 0.88f, h * 0.18f),
                end = Offset(w * 0.50f, h * 0.86f)
            )
        )

        // Inner precision V core highlight
        val innerCore = Path().apply {
            moveTo(w * 0.35f, h * 0.32f)
            lineTo(w * 0.50f, h * 0.56f)
            lineTo(w * 0.65f, h * 0.32f)
            lineTo(w * 0.59f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.65f)
            lineTo(w * 0.41f, h * 0.46f)
            close()
        }
        drawPath(
            path = innerCore,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF8EC5FF), brightBlue),
                startY = h * 0.30f,
                endY = h * 0.66f
            )
        )
    }
}

@Composable
fun VeyroxBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp
) {
    val colors = VeyroxTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VeyroxGeometricLogo(size = logoSize)
        Text(
            text = "VEYROX",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 2.4.sp,
            color = colors.textPrimary
        )
    }
}
