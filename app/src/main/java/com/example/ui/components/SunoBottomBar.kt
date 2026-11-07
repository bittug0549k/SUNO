package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class BottomBarItem(
    val title: String,
    val icon: ImageVector,
    val tabIndex: Int,
    val isHero: Boolean = false
)

@Composable
fun SunoBottomBar(
    currentTab: Int,
    dominantColor: Color,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomBarItem("Tracks", Icons.AutoMirrored.Filled.QueueMusic, 1),
        BottomBarItem("Albums", Icons.Default.Album, 3),
        BottomBarItem("Home", Icons.Default.Home, 0, isHero = true),
        BottomBarItem("Artists", Icons.Default.Person, 4),
        BottomBarItem("Favorites", Icons.Default.Favorite, 6)
    )

    var activeTooltipTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activeTooltipTitle) {
        if (activeTooltipTitle != null) {
            delay(1800)
            activeTooltipTitle = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating animated tooltip on long press
        AnimatedVisibility(
            visible = activeTooltipTitle != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200)),
            modifier = Modifier.offset(y = (-64).dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF222744))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = activeTooltipTitle ?: "",
                    color = dominantColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111425))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentTab == item.tabIndex

                    if (item.isHero) {
                        // Hero Center Button (Elevated)
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) dominantColor else Color.White.copy(alpha = 0.12f)
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onTabSelected(item.tabIndex) },
                                        onLongPress = { activeTooltipTitle = item.title }
                                    )
                                }
                                .testTag("bottom_tab_${item.title}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        // Standard Tab Items
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onTabSelected(item.tabIndex) },
                                        onLongPress = { activeTooltipTitle = item.title }
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("bottom_tab_${item.title}")
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) dominantColor else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )

                            Text(
                                text = item.title,
                                color = if (isSelected) dominantColor else Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
