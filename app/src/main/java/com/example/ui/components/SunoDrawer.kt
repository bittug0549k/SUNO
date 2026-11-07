package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val tabIndex: Int
)

@Composable
fun SunoDrawer(
    selectedTab: Int,
    dominantColor: Color,
    todayMinutes: Long,
    weekMinutes: Long,
    themeMode: String,
    onSelectItem: (Int) -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSettings: () -> Unit,
    onChangeThemeMode: (String) -> Unit
) {
    val items = listOf(
        DrawerItem("Home", Icons.Default.Home, 0),
        DrawerItem("Tracks", Icons.Default.MusicNote, 1),
        DrawerItem("Playlists", Icons.Default.PlaylistPlay, 2),
        DrawerItem("Albums", Icons.Default.Album, 3),
        DrawerItem("Artists", Icons.Default.Person, 4),
        DrawerItem("Folders", Icons.Default.Folder, 5),
        DrawerItem("Favorites", Icons.Default.Favorite, 6),
        DrawerItem("Genres", Icons.Default.Waves, 7)
    )

    ModalDrawerSheet(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = Color(0xFF0F111E),
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 18.dp, start = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_suno_logo),
                        contentDescription = "SUNO Logo",
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SUNO",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "BAS SUNTE RAHIYE.....",
                            color = dominantColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // Vertical Navigation Items
                items.forEach { item ->
                    val isSelected = selectedTab == item.tabIndex
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color.Black else dominantColor,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        selected = isSelected,
                        onClick = { onSelectItem(item.tabIndex) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = dominantColor,
                            unselectedContainerColor = Color.Transparent,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.White.copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Middle Section: Listening Stats Widget
                val todayHrs = todayMinutes / 60
                val todayMins = todayMinutes % 60
                val weekHrs = weekMinutes / 60
                val weekMins = weekMinutes % 60

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenAnalytics() }
                        .testTag("drawer_analytics_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171A2E))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Analytics",
                                    tint = dominantColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening Insights",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "View All →",
                                color = dominantColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Today: ${todayHrs}h ${todayMins}m  •  This Week: ${weekHrs}h ${weekMins}m",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Footer Section
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Theme Mode Toggles Row (System, Dark, Light)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onChangeThemeMode("SYSTEM") }) {
                        Icon(
                            imageVector = Icons.Default.BrightnessAuto,
                            contentDescription = "System Theme",
                            tint = if (themeMode == "SYSTEM") dominantColor else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onChangeThemeMode("DARK") }) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Dark Theme",
                            tint = if (themeMode == "DARK") dominantColor else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onChangeThemeMode("LIGHT") }) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Light Theme",
                            tint = if (themeMode == "LIGHT") dominantColor else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom-most Row: Sleep Timer & Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenSleepTimer() },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Sleep Timer",
                                tint = dominantColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Timer",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    val updateInfo by com.example.data.UpdateChecker.updateState.collectAsState()

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenSettings() },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Settings",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (updateInfo.hasUpdate) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 4.dp, end = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                listOf(Color(0xFFFF007F), Color(0xFFFF5252))
                                            )
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "NEW",
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
