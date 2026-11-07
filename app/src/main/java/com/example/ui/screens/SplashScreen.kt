package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashWaveTransition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650)
        )
        delay(1200) // Brief moment to enjoy the animated splash wave
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350)
        )
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF06070E),
                        Color(0xFF0C0E1A),
                        Color(0xFF06070E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient backdrop radial pulse
        Canvas(modifier = Modifier.fillMaxSize().alpha(alpha.value)) {
            val cx = size.width / 2f
            val cy = size.height / 2f - 40f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3300F0FF),
                        Color(0x18FF007F),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = (size.width * 0.7f) * pulseGlow
                ),
                center = Offset(cx, cy),
                radius = size.width * 0.7f * pulseGlow
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Logo container with illuminated circular ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF16192E), Color(0xFF0F111F))
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_suno_logo),
                    contentDescription = "SUNO Logo",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SUNO",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "BAS SUNTE RAHIYE.....",
                color = Color(0xFF00F0FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Equalizer Frequency Visualizer Bars
            Canvas(
                modifier = Modifier
                    .width(180.dp)
                    .height(36.dp)
            ) {
                val barCount = 18
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (barCount - 1)
                val barWidth = (size.width - totalSpacing) / barCount

                for (i in 0 until barCount) {
                    val x = i * (barWidth + barSpacing)
                    val harmonic1 = sin(wavePhase + i * 0.45).toFloat()
                    val harmonic2 = sin(wavePhase * 1.5f + i * 0.25).toFloat()
                    val normalized = (0.25f + 0.65f * ((harmonic1 + harmonic2 + 2f) / 4f)).coerceIn(0.15f, 1f)
                    val barHeight = size.height * normalized
                    val y = (size.height - barHeight) / 2f

                    val barColor = if (i % 2 == 0) Color(0xFF00F0FF) else Color(0xFFFF007F)
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
        }

        // Footer Attribution
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(alpha.value)
        ) {
            Text(
                text = stringResource(id = R.string.created_by),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Proudly Made in India 🇮🇳 • 100% Offline",
                color = Color(0xFFFFD166),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
