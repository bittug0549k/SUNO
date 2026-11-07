package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

val EASTER_EGG_PHRASES = listOf(
    "What are you doing? 😂",
    "No one is watching you, calm down! 👀",
    "Don't tap me, I'm ticklish! 💀",
    "Bhai gaana sun le, ungli mat kar! 🎧",
    "Error 404: Sleep not found for you.",
    "Tapping won't make the bass drop faster!",
    "I am a music player, not a bubble wrap!",
    "Are you trying to unlock secret FBI files?",
    "Dil se suno, screen tod ke nahi! ❤️",
    "Bas kar pagle, rulayega kya?",
    "DJ wale babu, mera gaana baja do!",
    "System hang ho jayega itna tap karega to!",
    "Keep calm and let the melody play.",
    "You found nothing, but congratulations on the effort!",
    "Warning: Over-tapping may cause extreme musical joy!"
)

@Composable
fun SunoTopBar(
    dominantColor: Color,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEasterEggTriggered: (String) -> Unit
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapIndex by remember { mutableIntStateOf(-1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("suno_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Hamburger Menu + Logo + Title & Subtitle ("sunte rahiye...")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("drawer_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Drawer",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Interactive Logo & Title Area with Easter Egg Tap Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        tapCount++
                        val nextIndex = (0 until EASTER_EGG_PHRASES.size).filter { it != lastTapIndex }.random()
                        lastTapIndex = nextIndex
                        onEasterEggTriggered(EASTER_EGG_PHRASES[nextIndex])
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("suno_title_easter_egg")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_suno_logo),
                    contentDescription = "SUNO Logo",
                    modifier = Modifier.size(34.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "SUNO",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "BAS SUNTE RAHIYE.....",
                        color = dominantColor.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Right Actions: Search & Settings with Update Ticket Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            val updateInfo by com.example.data.UpdateChecker.updateState.collectAsState()

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("topbar_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = dominantColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("topbar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (updateInfo.hasUpdate) {
                    val infiniteTransition = rememberInfiniteTransition(label = "TopBarBadge")
                    val badgeScale by infiniteTransition.animateFloat(
                        initialValue = 0.88f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(750, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "badgePulse"
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 2.dp)
                            .scale(badgeScale)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF007F), Color(0xFFFF5252))
                                )
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "NEW",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
