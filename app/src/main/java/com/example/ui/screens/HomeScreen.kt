package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.model.Song
import com.example.ui.MainViewModel
import com.example.ui.components.SunoTopBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTagEditor: (Song) -> Unit,
    onOpenCreatePlaylist: () -> Unit
) {
    val songs by viewModel.filteredSongs.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val mixedSongs by viewModel.mixedSongs.collectAsState()
    val recentSongs by viewModel.recentListenSongs.collectAsState()
    val topSongs by viewModel.topListenSongs.collectAsState()

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val customWallpaperUri by viewModel.customWallpaperUri.collectAsState()

    val tabs = listOf("For You", "Tracks", "Playlists", "Albums", "Artists", "Folders", "Favorites", "Genres")

    var isSearchVisible by remember { mutableStateOf(false) }

    // Easter egg snackbar message state
    var easterEggMessage by remember { mutableStateOf<String?>(null) }
    var easterEggVisible by remember { mutableStateOf(false) }

    LaunchedEffect(easterEggMessage) {
        if (easterEggMessage != null) {
            easterEggVisible = true
            delay(2800)
            easterEggVisible = false
            delay(200)
            easterEggMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .testTag("home_screen")
    ) {
        // Optional Custom Wallpaper background
        if (customWallpaperUri.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(customWallpaperUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Custom Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.28f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 36.dp)
        ) {
            // Top App Bar with Easter Egg System
            SunoTopBar(
                dominantColor = dominantColor,
                onMenuClick = onOpenDrawer,
                onSearchClick = {
                    isSearchVisible = !isSearchVisible
                },
                onSettingsClick = onOpenSettings,
                onEasterEggTriggered = { phrase ->
                    easterEggMessage = phrase
                }
            )

            // Search Bar (Only shown when user taps the search button or search query exists)
            AnimatedVisibility(
                visible = isSearchVisible || searchQuery.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = {
                        Text(
                            text = "Search title, artist, album, genre...",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = dominantColor,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(onClick = { isSearchVisible = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Search",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedBorderColor = dominantColor,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(50.dp)
                        .testTag("search_text_field")
                )
            }

            // Category Tab Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tabs) { index, tabTitle ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) dominantColor else Color.White.copy(alpha = 0.07f)
                            )
                            .clickable { viewModel.activeTab.value = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("tab_$tabTitle")
                    ) {
                        Text(
                            text = tabTitle,
                            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> { // For You (Top Carousel Mixed + Recent Listen + Top Listen)
                        HomeForYouView(
                            mixedSongs = mixedSongs,
                            recentSongs = recentSongs,
                            topSongs = topSongs,
                            dominantColor = dominantColor,
                            onRefreshMixed = { viewModel.refreshMixedSongs() },
                            onPlaySong = { song, list -> viewModel.playSong(song, list) },
                            onShuffleList = { list ->
                                if (list.isNotEmpty()) {
                                    val shuffled = list.shuffled()
                                    viewModel.enableShuffle(true)
                                    viewModel.playSong(shuffled.first(), shuffled)
                                }
                            }
                        )
                    }
                    1 -> { // Tracks (All Scanned Songs)
                        SongListTab(
                            songs = songs,
                            currentSong = currentSong,
                            dominantColor = dominantColor,
                            onSongClick = { song -> viewModel.playSong(song, songs) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onEditTags = onOpenTagEditor
                        )
                    }
                    2 -> { // Playlists
                        PlaylistTab(
                            playlists = playlists,
                            dominantColor = dominantColor,
                            onCreatePlaylist = onOpenCreatePlaylist,
                            onDeletePlaylist = { viewModel.deletePlaylist(it) }
                        )
                    }
                    3 -> { // Albums
                        GroupedListTab(
                            items = songs.groupBy { it.album }.map { Pair(it.key, it.value) },
                            icon = Icons.Default.Album,
                            dominantColor = dominantColor,
                            onPlayGroup = { groupSongs ->
                                if (groupSongs.isNotEmpty()) viewModel.playSong(groupSongs.first(), groupSongs)
                            }
                        )
                    }
                    4 -> { // Artists
                        GroupedListTab(
                            items = songs.groupBy { it.artist }.map { Pair(it.key, it.value) },
                            icon = Icons.Default.Person,
                            dominantColor = dominantColor,
                            onPlayGroup = { groupSongs ->
                                if (groupSongs.isNotEmpty()) viewModel.playSong(groupSongs.first(), groupSongs)
                            }
                        )
                    }
                    5 -> { // Folders
                        GroupedListTab(
                            items = songs.groupBy { it.folder }.map { Pair(it.key, it.value) },
                            icon = Icons.Default.Folder,
                            dominantColor = dominantColor,
                            onPlayGroup = { groupSongs ->
                                if (groupSongs.isNotEmpty()) viewModel.playSong(groupSongs.first(), groupSongs)
                            }
                        )
                    }
                    6 -> { // Favorites
                        val favSongs = songs.filter { it.isFavorite }
                        if (favSongs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No favorite tracks yet.\nTap the heart icon on any song!",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            SongListTab(
                                songs = favSongs,
                                currentSong = currentSong,
                                dominantColor = dominantColor,
                                onSongClick = { song -> viewModel.playSong(song, favSongs) },
                                onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                                onEditTags = onOpenTagEditor
                            )
                        }
                    }
                    7 -> { // Genres
                        GroupedListTab(
                            items = songs.groupBy { it.genre }.map { Pair(it.key, it.value) },
                            icon = Icons.Default.Waves,
                            dominantColor = dominantColor,
                            onPlayGroup = { groupSongs ->
                                if (groupSongs.isNotEmpty()) viewModel.playSong(groupSongs.first(), groupSongs)
                            }
                        )
                    }
                }
            }

            // Bottom Spacing for Mini Player
            if (currentSong != null) {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // --- Pinned Mini Player Bar ---
        if (currentSong != null) {
            MiniPlayerBar(
                song = currentSong!!,
                isPlaying = isPlaying,
                positionMs = positionMs,
                durationMs = durationMs,
                dominantColor = dominantColor,
                onBarClick = onOpenNowPlaying,
                onTogglePlay = { viewModel.togglePlayPause() },
                onNext = { viewModel.playNext() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 76.dp)
            )
        }

        // --- Floating Easter Egg Snackbar Overlay ---
        AnimatedVisibility(
            visible = easterEggVisible && easterEggMessage != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
                .padding(horizontal = 24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E223D)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.testTag("easter_egg_toast")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Fun",
                        tint = dominantColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = easterEggMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun HomeForYouView(
    mixedSongs: List<Song>,
    recentSongs: List<Song>,
    topSongs: List<Song>,
    dominantColor: Color,
    onRefreshMixed: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onShuffleList: (List<Song>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section 1: Mixed Dynamic Carousel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mixed Playlist",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onRefreshMixed,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Mixed",
                                tint = dominantColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onShuffleList(mixedSongs) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Quick Shuffle",
                            tint = dominantColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(mixedSongs) { song ->
                        SongCarouselCard(
                            song = song,
                            dominantColor = dominantColor,
                            onClick = { onPlaySong(song, mixedSongs) }
                        )
                    }
                }
            }
        }

        // Section 2: Recent Listen
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Listen",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { onShuffleList(recentSongs) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Recent",
                            tint = dominantColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentSongs) { song ->
                        SongCarouselCard(
                            song = song,
                            dominantColor = dominantColor,
                            onClick = { onPlaySong(song, recentSongs) }
                        )
                    }
                }
            }
        }

        // Section 3: Top Listen
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Listen",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { onShuffleList(topSongs) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle Top",
                            tint = dominantColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(topSongs) { song ->
                        SongCarouselCard(
                            song = song,
                            dominantColor = dominantColor,
                            onClick = { onPlaySong(song, topSongs) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongCarouselCard(
    song: Song,
    dominantColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("carousel_card_${song.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Album Art Thumbnail
            Box(
                modifier = Modifier
                    .size(115.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1C2038)),
                contentAlignment = Alignment.Center
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = dominantColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play badge on corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(dominantColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SongListTab(
    songs: List<Song>,
    currentSong: Song?,
    dominantColor: Color,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onEditTags: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No songs found in offline storage.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(songs, key = { it.id }) { song ->
            val isCurrent = currentSong?.id == song.id
            var showMenu by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isCurrent) dominantColor.copy(alpha = 0.15f) else Color(0xFF131524)
                    )
                    .clickable { onSongClick(song) }
                    .padding(10.dp)
                    .testTag("song_row_${song.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art or Vector Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1D2138)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.albumArtUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(song.albumArtUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = if (isCurrent) dominantColor else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info: Title, Artist & Duration
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = if (isCurrent) dominantColor else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.artist} • ${song.formattedDuration}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Button
                IconButton(onClick = { onToggleFavorite(song) }) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) Color(0xFFFF4081) else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Options Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit ID3 Tags") },
                            onClick = {
                                showMenu = false
                                onEditTags(song)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistTab(
    playlists: List<com.example.data.local.entity.PlaylistEntity>,
    dominantColor: Color,
    onCreatePlaylist: () -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onCreatePlaylist() }
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(dominantColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Playlist",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Create New Playlist",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Curate your custom offline collections",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom playlists created yet.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF131524))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlaylistPlay,
                                contentDescription = "Playlist",
                                tint = dominantColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = playlist.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Delete",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupedListTab(
    items: List<Pair<String, List<Song>>>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    dominantColor: Color,
    onPlayGroup: (List<Song>) -> Unit
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No grouped items available.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { (groupName, groupSongs) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131524))
                    .clickable { onPlayGroup(groupSongs) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1D2138)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = groupName,
                            tint = dominantColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = groupName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${groupSongs.size} tracks",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play All",
                    tint = dominantColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    dominantColor: Color,
    onBarClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "nano_equalizer")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "w3"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onBarClick() }
            .testTag("mini_player_bar"),
        colors = CardDefaults.cardColors(containerColor = Color(0xF0121526)),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                listOf(dominantColor.copy(alpha = 0.6f), Color(0x33FFFFFF), dominantColor.copy(alpha = 0.3f))
            )
        )
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = dominantColor,
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Album Art thumbnail with rounded styling
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1D2138))
                        .border(1.dp, dominantColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.albumArtUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(song.albumArtUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Art",
                            tint = dominantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Track Info & Live Equalizer Wave
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (isPlaying) {
                            Spacer(modifier = Modifier.width(6.dp))
                            // Mini Live Equalizer Wave
                            Row(
                                modifier = Modifier.height(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height((12 * wave1).dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(dominantColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height((12 * wave2).dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(dominantColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height((12 * wave3).dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(dominantColor)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = song.artist,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play / Pause Elevated Circular Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(dominantColor)
                        .clickable { onTogglePlay() }
                        .testTag("mini_btn_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Next Track Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("mini_btn_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
