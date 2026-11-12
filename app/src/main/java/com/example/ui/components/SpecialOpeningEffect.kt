package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpecialOpeningEffect(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Ball bounce and scale animatables
    val ballScale = remember { Animatable(0.2f) }
    val ballOffsetY = remember { Animatable(-250f) }
    val shockwaveScale = remember { Animatable(0.1f) }
    val shockwaveAlpha = remember { Animatable(1f) }
    val contentAlpha = remember { Animatable(0f) }

    // Sweeping floodlights animation
    val infiniteTransition = rememberInfiniteTransition(label = "floodlights")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep"
    )

    val ballRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ballRotate"
    )

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        // Drop ball
        ballOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
        ballScale.animateTo(
            targetValue = 1.15f,
            animationSpec = tween(durationMillis = 200)
        )
        ballScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 150)
        )

        // Shockwave expansion
        contentAlpha.animateTo(1f, animationSpec = tween(300))
        shockwaveScale.animateTo(2.8f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        shockwaveAlpha.animateTo(0f, animationSpec = tween(600))

        // Auto finish after energetic duration
        delay(1200)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFinished() }
            .testTag("opening_kickoff_splash"),
        contentAlignment = Alignment.Center
    ) {
        // Stadium Turf & Floodlight Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Deep stadium radial gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F3818), // illuminated pitch green
                        Color(0xFF091F10),
                        Color(0xFF050807),
                        Color.Black
                    ),
                    center = center,
                    radius = size.width * 0.9f
                )
            )

            // Sweeping Floodlight Cones
            val lightColor1 = PitchGreen.copy(alpha = 0.12f)
            val lightColor2 = GoldAccent.copy(alpha = 0.08f)

            // Left floodlight beam
            val leftRad = Math.toRadians((sweepAngle - 20.0)).toFloat()
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, lightColor1, Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(center.x + cos(leftRad) * size.width, center.y + sin(leftRad) * size.height)
                ),
                start = Offset(0f, 0f),
                end = Offset(center.x + cos(leftRad) * size.width, center.y + sin(leftRad) * size.height),
                strokeWidth = 140f,
                cap = StrokeCap.Round
            )

            // Right floodlight beam
            val rightRad = Math.toRadians((-sweepAngle + 20.0)).toFloat()
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, lightColor2, Color.Transparent),
                    start = Offset(size.width, 0f),
                    end = Offset(center.x + cos(rightRad) * size.width, center.y + sin(rightRad) * size.height)
                ),
                start = Offset(size.width, 0f),
                end = Offset(center.x + cos(rightRad) * size.width, center.y + sin(rightRad) * size.height),
                strokeWidth = 140f,
                cap = StrokeCap.Round
            )

            // Center pitch circle rings
            drawCircle(
                color = PitchGreen.copy(alpha = 0.25f),
                radius = 110.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = PitchGreen.copy(alpha = 0.45f),
                radius = 5.dp.toPx(),
                center = center
            )

            // Animated expanding turf shockwave
            if (shockwaveAlpha.value > 0f) {
                drawCircle(
                    color = PitchGreen.copy(alpha = shockwaveAlpha.value * 0.6f),
                    radius = (100.dp.toPx() * shockwaveScale.value),
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = GoldAccent.copy(alpha = shockwaveAlpha.value * 0.4f),
                    radius = (80.dp.toPx() * shockwaveScale.value),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Center Content: Glowing Soccer Ball & Stadium Brand
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Animated Soccer Ball
            Box(
                modifier = Modifier
                    .offset(y = ballOffsetY.value.dp)
                    .scale(ballScale.value)
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFFEEEEEE), Color(0xFF9E9E9E), Color(0xFF212121))
                        )
                    )
                    .border(3.dp, PitchGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = "Score Goal Ball",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(ballRotation)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated App Name & Glow
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SCORE GOAL",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                }

                Text(
                    text = "LIVE FOOTBALL & SCORING ARENA",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = PitchGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Goal Ranks Teaser
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, PitchGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👑 Active Player ➔ Universe Legend (5000+ Goals)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GoldAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Fast Kickoff / Skip Button
                Button(
                    onClick = onFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PitchGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("skip_kickoff_button")
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KICKOFF NOW",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
