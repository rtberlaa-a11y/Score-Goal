package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveStatusBadge(
    status: String,
    currentMinute: Long,
    modifier: Modifier = Modifier
) {
    val isLive = status in listOf("live_1h", "halftime", "live_2h", "extra_time", "penalties")
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val badgeColor = when {
        status == "halftime" -> Color(0xFFF59E0B)
        isLive -> LiveRed
        status == "finished" -> Color(0xFF64748B)
        else -> PitchGreen
    }

    val badgeText = when (status) {
        "live_1h" -> "LIVE ${currentMinute}'"
        "halftime" -> "HT"
        "live_2h" -> "LIVE ${currentMinute}'"
        "extra_time" -> "ET ${currentMinute}'"
        "penalties" -> "PEN"
        "finished" -> "FT"
        "upcoming" -> "UPCOMING"
        "cancelled" -> "CANCELLED"
        else -> status.uppercase()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.18f))
            .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        if (isLive && status != "halftime") {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(alphaAnim)
                    .clip(CircleShape)
                    .background(LiveRed)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = badgeText,
            color = badgeColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
fun TeamJerseyCircle(
    teamName: String,
    colorHex: String,
    sizeDp: Int = 36,
    modifier: Modifier = Modifier
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        PitchGreen
    }
    val initials = teamName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "FC" }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = (sizeDp / 2.6).sp
            )
        )
    }
}

@Composable
fun StatBarRow(
    label: String,
    homeValue: Long,
    awayValue: Long,
    isPercentage: Boolean = false
) {
    val total = (homeValue + awayValue).coerceAtLeast(1L).toFloat()
    val homeRatio = (homeValue.toFloat() / total).coerceIn(0.05f, 0.95f)

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isPercentage) "$homeValue%" else homeValue.toString(),
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = label,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (isPercentage) "$awayValue%" else awayValue.toString(),
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .weight(homeRatio)
                    .height(6.dp)
                    .background(PitchGreen)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(1f - homeRatio)
                    .height(6.dp)
                    .background(Color(0xFF38BDF8))
            )
        }
    }
}
