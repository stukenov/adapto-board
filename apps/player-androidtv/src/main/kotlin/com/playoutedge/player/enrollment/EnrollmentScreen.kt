package com.playoutedge.player.enrollment

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enrollment screen composable.
 */
@Composable
fun EnrollmentScreen(
    state: EnrollmentState,
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is EnrollmentState.EnterCode -> EnterCodeContent(
                code = code,
                onCodeChange = onCodeChange,
                onSubmit = onSubmit
            )
            is EnrollmentState.Enrolling -> EnrollingContent()
            is EnrollmentState.WaitingForChannel -> WaitingForChannelContent(
                deviceId = state.deviceId
            )
            is EnrollmentState.Enrolled -> EnrolledContent(
                channelId = state.channelId,
                onComplete = onComplete
            )
            is EnrollmentState.Error -> ErrorContent(
                message = state.message,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun EnterCodeContent(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Enter Enrollment Code",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Enter the 6-character code shown in the admin panel",
            fontSize = 18.sp,
            color = Color.Gray
        )

        // Code input display
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF16213E), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = code.getOrNull(index)?.toString() ?: "",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Virtual keyboard hint
        Text(
            text = "Use remote to enter code",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Button(
            onClick = onSubmit,
            enabled = code.length == 6,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F4C75)
            ),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Enroll Device", fontSize = 18.sp)
        }

        // Device info
        DeviceInfoCard()
    }
}

@Composable
private fun EnrollingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CircularProgressIndicator(
            color = Color(0xFF0F4C75),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "Connecting to server...",
            fontSize = 24.sp,
            color = Color.White
        )
    }
}

@Composable
private fun WaitingForChannelContent(deviceId: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "✓",
            fontSize = 64.sp,
            color = Color(0xFF4CAF50)
        )

        Text(
            text = "Device Enrolled",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Waiting for channel assignment...",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Device ID",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = deviceId.take(8),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Text(
            text = "Assign this device to a channel in the admin panel",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnrolledContent(
    channelId: String,
    onComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onComplete()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "🎉",
            fontSize = 64.sp
        )

        Text(
            text = "Success!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Starting playback...",
            fontSize = 18.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "⚠",
            fontSize = 64.sp,
            color = Color(0xFFF44336)
        )

        Text(
            text = "Enrollment Failed",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = message,
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F4C75)
            )
        ) {
            Text("Try Again", fontSize = 18.sp)
        }
    }
}

@Composable
private fun DeviceInfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
        modifier = Modifier.padding(top = 32.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Device Info",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${Build.MANUFACTURER} ${Build.MODEL}",
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = "Android ${Build.VERSION.RELEASE}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
