package com.example.ui.screens

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.LyricsLine
import com.example.model.RepeatMode
import com.example.ui.MainViewModel
import kotlin.math.sin

@Composable
fun NowPlayingScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenTagEditor: () -> Unit
) {
    BackHandler { onDismiss() }

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()
    val secondaryColor by viewModel.secondaryColor.collectAsState()
    val lyrics by viewModel.currentLyrics.collectAsState()
    val sleepSeconds by viewModel.sleepTimerSeconds.collectAsState()

    var showLyricsView by remember { mutableStateOf(false) }

    // Horizontal swipe gesture for track changes
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dragOffset"
    )

    // Pulsating ambient depth lighting transition
    val infiniteTransition = rememberInfiniteTransition(label = "AmbientDepth")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "pulse"
    )
    val ambientRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 35000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A12))
            .testTag("now_playing_screen")
    ) {
        // --- LAYER 1: Dynamic Embedded Blur Engine ---
        // RenderEffect dynamic blur background with RenderEffect (API 31+) or soft gradient fallback
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = RenderEffect.createBlurEffect(
                            85f, 85f, Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                }
        ) {
            // Ambient colored light orbs that slowly rotate and pulse with the audio mood
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 3f

                // Top primary ambient orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.55f),
                            secondaryColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.width * 0.9f * ambientPulse
                    ),
                    center = Offset(cx, cy),
                    radius = size.width * 0.9f
                )

                // Bottom counter-accent ambient orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.40f),
                            Color.Transparent
                        ),
                        center = Offset(cx + 100f, size.height * 0.75f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(cx + 100f, size.height * 0.75f),
                    radius = size.width * 0.8f
                )
            }
        }

        // Dark gradient scrim for maximum text clarity & contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x99080914),
                            Color(0xBB080914),
                            Color(0xEE05060D)
                        )
                    )
                )
        )

        // --- LAYER 2: Main Content Layout ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_now_playing")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = currentSong?.album ?: "Offline Library",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { showLyricsView = !showLyricsView },
                    modifier = Modifier.testTag("toggle_lyrics")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Subtitles,
                        contentDescription = "Toggle Lyrics",
                        tint = if (showLyricsView) dominantColor else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center Content: Album Art OR Synchronized Lyrics View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showLyricsView) {
                    // Synchronized LRC Lyrics
                    LyricsView(
                        lyrics = lyrics,
                        positionMs = positionMs,
                        dominantColor = dominantColor,
                        onSeekTo = { viewModel.seekTo(it) }
                    )
                } else {
                    // Album Art with horizontal swipe gesture for next/previous track
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(280.dp)
                            .graphicsLayer {
                                translationX = animatedOffsetX
                                rotationZ = (animatedOffsetX / 30f).coerceIn(-12f, 12f)
                            }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = { dragOffsetX = 0f },
                                    onDragEnd = {
                                        if (dragOffsetX < -80f) {
                                            viewModel.playNext()
                                        } else if (dragOffsetX > 80f) {
                                            viewModel.playPrevious()
                                        }
                                        dragOffsetX = 0f
                                    },
                                    onDragCancel = {
                                        dragOffsetX = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX = (dragOffsetX + dragAmount).coerceIn(-220f, 220f)
                                    }
                                )
                            }
                            .scale(if (isPlaying) ambientPulse.coerceIn(0.96f, 1.04f) else 1f)
                    ) {
                        // Ambient radial glow behind vinyl/art
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            dominantColor.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Album artwork or fallback logo
                        if (currentSong?.albumArtUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentSong!!.albumArtUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Album Artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(260.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                            )
                        } else {
                            // High-end stylized vinyl record / SUNO artwork
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF16192E),
                                                Color(0xFF0F111F)
                                            )
                                        )
                                    )
                                    .border(1.dp, dominantColor.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_suno_logo),
                                    contentDescription = "SUNO Cover",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .rotate(if (isPlaying) ambientRotate else 0f)
                                )
                            }
                        }
                    }
                }
            }

            // Song Info: Title & Artist + Favorite button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong?.title ?: "Select a Track",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong?.artist ?: "SUNO Offline Player",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { currentSong?.let { viewModel.toggleFavorite(it) } },
                    modifier = Modifier.testTag("toggle_favorite")
                ) {
                    Icon(
                        imageVector = if (currentSong?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentSong?.isFavorite == true) Color(0xFFFF4081) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform Seekbar
            WaveformSeekbar(
                positionMs = positionMs,
                durationMs = durationMs,
                dominantColor = dominantColor,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.testTag("btn_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleEnabled) dominantColor else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { viewModel.playPrevious() },
                    modifier = Modifier.testTag("btn_previous")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Play / Pause (Hero Glowing Button)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(dominantColor, secondaryColor)
                            )
                        )
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple(bounded = true, radius = 34.dp),
                            onClick = { viewModel.togglePlayPause() }
                        )
                        .testTag("btn_play_pause_hero"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = { viewModel.playNext() },
                    modifier = Modifier.testTag("btn_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Repeat Mode
                IconButton(
                    onClick = { viewModel.cycleRepeatMode() },
                    modifier = Modifier.testTag("btn_repeat")
                ) {
                    val icon = when (repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    val tint = when (repeatMode) {
                        RepeatMode.OFF -> Color.White.copy(alpha = 0.5f)
                        else -> dominantColor
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Repeat",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Quick Action Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equalizer Shortcut
                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier.testTag("action_equalizer")
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = dominantColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Sleep Timer
                IconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier.testTag("action_sleep_timer")
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepSeconds > 0) dominantColor else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Tag Editor
                IconButton(
                    onClick = onOpenTagEditor,
                    modifier = Modifier.testTag("action_tag_editor")
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Tag Editor",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WaveformSeekbar(
    positionMs: Long,
    durationMs: Long,
    dominantColor: Color,
    onSeek: (Long) -> Unit
) {
    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val activeProgress = if (isDragging) dragProgress else progress

    Column(modifier = Modifier.fillMaxWidth()) {
        // Waveform Bars Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val newProg = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((newProg * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragProgress * durationMs).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = 48
                val barSpacing = 3.dp.toPx()
                val totalSpacing = barSpacing * (barCount - 1)
                val barWidth = (size.width - totalSpacing) / barCount
                val activeBarIndex = (activeProgress * barCount).toInt()

                for (i in 0 until barCount) {
                    val x = i * (barWidth + barSpacing)
                    // Synthesize organic musical frequency heights
                    val normalizedHeight = 0.25f + 0.65f * (
                        0.5f * (1f + sin(i * 0.45).toFloat()) *
                        0.5f * (1f + sin(i * 0.2 + 1.2).toFloat())
                    )
                    val barHeight = size.height * normalizedHeight
                    val y = (size.height - barHeight) / 2f

                    val color = if (i <= activeBarIndex) dominantColor else Color.White.copy(alpha = 0.22f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                // Draggable scrub head indicator
                val scrubHeadX = (activeProgress * size.width).coerceIn(0f, size.width)
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(scrubHeadX, size.height / 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currentFormatted = formatMs(if (isDragging) (dragProgress * durationMs).toLong() else positionMs)
            val totalFormatted = formatMs(durationMs)
            Text(
                text = currentFormatted,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Text(
                text = totalFormatted,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LyricsView(
    lyrics: List<LyricsLine>,
    positionMs: Long,
    dominantColor: Color,
    onSeekTo: (Long) -> Unit
) {
    if (lyrics.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No lyrics found for this track",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
        return
    }

    val listState = rememberLazyListState()

    // Find active lyric line index
    val activeIndex = lyrics.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)

    LaunchedEffect(activeIndex) {
        if (activeIndex in lyrics.indices) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isActive = index == activeIndex
            val scale = if (isActive) 1.05f else 1.0f
            val alpha = if (isActive) 1.0f else 0.4f
            val color = if (isActive) dominantColor else Color.White

            Text(
                text = line.text,
                color = color.copy(alpha = alpha),
                fontSize = if (isActive) 19.sp else 16.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable { onSeekTo(line.timeMs) }
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
