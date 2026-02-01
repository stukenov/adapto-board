package com.playoutedge.player.status

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playoutedge.player.config.PlayerConfig
import com.playoutedge.player.network.NetworkMonitor
import com.playoutedge.player.playback.PlaybackState
import com.playoutedge.player.playlist.PlaylistManifest
import kotlinx.coroutines.delay

/**
 * Status screen for diagnostics.
 */
@Composable
fun StatusScreen(
    deviceId: String?,
    config: PlayerConfig?,
    networkState: NetworkMonitor.NetworkState,
    playbackState: PlaybackState,
    playlist: PlaylistManifest?,
    appVersion: String,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 30 seconds
    LaunchedEffect(Unit) {
        delay(30_000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Device Status",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Press BACK to close",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status sections
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Column 1: Device & Network
                    Column(modifier = Modifier.weight(1f)) {
                        StatusSection(title = "Device Info") {
                            StatusRow("Device ID", deviceId?.take(8) ?: "Not enrolled")
                            StatusRow("Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                            StatusRow("Android", Build.VERSION.RELEASE)
                            StatusRow("App Version", appVersion)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        StatusSection(title = "Network") {
                            StatusRow(
                                "Status",
                                if (networkState.isOnline) "Online" else "Offline",
                                if (networkState.isOnline) Color.Green else Color.Red
                            )
                            StatusRow("Type", networkState.networkType.name)
                        }
                    }

                    // Column 2: Config & Playback
                    Column(modifier = Modifier.weight(1f)) {
                        StatusSection(title = "Configuration") {
                            StatusRow("Channel", config?.channelId?.take(8) ?: "Not assigned")
                            StatusRow("Schedule", config?.scheduleVersionId?.take(8) ?: "None")
                            StatusRow("Poll Interval", "${config?.pollIntervalSec ?: 60}s")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        StatusSection(title = "Playback") {
                            val (status, statusColor) = when (playbackState) {
                                is PlaybackState.Playing -> "Playing" to Color.Green
                                is PlaybackState.Buffering -> "Buffering" to Color.Yellow
                                is PlaybackState.Error -> "Error" to Color.Red
                                else -> "Idle" to Color.Gray
                            }
                            StatusRow("Status", status, statusColor)

                            if (playbackState is PlaybackState.Playing) {
                                StatusRow("Asset", playbackState.assetId.take(8))
                            }

                            StatusRow("Items", "${playlist?.items?.size ?: 0}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F4C75)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}
