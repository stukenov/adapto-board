package com.playoutedge.player.overlay

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Overlay container that renders widgets on top of video.
 */
@Composable
fun OverlayContainer(
    state: OverlayState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        state.widgets.values
            .filter { it.visible }
            .forEach { widget ->
                WidgetRenderer(
                    widget = widget,
                    modifier = Modifier.align(widget.position.anchor.toAlignment())
                )
            }
    }
}

@Composable
private fun WidgetRenderer(
    widget: Widget,
    modifier: Modifier = Modifier
) {
    val offsetModifier = modifier
        .offset(
            x = widget.position.x.dp,
            y = widget.position.y.dp
        )

    AnimatedVisibility(
        visible = widget.visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when (widget) {
            is Widget.Ticker -> TickerWidget(widget, offsetModifier)
            is Widget.Text -> TextWidget(widget, offsetModifier)
            is Widget.Table -> TableWidget(widget, offsetModifier)
            is Widget.QrCode -> QrCodeWidget(widget, offsetModifier)
            is Widget.Kpi -> KpiWidget(widget, offsetModifier)
        }
    }
}

@Composable
private fun TickerWidget(
    widget: Widget.Ticker,
    modifier: Modifier
) {
    var offset by remember { mutableStateOf(0f) }

    LaunchedEffect(widget.text, widget.speed) {
        while (true) {
            offset -= 1f
            delay((1000 / widget.speed).toLong())
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(parseColor(widget.backgroundColor)))
    ) {
        Text(
            text = widget.text,
            color = Color(parseColor(widget.textColor)),
            fontSize = 24.sp,
            modifier = Modifier
                .offset(x = offset.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun TextWidget(
    widget: Widget.Text,
    modifier: Modifier
) {
    Text(
        text = widget.text,
        color = Color(parseColor(widget.textColor)),
        fontSize = widget.fontSize.sp,
        modifier = modifier.padding(16.dp)
    )
}

@Composable
private fun TableWidget(
    widget: Widget.Table,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xCC000000))
            .padding(16.dp)
    ) {
        // Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            widget.headers.forEach { header ->
                Text(
                    text = header,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rows
        widget.rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun QrCodeWidget(
    widget: Widget.QrCode,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // QR code placeholder - would use QR library
        Box(
            modifier = Modifier
                .size(widget.size.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QR",
                fontSize = 24.sp,
                color = Color.Gray
            )
        }

        widget.label?.let { label ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun KpiWidget(
    widget: Widget.Kpi,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xCC000000))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = widget.title,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = widget.value,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            widget.unit?.let { unit ->
                Text(
                    text = unit,
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        }

        widget.trend?.let { trend ->
            val (icon, color) = when (trend) {
                "up" -> "↑" to Color.Green
                "down" -> "↓" to Color.Red
                else -> "→" to Color.Gray
            }
            Text(
                text = icon,
                fontSize = 24.sp,
                color = color
            )
        }
    }
}

private fun Anchor.toAlignment(): Alignment = when (this) {
    Anchor.TOP_LEFT -> Alignment.TopStart
    Anchor.TOP_RIGHT -> Alignment.TopEnd
    Anchor.BOTTOM_LEFT -> Alignment.BottomStart
    Anchor.BOTTOM_RIGHT -> Alignment.BottomEnd
    Anchor.CENTER -> Alignment.Center
}

private fun parseColor(hex: String): Long {
    val cleanHex = hex.removePrefix("#")
    return when (cleanHex.length) {
        6 -> "FF$cleanHex".toLong(16)
        8 -> cleanHex.toLong(16)
        else -> 0xFFFFFFFF
    }
}
