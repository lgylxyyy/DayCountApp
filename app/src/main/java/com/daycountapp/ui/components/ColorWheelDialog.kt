package com.daycountapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ColorWheelDialog(
    initialColor: Color = Color.Red,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    var pickedColor by remember { mutableStateOf(initialColor) }

    val wheelSize = 240
    val wheelBitmap =
        remember {
            val bmp = Bitmap.createBitmap(wheelSize, wheelSize, Bitmap.Config.ARGB_8888)
            val cx = wheelSize / 2f
            val cy = wheelSize / 2f
            val r = wheelSize / 2f
            for (y in 0 until wheelSize) {
                for (x in 0 until wheelSize) {
                    val dx = x - cx
                    val dy = y - cy
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist <= r) {
                        val hue = (atan2(dy, dx) * 180f / PI.toFloat() + 90f + 360f) % 360f
                        val sat = dist / r
                        bmp.setPixel(x, y, android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, 1f)))
                    }
                }
            }
            bmp
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自定义颜色", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 取色圆环（边框在外层，消除锯齿）
                Box(
                    modifier =
                        Modifier
                            .size(wheelSize.dp)
                            .border(2.dp, Color.Gray, CircleShape),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val pos = event.changes.first().position
                                            val cx = size.width / 2f
                                            val cy = size.height / 2f
                                            val dx = pos.x - cx
                                            val dy = pos.y - cy
                                            val dist = sqrt(dx * dx + dy * dy)
                                            val radius = size.width / 2f

                                            if (dist <= radius) {
                                                var hue = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                                                if (hue < 0) hue += 360f
                                                val saturation = (dist / radius).coerceIn(0f, 1f)
                                                pickedColor = Color.hsv(hue, saturation, 1f)
                                            }
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                },
                    ) {
                        Image(
                            bitmap = wheelBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(pickedColor)
                                .border(2.dp, Color.Gray.copy(alpha = 0.3f), CircleShape),
                    )
                    Column {
                        Text(
                            "当前颜色",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "#${String.format("%06X", pickedColor.toArgb() and 0xFFFFFF)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onColorSelected(pickedColor) }) { Text("应用") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
