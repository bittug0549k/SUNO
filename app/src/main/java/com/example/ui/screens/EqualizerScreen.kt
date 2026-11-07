package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun EqualizerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val presetName by viewModel.eqPresetName.collectAsState()
    val bands by viewModel.eqBands.collectAsState()
    val bassBoost by viewModel.bassBoost.collectAsState()
    val virtualizer by viewModel.virtualizer.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()

    val freqLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val presets = listOf(
        Triple("SUNO Dynamic", listOf(3, 2, 0, 1, 3), Pair(400, 300)),
        Triple("Flat", listOf(0, 0, 0, 0, 0), Pair(0, 0)),
        Triple("Bass Boost", listOf(6, 4, 1, 0, 0), Pair(800, 200)),
        Triple("Rock", listOf(4, 2, -1, 2, 4), Pair(500, 400)),
        Triple("Bollywood", listOf(3, 2, 1, 2, 4), Pair(450, 400)),
        Triple("Electronic", listOf(5, 3, 0, 2, 5), Pair(600, 500)),
        Triple("Classical", listOf(3, 1, 0, 2, 3), Pair(200, 250)),
        Triple("Vocal Booster", listOf(-2, 1, 4, 3, 1), Pair(150, 200))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A12))
            .padding(top = 40.dp)
            .verticalScroll(rememberScrollState())
            .testTag("equalizer_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("eq_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "EQUALIZER & FX",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = {
                    viewModel.applyEqPreset("Flat", listOf(0, 0, 0, 0, 0), 0, 0)
                },
                modifier = Modifier.testTag("eq_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Chips Row
        Text(
            text = "AUDIO PRESETS",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(presets) { preset ->
                val isSelected = presetName == preset.first
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) dominantColor else Color.White.copy(alpha = 0.08f)
                        )
                        .clickable {
                            viewModel.applyEqPreset(
                                preset.first,
                                preset.second,
                                preset.third.first,
                                preset.third.second
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = preset.first,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Frequency Band Sliders Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "5-BAND STUDIO GRAPHIC EQ",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                bands.forEachIndexed { index, level ->
                    val label = freqLabels.getOrElse(index) { "Band $index" }
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${if (level > 0) "+" else ""}$level dB",
                                color = dominantColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = level.toFloat(),
                            onValueChange = { newVal ->
                                viewModel.setBandLevel(index, newVal.toInt())
                            },
                            valueRange = -10f..10f,
                            steps = 19,
                            colors = SliderDefaults.colors(
                                thumbColor = dominantColor,
                                activeTrackColor = dominantColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bass Boost & Virtualizer 3D Audio Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bass Boost
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BASS BOOST",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${bassBoost / 10}%",
                        color = dominantColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Slider(
                        value = bassBoost.toFloat(),
                        onValueChange = { viewModel.setBassBoostLevel(it.toInt()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = dominantColor,
                            activeTrackColor = dominantColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            // Virtualizer
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131524))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "3D VIRTUALIZER",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${virtualizer / 10}%",
                        color = dominantColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Slider(
                        value = virtualizer.toFloat(),
                        onValueChange = { viewModel.setVirtualizerLevel(it.toInt()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = dominantColor,
                            activeTrackColor = dominantColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
