package com.daycountapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycountapp.DayCountApp
import com.daycountapp.data.model.Event
import com.daycountapp.ui.components.ColorPickerDialog
import com.daycountapp.ui.components.ColorWheelDialog
import com.daycountapp.ui.components.WheelDatePicker
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.ui.theme.GradientIconEnd
import com.daycountapp.ui.theme.GradientIconStart
import com.daycountapp.ui.theme.TextSecondary
import com.daycountapp.ui.viewmodel.EventViewModel
import com.daycountapp.util.VibrationManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val PRESET_MODE = 0
private const val CUSTOM_MODE = 1
private const val GRADIENT_MODE = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    viewModel: EventViewModel,
    eventId: Long?,
    onNavigateBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var isCountUp by remember { mutableStateOf(false) }
    var isPinned by remember { mutableStateOf(false) }

    var colorMode by remember { mutableIntStateOf(PRESET_MODE) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    val isEditing = eventId != null
    val app = DayCountApp.instance
    val scope = rememberCoroutineScope()

    // 事件配色（独立存储）
    val eventCustomArgb by app.appSettings.eventCustomColorArgb.collectAsState(initial = 0)
    val eventGradientStartIdx by app.appSettings.eventGradientStartIndex.collectAsState(initial = -1)
    val eventGradientEndIdx by app.appSettings.eventGradientEndIndex.collectAsState(initial = -1)
    val eventGradientStartArgb by app.appSettings.eventGradientStartArgb.collectAsState(initial = 0)
    val eventGradientEndArgb by app.appSettings.eventGradientEndArgb.collectAsState(initial = 0)
    val eventGradientDirection by app.appSettings.eventGradientDirection.collectAsState(initial = 0)

    var pendingCustomEdit by remember { mutableStateOf(false) }
    var showGradientPicker by remember { mutableStateOf(false) }

    if (pendingCustomEdit) {
        ColorWheelDialog(
            initialColor = if (eventCustomArgb != 0) Color(eventCustomArgb) else Color.Red,
            onColorSelected = { color ->
                scope.launch { app.appSettings.setEventCustomColorArgb(color.toArgb()) }
                pendingCustomEdit = false
            },
            onDismiss = { pendingCustomEdit = false },
        )
    }

    if (showGradientPicker) {
        val initStart = if (eventGradientStartIdx >= 0) eventGradientStartIdx else 0
        val initEnd = if (eventGradientEndIdx >= 0) eventGradientEndIdx else 0
        ColorPickerDialog(
            currentColor = initStart,
            currentColorEnd = initEnd,
            currentDirection = eventGradientDirection,
            onDismiss = { showGradientPicker = false },
            onColorSelected = { _, index ->
                colorMode = PRESET_MODE
                selectedPresetIndex = index
                scope.launch { app.appSettings.setEventColorIndex(index) }
                showGradientPicker = false
            },
            enableGradient = true,
            customColorArgb = eventGradientStartArgb,
            customColorEndArgb = eventGradientEndArgb,
            onCustomColorPicked = { primaryArgb, secondaryArgb ->
                scope.launch {
                    app.appSettings.setEventCustomColorArgb(primaryArgb)
                    app.appSettings.setEventGradient(
                        if (primaryArgb != 0) CUSTOM_COLOR_INDEX else 0,
                        CUSTOM_COLOR_INDEX,
                        primaryArgb,
                        secondaryArgb,
                        eventGradientDirection,
                    )
                }
            },
            onGradientSelected = { _, _, startIdx, endIdx, direction ->
                scope.launch {
                    app.appSettings.setEventGradient(
                        startIdx,
                        endIdx,
                        eventGradientStartArgb,
                        eventGradientEndArgb,
                        direction,
                    )
                }
                colorMode = GRADIENT_MODE
                showGradientPicker = false
            },
        )
    }

    LaunchedEffect(eventId) {
        if (eventId != null) viewModel.loadEventById(eventId)
    }

    val editingEvent by viewModel.eventDetail.collectAsState()
    LaunchedEffect(editingEvent) {
        val event = editingEvent
        if (event != null && isEditing) {
            title = event.title
            description = event.description
            targetDate = event.targetDate
            isCountUp = event.isCountUp
            isPinned = event.isPinned
            selectedPresetIndex = event.colorPreset
            colorMode =
                when {
                    event.colorPresetEnd >= 0 -> GRADIENT_MODE
                    event.colorPreset == CUSTOM_COLOR_INDEX -> CUSTOM_MODE
                    else -> PRESET_MODE
                }
        }
    }

    // 预见性返回
    BackHandler {
        VibrationManager.vibrate(10L)
        onNavigateBack()
    }

    // 日期变化震动反馈
    var previousDate by remember { mutableLongStateOf(targetDate) }
    LaunchedEffect(targetDate) {
        if (targetDate != previousDate) {
            previousDate = targetDate
            VibrationManager.vibrate(10L)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    VibrationManager.vibrate(10L)
                    onNavigateBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8FD4\u56DE")
                }
                Text(
                    text = if (isEditing) "\u7F16\u8F91\u4E8B\u4EF6" else "\u65B0\u5EFA\u4E8B\u4EF6",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("\u4E8B\u4EF6\u540D\u79F0") },
                placeholder = { Text("\u8F93\u5165\u4E8B\u4EF6\u540D\u79F0", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("\u4E8B\u4EF6\u63CF\u8FF0") },
                placeholder = {
                    Text(
                        "\u63CF\u8FF0\u8FD9\u4E2A\u91CD\u8981\u7684\u65E5\u5B50\uFF08\u53EF\u9009\uFF09",
                        color = TextSecondary,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
            )

            Text("\u76EE\u6807\u65E5\u671F", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            WheelDatePicker(initialDate = targetDate, onDateSelected = { targetDate = it }, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("\u4E8B\u4EF6\u7C7B\u578B", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "\u5012\u6570\u65E5",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!isCountUp) MaterialTheme.colorScheme.primary else TextSecondary,
                    )
                    Switch(
                        checked = isCountUp,
                        onCheckedChange = { isCountUp = it },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = switchColors(),
                    )
                    Text(
                        "\u6B63\u6570\u65E5",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCountUp) MaterialTheme.colorScheme.primary else TextSecondary,
                    )
                }
            }

            Text("\u4E8B\u4EF6\u914D\u8272", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EventColors.forEachIndexed { index, color ->
                    val isSelected = colorMode == PRESET_MODE && selectedPresetIndex == index
                    Surface(
                        modifier =
                            Modifier.size(44.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                colorMode = PRESET_MODE
                                selectedPresetIndex = index
                                scope.launch { app.appSettings.setEventColorIndex(index) }
                            },
                        shape = CircleShape,
                        color = color,
                        border =
                            if (isSelected) {
                                androidx.compose.foundation.BorderStroke(
                                    3.dp,
                                    MaterialTheme.colorScheme.onBackground,
                                )
                            } else {
                                null
                            },
                    ) {}
                }

                Surface(
                    modifier =
                        Modifier.size(44.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            if (colorMode == CUSTOM_MODE && eventCustomArgb != 0) {
                                pendingCustomEdit = true
                            } else {
                                colorMode = CUSTOM_MODE
                                if (eventCustomArgb == 0) {
                                    pendingCustomEdit = true
                                }
                                scope.launch { app.appSettings.setEventColorIndex(CUSTOM_COLOR_INDEX) }
                            }
                        },
                    shape = CircleShape,
                    color = if (eventCustomArgb != 0) Color(eventCustomArgb) else MaterialTheme.colorScheme.surfaceVariant,
                    border =
                        if (colorMode ==
                            CUSTOM_MODE
                        ) {
                            androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onBackground)
                        } else {
                            null
                        },
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (colorMode == CUSTOM_MODE) {
                            Text(
                                text = "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color =
                                    if (eventCustomArgb != 0) {
                                        if (Color(
                                                eventCustomArgb,
                                            ).let { it.red * 0.299 + it.green * 0.587 + it.blue * 0.114 > 0.5 }
                                        ) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }
                }

                Surface(
                    modifier =
                        Modifier.size(44.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            if (colorMode == GRADIENT_MODE) {
                                showGradientPicker = true
                            } else {
                                colorMode = GRADIENT_MODE
                                scope.launch { app.appSettings.setEventColorIndex(CUSTOM_COLOR_INDEX) }
                            }
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border =
                        if (colorMode ==
                            GRADIENT_MODE
                        ) {
                            androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onBackground)
                        } else {
                            null
                        },
                ) {
                    Box(
                        modifier =
                            Modifier.size(36.dp).clip(CircleShape).background(
                                if (eventGradientStartIdx >= 0) {
                                    val start =
                                        when {
                                            eventGradientStartIdx == CUSTOM_COLOR_INDEX && eventGradientStartArgb != 0 -> {
                                                Color(
                                                    eventGradientStartArgb,
                                                )
                                            }

                                            eventGradientStartIdx in EventColors.indices -> {
                                                EventColors[eventGradientStartIdx]
                                            }

                                            else -> {
                                                GradientIconStart
                                            }
                                        }
                                    val end =
                                        when {
                                            eventGradientEndIdx == CUSTOM_COLOR_INDEX && eventGradientEndArgb != 0 -> {
                                                Color(
                                                    eventGradientEndArgb,
                                                )
                                            }

                                            eventGradientEndIdx in EventColors.indices -> {
                                                EventColors[eventGradientEndIdx]
                                            }

                                            else -> {
                                                GradientIconEnd
                                            }
                                        }
                                    GradientDirection.createBrush(eventGradientDirection, listOf(start, end))
                                } else {
                                    GradientDirection.createBrush(0, listOf(GradientIconStart, GradientIconEnd))
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (colorMode == GRADIENT_MODE) {
                            Text(
                                text = "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.Black,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("\u7F6E\u9876\u663E\u793A", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Switch(checked = isPinned, onCheckedChange = { isPinned = it }, colors = switchColors())
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    VibrationManager.vibrate(10L)
                    scope.launch {
                    // 确定颜色设置
                    val usePreviewDefault = app.appSettings.usePreviewAsDefault.first()
                    val (finalColorPreset, finalColorPresetEnd, finalGradientDirection, finalCustomColorArgb, finalCustomColorEndArgb) = if (usePreviewDefault && !isEditing) {
                        // 使用预览事件的颜色
                        val previewEvent = app.eventRepository.getPreviewEvent()
                        if (previewEvent != null) {
                            listOf(
                                previewEvent.colorPreset,
                                previewEvent.colorPresetEnd,
                                previewEvent.gradientDirection,
                                previewEvent.customColorArgb,
                                previewEvent.customColorEndArgb
                            )
                        } else {
                            // 回退到当前选择
                            listOf(
                                when (colorMode) {
                                    CUSTOM_MODE -> CUSTOM_COLOR_INDEX
                                    GRADIENT_MODE -> if (eventGradientStartIdx >= 0) eventGradientStartIdx else 0
                                    else -> selectedPresetIndex
                                },
                                when (colorMode) {
                                    GRADIENT_MODE -> if (eventGradientEndIdx >= 0) eventGradientEndIdx else -1
                                    else -> -1
                                },
                                if (colorMode == GRADIENT_MODE) eventGradientDirection else 0,
                                if (colorMode == CUSTOM_MODE || colorMode == GRADIENT_MODE) eventCustomArgb else 0,
                                if (colorMode == GRADIENT_MODE) eventGradientEndArgb else 0,
                            )
                        }
                    } else {
                        // 使用当前选择
                        listOf(
                            when (colorMode) {
                                CUSTOM_MODE -> CUSTOM_COLOR_INDEX
                                GRADIENT_MODE -> if (eventGradientStartIdx >= 0) eventGradientStartIdx else 0
                                else -> selectedPresetIndex
                            },
                            when (colorMode) {
                                GRADIENT_MODE -> if (eventGradientEndIdx >= 0) eventGradientEndIdx else -1
                                else -> -1
                            },
                            if (colorMode == GRADIENT_MODE) eventGradientDirection else 0,
                            if (colorMode == CUSTOM_MODE || colorMode == GRADIENT_MODE) eventCustomArgb else 0,
                            if (colorMode == GRADIENT_MODE) eventGradientEndArgb else 0,
                        )
                    }

                    val event =
                        Event(
                            id = eventId ?: 0,
                            title = title,
                            description = description,
                            targetDate = targetDate,
                            isCountUp = isCountUp,
                            isPinned = isPinned,
                            colorPreset = finalColorPreset,
                            colorPresetEnd = finalColorPresetEnd,
                            gradientDirection = finalGradientDirection,
                            customColorArgb = finalCustomColorArgb,
                            customColorEndArgb = finalCustomColorEndArgb,
                        )
                    if (isEditing) {
                        val existingEvent = editingEvent
                        if (existingEvent != null) {
                            viewModel.updateEvent(
                                event.copy(
                                    backgroundUri = existingEvent.backgroundUri,
                                    backgroundOpacity = existingEvent.backgroundOpacity,
                                    isPreview = existingEvent.isPreview,
                                    isDeleted = existingEvent.isDeleted,
                                    deleteTime = existingEvent.deleteTime,
                                    createTime = existingEvent.createTime,
                                ),
                            )
                        }
                    } else {
                        viewModel.insertEvent(event)
                    }
                    onNavigateBack()
                    } // scope.launch
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = title.isNotBlank(),
            ) {
                Text(
                    text = if (isEditing) "\u4FDD\u5B58\u4FEE\u6539" else "\u521B\u5EFA\u4E8B\u4EF6",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun switchColors() =
    SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    )
