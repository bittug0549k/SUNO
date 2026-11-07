package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.theme.FontPreset
import com.example.theme.ThemePreset
import com.example.ui.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenFolderIndexer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenAnalytics: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val currentTheme by viewModel.themePreset.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val customWallpaperUri by viewModel.customWallpaperUri.collectAsState()
    val currentFont by viewModel.fontPreset.collectAsState()
    val customFontName by viewModel.customFontName.collectAsState()
    val crossfade by viewModel.crossfadeSeconds.collectAsState()
    val gapless by viewModel.gaplessPlayback.collectAsState()
    val replayGain by viewModel.replayGain.collectAsState()
    val filterShortAudio by viewModel.filterShortAudio.collectAsState()

    var showLicensesDialog by remember { mutableStateOf(false) }

    // File Picker for local .ttf / .otf fonts
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCustomFont(it) }
    }

    // Photo Picker for Custom App Wallpaper
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setCustomWallpaperUri(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A12))
            .padding(top = 40.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SETTINGS & PREFERENCES",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DEVELOPER ATTRIBUTION (CREATED BY DUMP KODER) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14172B))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Created by DUMP KODER",
                            color = currentTheme.primaryColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BAS SUNTE RAHIYE.....",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFD166).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Made in India 🇮🇳",
                            color = Color(0xFFFFD166),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: More Apps by DUMP KODER (TG ICON + CLICK HERE LINK WITH @DUMP_KODER)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191D33)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33229ED9))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF229ED9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Telegram",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "More Apps by DUMP KODER",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@DUMP_KODER",
                                    color = Color(0xFF229ED9),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Discover all exclusive apps, high-speed tools, and music player updates on our official channel.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val telegramIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://t.me/DUMP_KODER")
                                )
                                context.startActivity(telegramIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("telegram_dump_koder_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "CLICK HERE - @DUMP_KODER",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- SHORTCUTS TO SUB-MODULES (EQUALIZER, FOLDER INDEXER, ANALYTICS) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenFolderIndexer() }
                    .testTag("shortcut_folder_indexer"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Folders",
                        tint = Color(0xFF06D6A0),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Folder Indexer",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenEqualizer() }
                    .testTag("shortcut_equalizer"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = currentTheme.primaryColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Equalizer & FX",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenAnalytics() }
                    .testTag("shortcut_analytics"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analytics",
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Listening Stats",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION: THEME MODE (DARK / LIGHT / BY DEFAULT) ---
        Text(
            text = "THEME MODE (DARK / LIGHT / BY DEFAULT)",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val themeModes = listOf(
                    Triple("DARK", "Dark Mode", Icons.Default.DarkMode),
                    Triple("LIGHT", "Light Mode", Icons.Default.LightMode),
                    Triple("SYSTEM", "By Default", Icons.Default.SettingsBrightness)
                )

                themeModes.forEach { (modeKey, modeTitle, modeIcon) ->
                    val isSelected = currentThemeMode == modeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) currentTheme.primaryColor else Color.White.copy(alpha = 0.06f)
                            )
                            .clickable { viewModel.setThemeMode(modeKey) }
                            .padding(vertical = 12.dp)
                            .testTag("theme_mode_$modeKey"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = modeTitle,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = modeTitle,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION 2: APPEARANCE THEMES (3 PRESETS) ---
        Text(
            text = "APPEARANCE THEMES (3 PRESETS)",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemePreset.entries.forEach { preset ->
                val isSelected = currentTheme == preset
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { viewModel.setTheme(preset) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) preset.primaryColor else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(preset.primaryColor, preset.secondaryColor)
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = preset.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.subtitle,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(preset.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION: CUSTOM APP WALLPAPER ---
        Text(
            text = "CUSTOM APP WALLPAPER",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (customWallpaperUri.isNotBlank()) "Custom Wallpaper Active" else "Default Theme Backdrop",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (customWallpaperUri.isNotBlank()) "App background uses your personal photo" else "Pick an image from your device gallery",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    if (customWallpaperUri.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, currentTheme.primaryColor, RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(customWallpaperUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Wallpaper Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            wallpaperPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("pick_wallpaper_button")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (customWallpaperUri.isNotBlank()) "Change Wallpaper" else "Choose Wallpaper",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (customWallpaperUri.isNotBlank()) {
                        Button(
                            onClick = { viewModel.clearCustomWallpaper() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF0055)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("clear_wallpaper_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION 3: CUSTOM FONT ENGINE ---
        Text(
            text = "TYPOGRAPHY & CUSTOM FONTS",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                FontPreset.entries.forEach { font ->
                    val isSelected = currentFont == font
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setFontPreset(font) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = font.displayName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (font == FontPreset.CUSTOM && customFontName.isNotEmpty())
                                    "Loaded: $customFontName" else font.description,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = currentTheme.primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        fontPickerLauncher.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("import_font_button")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Import", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Local .TTF / .OTF Font", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION 4: AUDIO PLAYBACK ENGINE ---
        Text(
            text = "AUDIO ENGINE & PLAYBACK",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Gapless Playback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gapless Playback", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Continuous music flow without silent pauses", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = gapless,
                        onCheckedChange = { viewModel.setGapless(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = currentTheme.primaryColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Crossfade
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Crossfade Duration", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (crossfade == 0) "Off" else "$crossfade seconds", color = currentTheme.primaryColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = crossfade.toFloat(),
                        onValueChange = { viewModel.setCrossfade(it.toInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.primaryColor,
                            activeTrackColor = currentTheme.primaryColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ReplayGain Normalization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ReplayGain Auto-Volume", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Normalizes loudness variance across all tracks", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = replayGain,
                        onCheckedChange = { viewModel.setReplayGain(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = currentTheme.primaryColor
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- IN-APP UPDATE BADGE / CARD SYSTEM ---
        val updateInfo by com.example.data.UpdateChecker.updateState.collectAsState()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("update_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (updateInfo.hasUpdate) Color(0xFF1C1326) else Color(0xFF131524)
            ),
            border = if (updateInfo.hasUpdate) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF007F)) else null
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (updateInfo.hasUpdate) "Update Available" else "SUNO is Up to Date",
                            color = if (updateInfo.hasUpdate) Color(0xFFFF4081) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (updateInfo.hasUpdate) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF007F))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NEW",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Text(
                        text = if (updateInfo.hasUpdate) updateInfo.latestVersionName else "v1.0",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (updateInfo.hasUpdate) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = updateInfo.releaseNotes,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF007F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("btn_update_now")
                        ) {
                            Text("Update Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { com.example.data.UpdateChecker.dismissUpdate() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Later", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "You are on the latest high-performance build of SUNO Music Player.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { com.example.data.UpdateChecker.checkForUpdates() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentTheme.primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Check for Updates", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Open Source Licenses Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = { showLicensesDialog = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Source Licenses & Architecture", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("SUNO Open Source Notice", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "SUNO Music Player\n\"BAS SUNTE RAHIYE.....\"\n\n" +
                            "Created by DUMP KODER\n" +
                            "Official Channel: @DUMP_KODER\n" +
                            "100% Offline & Privacy-First\n\n" +
                            "Built with:\n" +
                            "• AndroidX Media3 & ExoPlayer\n" +
                            "• Jetpack Compose & Material 3\n" +
                            "• Room Database & SQLite\n" +
                            "• Kotlin Coroutines & StateFlow\n" +
                            "• AndroidX Palette & Coil\n\n" +
                            "Crafted with love in India 🇮🇳",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("OK", color = currentTheme.primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF14172B)
        )
    }
}
