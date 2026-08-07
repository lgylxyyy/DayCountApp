package com.daycountapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection

@Composable
fun ColorPickerDialog(
    currentColor: Int = 0,
    currentColorEnd: Int = -1,
    currentDirection: Int = 0,
    onDismiss: () -> Unit,
    onColorSelected: (Color, Int) -> Unit,
    enableGradient: Boolean = false,
    customColorArgb: Int = 0,
    customColorEndArgb: Int = 0,
    onCustomColorPicked: ((Int, Int) -> Unit)? = null,
    onGradientSelected: ((startColor: Color, endColor: Color, startIndex: Int, endIndex: Int, direction: Int) -> Unit)? = null,
) {
    val safePrimaryIndex = if (currentColor in EventColors.indices) currentColor else 0
    val safeEndIndex = if (currentColorEnd in EventColors.indices) currentColorEnd else -1
    val safeDirection = if (currentDirection in 0..3) currentDirection else 0

    var step by remember { mutableIntStateOf(0) } // 0=primary, 1=secondary, 2=direction
    var primaryIndex by remember { mutableIntStateOf(safePrimaryIndex) }
    var secondaryIndex by remember { mutableIntStateOf(safeEndIndex) }
    var directionIndex by remember { mutableIntStateOf(safeDirection) }
    var localCustomPrimaryArgb by remember { mutableIntStateOf(if (primaryIndex == CUSTOM_COLOR_INDEX) customColorArgb else 0) }
    var localCustomSecondaryArgb by remember { mutableIntStateOf(if (secondaryIndex == CUSTOM_COLOR_INDEX) customColorEndArgb else 0) }
    var showWheel by remember { mutableStateOf(false) }
    var wheelForStep by remember { mutableIntStateOf(0) } // 0=primary, 1=secondary

    val previewStart =
        when {
            primaryIndex == CUSTOM_COLOR_INDEX && localCustomPrimaryArgb != 0 -> Color(localCustomPrimaryArgb)
            primaryIndex in EventColors.indices -> EventColors[primaryIndex]
            else -> Color.Gray
        }
    val previewEnd =
        when {
            secondaryIndex == CUSTOM_COLOR_INDEX && localCustomSecondaryArgb != 0 -> Color(localCustomSecondaryArgb)
            secondaryIndex in EventColors.indices -> EventColors[secondaryIndex]
            else -> previewStart
        }
    val hasGradient = enableGradient && secondaryIndex >= 0

    // 取色圆环弹窗（内嵌）
    if (showWheel) {
        ColorWheelDialog(
            initialColor =
                if (wheelForStep == 0) {
                    if (localCustomPrimaryArgb != 0) Color(localCustomPrimaryArgb) else Color.Red
                } else {
                    if (localCustomSecondaryArgb != 0) Color(localCustomSecondaryArgb) else Color.Red
                },
            onColorSelected = { color ->
                if (wheelForStep == 0) {
                    localCustomPrimaryArgb = color.toArgb()
                    primaryIndex = CUSTOM_COLOR_INDEX
                    secondaryIndex = -1
                    step = 1
                } else {
                    localCustomSecondaryArgb = color.toArgb()
                    secondaryIndex = CUSTOM_COLOR_INDEX
                    step = 2
                }
                showWheel = false
            },
            onDismiss = { showWheel = false },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    when {
                        !enableGradient -> "\u81EA\u5B9A\u4E49\u914D\u8272"
                        step == 0 -> "\u9009\u62E9\u4E3B\u8272"
                        step == 1 -> "\u9009\u62E9\u526F\u8272"
                        else -> "\u6E10\u53D8\u8BBE\u7F6E"
                    },
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ===== \u5B9E\u65F6\u6E10\u53D8\u9884\u89C8 =====
                if (enableGradient) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    GradientDirection.createBrush(
                                        directionIndex,
                                        listOf(previewStart, previewEnd),
                                    ),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!hasGradient) {
                            Text(
                                text = if (step == 0) "\u9009\u62E9\u4E3B\u8272\u540E\u7EE7\u7EED..." else "\u8BF7\u9009\u62E9\u526F\u8272",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                // ===== \u4E3B\u8272/\u526F\u8272 \u5207\u6362 =====
                if (enableGradient) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = step == 0,
                            onClick = { step = 0 },
                            label = { Text("\u4E3B\u8272") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(14.dp).clip(CircleShape).background(previewStart),
                                )
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                        FilterChip(
                            selected = step == 1,
                            onClick = { if (secondaryIndex >= 0) step = 1 },
                            label = { Text("\u526F\u8272") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(14.dp).clip(CircleShape).background(previewEnd),
                                )
                            },
                        )
                    }
                }

                // ===== \u989C\u8272\u7F51\u683C 5\u5217x3\u884C (14\u9884\u8BBE + 1\u81EA\u5B9A\u4E49) =====
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        for (col in 0..4) {
                            val idx = row * 5 + col
                            if (idx < EventColors.size) {
                                val color = EventColors[idx]
                                val isSel = if (step == 0) idx == primaryIndex else idx == secondaryIndex
                                Box(
                                    modifier =
                                        Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                            ) {
                                                if (step == 0) {
                                                    primaryIndex = idx
                                                    secondaryIndex = -1
                                                    if (!enableGradient) {
                                                        onColorSelected(color, idx)
                                                    } else {
                                                        step = 1
                                                    }
                                                } else {
                                                    secondaryIndex = idx
                                                    step = 2
                                                }
                                            }.then(
                                                if (isSel) {
                                                    Modifier.border(3.dp, Color.White, CircleShape)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                )
                            } else if (idx == EventColors.size) {
                                // \u7B2C 15 \u4E2A\uFF1A\u81EA\u5B9A\u4E49\u989C\u8272
                                val isCustomSel =
                                    if (step ==
                                        0
                                    ) {
                                        primaryIndex == CUSTOM_COLOR_INDEX
                                    } else {
                                        secondaryIndex == CUSTOM_COLOR_INDEX
                                    }
                                Box(
                                    modifier =
                                        Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (step == 0) {
                                                    if (localCustomPrimaryArgb != 0) {
                                                        Color(localCustomPrimaryArgb)
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                } else {
                                                    if (localCustomSecondaryArgb != 0) {
                                                        Color(localCustomSecondaryArgb)
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                },
                                            ).clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                            ) {
                                                wheelForStep = step
                                                showWheel = true
                                            }.then(
                                                if (isCustomSel) {
                                                    Modifier.border(3.dp, Color.White, CircleShape)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "+",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Light,
                                        color =
                                            if (step == 0) {
                                                if (localCustomPrimaryArgb != 0) {
                                                    if (Color(
                                                            localCustomPrimaryArgb,
                                                        ).let { it.red * 0.299 + it.green * 0.587 + it.blue * 0.114 > 0.5f }
                                                    ) {
                                                        Color.Black
                                                    } else {
                                                        Color.White
                                                    }
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            } else {
                                                if (localCustomSecondaryArgb != 0) {
                                                    if (Color(
                                                            localCustomSecondaryArgb,
                                                        ).let { it.red * 0.299 + it.green * 0.587 + it.blue * 0.114 > 0.5f }
                                                    ) {
                                                        Color.Black
                                                    } else {
                                                        Color.White
                                                    }
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            },
                                    )
                                }
                            } else {
                                Spacer(Modifier.size(48.dp))
                            }
                        }
                    }
                }

                // ===== \u6E10\u53D8\u65B9\u5411 (step == 2) =====
                if (enableGradient && step == 2 && hasGradient) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "\u6E10\u53D8\u65B9\u5411",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        GradientDirection.entries.forEach { dir ->
                            val isDirSel = directionIndex == dir.id
                            Surface(
                                modifier =
                                    Modifier
                                        .size(64.dp, 52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .then(
                                            if (isDirSel) {
                                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                            } else {
                                                Modifier
                                            },
                                        ).clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { directionIndex = dir.id },
                                shape = RoundedCornerShape(12.dp),
                                color =
                                    if (isDirSel) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(48.dp, 36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                GradientDirection.createBrush(dir.id, listOf(previewStart, previewEnd)),
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = dir.icon,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        GradientDirection.entries.forEach { dir ->
                            Text(
                                text = dir.label,
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    if (directionIndex == dir.id) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                modifier = Modifier.width(64.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text =
                        when {
                            !enableGradient -> "\u70B9\u51FB\u5706\u5F62\u9009\u62E9\u914D\u8272"
                            step == 0 -> "\u70B9\u51FB\u9009\u62E9\u4E3B\u8272 \u2192"
                            step == 1 -> "\u70B9\u51FB\u9009\u62E9\u526F\u8272\u5B8C\u6210\u6E10\u53D8"
                            step == 2 -> "\u8C03\u6574\u65B9\u5411\u540E\u70B9\u51FB\u201C\u5E94\u7528\u201D"
                            else -> ""
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            if (enableGradient && step == 2 && hasGradient) {
                TextButton(onClick = {
                    onGradientSelected?.invoke(
                        previewStart,
                        previewEnd,
                        primaryIndex,
                        secondaryIndex,
                        directionIndex,
                    )
                    // 如果有自定义颜色，传回 ARGB 值
                    if (primaryIndex == CUSTOM_COLOR_INDEX || secondaryIndex == CUSTOM_COLOR_INDEX) {
                        onCustomColorPicked?.invoke(localCustomPrimaryArgb, localCustomSecondaryArgb)
                    }
                }) { Text("\u5E94\u7528\u6E10\u53D8") }
            } else if (!enableGradient) {
                TextButton(onClick = onDismiss) { Text("\u53D6\u6D88") }
            } else if (step == 1) {
                TextButton(onClick = { step = 2 }) { Text("\u4E0B\u4E00\u6B65\uFF1A\u65B9\u5411") }
            } else {
                TextButton(onClick = onDismiss) { Text("\u53D6\u6D88") }
            }
        },
        dismissButton = {
            if (enableGradient && step == 1) {
                TextButton(onClick = { step = 0 }) { Text("\u8FD4\u56DE\u4E3B\u8272") }
            } else if (enableGradient && step == 2) {
                TextButton(onClick = { step = 1 }) { Text("\u8FD4\u56DE\u526F\u8272") }
            }
        },
    )
}
