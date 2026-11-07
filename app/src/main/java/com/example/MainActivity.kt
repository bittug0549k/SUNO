package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.model.Song
import com.example.service.SunoPlaybackService
import com.example.theme.FontEngine
import com.example.ui.MainViewModel
import com.example.ui.components.SunoBottomBar
import com.example.ui.components.SunoDrawer
import com.example.ui.dialogs.CreatePlaylistDialog
import com.example.ui.dialogs.SleepTimerDialog
import com.example.ui.dialogs.TagEditorDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.FolderIndexerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.SunoTheme
import kotlinx.coroutines.launch

enum class ScreenState {
    SPLASH, HOME, EQUALIZER, SETTINGS, ANALYTICS, FOLDER_INDEXER
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val serviceIntent = Intent(this, SunoPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            val themePreset by viewModel.themePreset.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val fontPreset by viewModel.fontPreset.collectAsState()
            val customFontPath by viewModel.customFontPath.collectAsState()
            val dominantColor by viewModel.dominantColor.collectAsState()
            val sleepSeconds by viewModel.sleepTimerSeconds.collectAsState()
            val activeTab by viewModel.activeTab.collectAsState()

            val todayMinutes by viewModel.todayMinutes.collectAsState()
            val weekMinutes by viewModel.weekMinutes.collectAsState()

            val customWallpaperUri by viewModel.customWallpaperUri.collectAsState()

            val activeFontFamily = remember(fontPreset, customFontPath) {
                FontEngine.getFontFamily(fontPreset, customFontPath)
            }

            // Permissions
            val permissionsToRequest = buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                    add(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Scans reactive flow */ }

            LaunchedEffect(Unit) {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            }

            SunoTheme(
                preset = themePreset,
                themeMode = themeMode,
                fontFamily = activeFontFamily
            ) {
                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
                var showNowPlayingSheet by remember { mutableStateOf(false) }
                var editingSong by remember { mutableStateOf<Song?>(null) }
                var showSleepTimer by remember { mutableStateOf(false) }
                var showCreatePlaylist by remember { mutableStateOf(false) }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Intercept back button when drawer is open
                BackHandler(enabled = drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentScreen == ScreenState.HOME) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                SunoDrawer(
                                    selectedTab = activeTab,
                                    dominantColor = dominantColor,
                                    todayMinutes = todayMinutes,
                                    weekMinutes = weekMinutes,
                                    themeMode = themeMode,
                                    onSelectItem = { tabIndex ->
                                        viewModel.activeTab.value = tabIndex
                                        scope.launch { drawerState.close() }
                                    },
                                    onOpenAnalytics = {
                                        scope.launch { drawerState.close() }
                                        currentScreen = ScreenState.ANALYTICS
                                    },
                                    onOpenSleepTimer = {
                                        scope.launch { drawerState.close() }
                                        showSleepTimer = true
                                    },
                                    onOpenSettings = {
                                        scope.launch { drawerState.close() }
                                        currentScreen = ScreenState.SETTINGS
                                    },
                                    onChangeThemeMode = { mode ->
                                        viewModel.setThemeMode(mode)
                                    }
                                )
                            }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpenDrawer = {
                                        scope.launch { drawerState.open() }
                                    },
                                    onOpenNowPlaying = { showNowPlayingSheet = true },
                                    onOpenEqualizer = { currentScreen = ScreenState.EQUALIZER },
                                    onOpenSettings = { currentScreen = ScreenState.SETTINGS },
                                    onOpenTagEditor = { song -> editingSong = song },
                                    onOpenCreatePlaylist = { showCreatePlaylist = true }
                                )

                                // Dual Navigation Bottom Bar
                                SunoBottomBar(
                                    currentTab = activeTab,
                                    dominantColor = dominantColor,
                                    onTabSelected = { tab -> viewModel.activeTab.value = tab },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    } else {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "sub_screen_transition"
                        ) { screen ->
                            when (screen) {
                                ScreenState.SPLASH -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            currentScreen = ScreenState.HOME
                                        }
                                    )
                                }
                                ScreenState.EQUALIZER -> {
                                    EqualizerScreen(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = ScreenState.HOME }
                                    )
                                }
                                ScreenState.SETTINGS -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = ScreenState.HOME },
                                        onOpenFolderIndexer = { currentScreen = ScreenState.FOLDER_INDEXER },
                                        onOpenEqualizer = { currentScreen = ScreenState.EQUALIZER },
                                        onOpenAnalytics = { currentScreen = ScreenState.ANALYTICS }
                                    )
                                }
                                ScreenState.ANALYTICS -> {
                                    AnalyticsScreen(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = ScreenState.HOME }
                                    )
                                }
                                ScreenState.FOLDER_INDEXER -> {
                                    FolderIndexerScreen(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = ScreenState.SETTINGS }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }

                    // Full-Screen Embedded Blur Now Playing Screen
                    if (showNowPlayingSheet) {
                        NowPlayingScreen(
                            viewModel = viewModel,
                            onDismiss = { showNowPlayingSheet = false },
                            onOpenEqualizer = {
                                showNowPlayingSheet = false
                                currentScreen = ScreenState.EQUALIZER
                            },
                            onOpenSleepTimer = { showSleepTimer = true },
                            onOpenTagEditor = {
                                viewModel.currentSong.value?.let { editingSong = it }
                            }
                        )
                    }

                    // Dialogs
                    editingSong?.let { song ->
                        TagEditorDialog(
                            song = song,
                            dominantColor = dominantColor,
                            onDismiss = { editingSong = null },
                            onSave = { title, artist, album, year ->
                                viewModel.updateSongTags(song.id, title, artist, album, year)
                            }
                        )
                    }

                    if (showSleepTimer) {
                        SleepTimerDialog(
                            activeSeconds = sleepSeconds,
                            dominantColor = dominantColor,
                            onDismiss = { showSleepTimer = false },
                            onSetTimer = { min, atEnd -> viewModel.setSleepTimer(min, atEnd) },
                            onCancelTimer = { viewModel.cancelSleepTimer() }
                        )
                    }

                    if (showCreatePlaylist) {
                        CreatePlaylistDialog(
                            dominantColor = dominantColor,
                            onDismiss = { showCreatePlaylist = false },
                            onCreate = { name -> viewModel.createPlaylist(name) }
                        )
                    }
                }
            }
        }
    }
}
