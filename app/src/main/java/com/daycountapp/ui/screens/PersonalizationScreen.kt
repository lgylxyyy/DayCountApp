package com.daycountapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycountapp.DayCountApp
import com.daycountapp.data.model.Event
import com.daycountapp.ui.components.ColorPickerDialog
import com.daycountapp.ui.components.ColorWheelDialog
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.ui.theme.GradientIconEnd
import com.daycountapp.ui.theme.GradientIconStart
import com.daycountapp.ui.theme.TextSecondary
import com.daycountapp.util.DateUtil
import kotlinx.coroutines.launch

private const val PRESET_MODE = 0
private const val CUSTOM_MODE = 1
private const val GRADIENT_MODE = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMyBackground: () -> Unit = {},
    onNavigateToImageCrop: (Uri) -> Unit = {},
    onNavigateToPreviewEdit: (Long) -> Unit = {},
    isEditingPreview: Boolean = false,
) {
    val app = DayCountApp.instance
    val scope = rememberCoroutineScope()

    var colorMode by remember { mutableIntStateOf(PRESET_MODE) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    // 主题配色
    val themeCustomArgb by app.appSettings.themeCustomColorArgb.collectAsState(initial = 0)
    val themeGradientStartIdx by app.appSettings.themeGradientStartIndex.collectAsState(initial = -1)
    val themeGradientEndIdx by app.appSettings.themeGradientEndIndex.collectAsState(initial = -1)
    val themeGradientStartArgb by app.appSettings.themeGradientStartArgb.collectAsState(initial = 0)
    val themeGradientEndArgb by app.appSettings.themeGradientEndArgb.collectAsState(initial = 0)
    val themeGradientDirection by app.appSettings.themeGradientDirection.collectAsState(initial = 0)

    var pendingCustomEdit by remember { mutableStateOf(false) }
    var showGradientPicker by remember { mutableStateOf(false) }

    // 背景设置
    val appBackgroundEnabled by app.appSettings.appBackgroundEnabled.collectAsState(initial = true)
    val darkModeBackground by app.appSettings.darkModeBackground.collectAsState(initial = true)
    val backgroundOpacity by app.appSettings.backgroundOpacity.collectAsState(initial = 0.5f)
    val backgroundBlur by app.appSettings.backgroundBlur.collectAsState(initial = 0f)
    val barOpacity by app.appSettings.barOpacity.collectAsState(initial = 0.3f)
    val cardOpacity by app.appSettings.cardOpacity.collectAsState(initial = 0.9f)
    val usePreviewAsDefault by app.appSettings.usePreviewAsDefault.collectAsState(initial = false)

    // 预览事件（编辑完成后刷新）
    var previewEvent by remember { mutableStateOf<Event?>(null) }
    var lastEditState by remember { mutableStateOf(isEditingPreview) }
    LaunchedEffect(isEditingPreview) {
        // 从编辑状态返回时刷新
        if (lastEditState && !isEditingPreview) {
            previewEvent = app.eventRepository.getOrCreatePreviewEvent()
        }
        lastEditState = isEditingPreview
        if (previewEvent == null) {
            previewEvent = app.eventRepository.getOrCreatePreviewEvent()
        }
    }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onNavigateToImageCrop(uri)
        }
    }

    if (pendingCustomEdit) {
        ColorWheelDialog(
            initialColor = if (themeCustomArgb != 0) Color(themeCustomArgb) else Color.Red,
            onColorSelected = { color ->
                scope.launch {
                    app.appSettings.setThemeCustomColorArgb(color.toArgb())
                    app.appSettings.setThemeColorIndex(CUSTOM_COLOR_INDEX)
                }
                pendingCustomEdit = false
            },
            onDismiss = { pendingCustomEdit = false },
        )
    }

    if (showGradientPicker) {
        ColorPickerDialog(
            currentColor = if (themeGradientStartIdx >= 0) themeGradientStartIdx else 0,
            currentColorEnd = if (themeGradientEndIdx >= 0) themeGradientEndIdx else 0,
            currentDirection = themeGradientDirection,
            onDismiss = { showGradientPicker = false },
            onColorSelected = { _, index ->
                colorMode = PRESET_MODE
                selectedPresetIndex = index
                scope.launch {
                    app.appSettings.setThemeColorIndex(index)
                    app.appSettings.clearThemeGradient()
                }
                showGradientPicker = false
            },
            enableGradient = true,
            customColorArgb = themeGradientStartArgb,
            customColorEndArgb = themeGradientEndArgb,
            onCustomColorPicked = { primaryArgb, secondaryArgb ->
                scope.launch {
                    app.appSettings.setThemeCustomColorArgb(primaryArgb)
                    val startIdx = if (primaryArgb != 0) CUSTOM_COLOR_INDEX else 0
                    app.appSettings.setThemeGradient(startIdx, CUSTOM_COLOR_INDEX, primaryArgb, secondaryArgb, themeGradientDirection)
                }
            },
            onGradientSelected = { _, _, startIdx, endIdx, direction ->
                scope.launch {
                    app.appSettings.setThemeGradient(startIdx, endIdx, themeGradientStartArgb, themeGradientEndArgb, direction)
                }
                colorMode = GRADIENT_MODE
                showGradientPicker = false
            },
        )
    }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    )

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // ==================== 应用背景部分 ====================
            // 主开关
            SettingsItem(
                title = "应用背景",
                description = "启用后可设置个性化背景图片",
                trailing = {
                    Switch(
                        checked = appBackgroundEnabled,
                        onCheckedChange = { scope.launch { app.appSettings.setAppBackgroundEnabled(it) } },
                        colors = switchColors,
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            // 主开关打开时显示的选项
            if (appBackgroundEnabled) {
                // 我的背景
                SettingsItem(
                    title = "我的背景",
                    description = "管理预设和历史背景",
                    trailing = {
                        Text("›", style = MaterialTheme.typography.headlineMedium, color = TextSecondary)
                    },
                    onClick = { onNavigateToMyBackground() },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

                // 选择新背景（支持渐变主题色）
                val primaryColor = MaterialTheme.colorScheme.primary
                val buttonBrush = if (themeGradientStartIdx >= 0) {
                    val start = when {
                        themeGradientStartIdx == CUSTOM_COLOR_INDEX && themeGradientStartArgb != 0 -> Color(themeGradientStartArgb)
                        themeGradientStartIdx in EventColors.indices -> EventColors[themeGradientStartIdx]
                        else -> primaryColor
                    }
                    val end = when {
                        themeGradientEndIdx == CUSTOM_COLOR_INDEX && themeGradientEndArgb != 0 -> Color(themeGradientEndArgb)
                        themeGradientEndIdx in EventColors.indices -> EventColors[themeGradientEndIdx]
                        else -> primaryColor.copy(alpha = 0.7f)
                    }
                    GradientDirection.createBrush(themeGradientDirection, listOf(start, end))
                } else if (themeCustomArgb != 0) {
                    Brush.horizontalGradient(colors = listOf(Color(themeCustomArgb), Color(themeCustomArgb).copy(alpha = 0.7f)))
                } else {
                    Brush.horizontalGradient(colors = listOf(primaryColor, primaryColor.copy(alpha = 0.7f)))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(buttonBrush)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "选择新背景",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 内容不透明度
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        "内容不透明度: ${(backgroundOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = backgroundOpacity,
                        onValueChange = { scope.launch { app.appSettings.setBackgroundOpacity(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        valueRange = 0f..1f,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

                // 背景模糊度
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        "背景模糊度: ${(backgroundBlur * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = backgroundBlur,
                        onValueChange = { scope.launch { app.appSettings.setBackgroundBlur(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        valueRange = 0f..1f,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

                // 磨砂效果强度
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        "磨砂效果强度: ${(barOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = barOpacity,
                        onValueChange = { scope.launch { app.appSettings.setBarOpacity(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        valueRange = 0f..1f,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

                // 暗色背景
                SettingsItem(
                    title = "暗色背景",
                    description = "在暗色模式下显示背景图片",
                    trailing = {
                        Switch(
                            checked = darkModeBackground,
                            onCheckedChange = { scope.launch { app.appSettings.setDarkModeBackground(it) } },
                            colors = switchColors,
                        )
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))
            }

            // ==================== 卡片设置部分 ====================
            Spacer(Modifier.height(8.dp))

            // 卡片透明度 + 预览
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                // 预览卡片
                previewEvent?.let { event ->
                    PreviewCard(
                        event = event,
                        cardOpacity = cardOpacity,
                        onEditClick = { onNavigateToPreviewEdit(event.id) }
                    )
                }

                Text(
                    "卡片透明度: ${(cardOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = cardOpacity,
                    onValueChange = { scope.launch { app.appSettings.setCardOpacity(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0.3f..1f,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            // 设为默认样式
            SettingsItem(
                title = "设为默认样式",
                description = "新建卡片使用预览卡片的颜色",
                trailing = {
                    Switch(
                        checked = usePreviewAsDefault,
                        onCheckedChange = { scope.launch { app.appSettings.setUsePreviewAsDefault(it) } },
                        colors = switchColors,
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            // ==================== 主题配色部分 ====================
            Spacer(Modifier.height(8.dp))

            Text(
                "主题配色",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            Text(
                "可滑动浏览更多颜色",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EventColors.forEachIndexed { index, color ->
                    val isSelected = colorMode == PRESET_MODE && selectedPresetIndex == index
                    Surface(
                        modifier = Modifier.size(44.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            colorMode = PRESET_MODE
                            selectedPresetIndex = index
                            scope.launch {
                                app.appSettings.setThemeColorIndex(index)
                                app.appSettings.clearThemeGradient()
                            }
                        },
                        shape = CircleShape,
                        color = color,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onBackground)
                        } else null,
                    ) {}
                }

                // 自定义颜色
                Surface(
                    modifier = Modifier.size(44.dp).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (colorMode == CUSTOM_MODE && themeCustomArgb != 0) {
                            pendingCustomEdit = true
                        } else {
                            if (themeCustomArgb != 0) {
                                colorMode = CUSTOM_MODE
                                scope.launch { app.appSettings.setThemeColorIndex(CUSTOM_COLOR_INDEX) }
                            } else {
                                colorMode = CUSTOM_MODE
                                pendingCustomEdit = true
                            }
                        }
                    },
                    shape = CircleShape,
                    color = if (themeCustomArgb != 0) Color(themeCustomArgb) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (colorMode == CUSTOM_MODE) {
                        androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onBackground)
                    } else null,
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (colorMode == CUSTOM_MODE) {
                            Text(
                                text = "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color = if (themeCustomArgb != 0) {
                                    if (Color(themeCustomArgb).let { it.red * 0.299 + it.green * 0.587 + it.blue * 0.114 > 0.5 }) {
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

                // 渐变色
                Surface(
                    modifier = Modifier.size(44.dp).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (colorMode == GRADIENT_MODE) {
                            showGradientPicker = true
                        } else {
                            colorMode = GRADIENT_MODE
                            scope.launch {
                                app.appSettings.setThemeColorIndex(CUSTOM_COLOR_INDEX)
                                if (themeGradientStartIdx < 0) {
                                    app.appSettings.setThemeGradient(0, 0, 0, 0, 0)
                                }
                            }
                        }
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = if (colorMode == GRADIENT_MODE) {
                        androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onBackground)
                    } else null,
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(
                            if (themeGradientStartIdx >= 0) {
                                val start = when {
                                    themeGradientStartIdx == CUSTOM_COLOR_INDEX && themeGradientStartArgb != 0 -> Color(themeGradientStartArgb)
                                    themeGradientStartIdx in EventColors.indices -> EventColors[themeGradientStartIdx]
                                    else -> GradientIconStart
                                }
                                val end = when {
                                    themeGradientEndIdx == CUSTOM_COLOR_INDEX && themeGradientEndArgb != 0 -> Color(themeGradientEndArgb)
                                    themeGradientEndIdx in EventColors.indices -> EventColors[themeGradientEndIdx]
                                    else -> GradientIconEnd
                                }
                                GradientDirection.createBrush(themeGradientDirection, listOf(start, end))
                            } else {
                                GradientDirection.createBrush(0, listOf(GradientIconStart, GradientIconEnd))
                            }
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

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PreviewCard(
    event: Event,
    cardOpacity: Float,
    onEditClick: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val primaryColor = when {
        event.colorPreset == CUSTOM_COLOR_INDEX && event.customColorArgb != 0 -> Color(event.customColorArgb)
        event.colorPreset in EventColors.indices -> EventColors[event.colorPreset]
        else -> MaterialTheme.colorScheme.primary
    }
    val secondaryColor = when {
        event.colorPresetEnd == CUSTOM_COLOR_INDEX && event.customColorEndArgb != 0 -> Color(event.customColorEndArgb)
        event.colorPresetEnd in EventColors.indices && event.colorPresetEnd >= 0 -> EventColors[event.colorPresetEnd]
        else -> primaryColor
    }
    val cardBrush = if (event.colorPresetEnd >= 0) {
        Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.08f), secondaryColor.copy(alpha = 0.08f)))
    } else {
        Brush.verticalGradient(listOf(primaryColor.copy(alpha = 0.08f), primaryColor.copy(alpha = 0.08f)))
    }
    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) 157.dp else 76.dp,
        animationSpec = tween(450),
        label = "cardHeight"
    )
    val barHeight by animateDpAsState(
        targetValue = if (isExpanded) 94.dp else 44.dp,
        animationSpec = tween(450),
        label = "barHeight"
    )
    val daysTextSize by animateFloatAsState(
        targetValue = if (isExpanded) 36f else 14f,
        animationSpec = tween(450),
        label = "daysSize"
    )

    val days = if (event.isCountUp) {
        com.daycountapp.util.DateUtil.getDaysPassed(event.targetDate)
    } else {
        val d = com.daycountapp.util.DateUtil.getDaysBetween(event.targetDate)
        if (d >= 0) d else -d
    }
    val daysLabel = if (event.isCountUp) "已过" else {
        val d = com.daycountapp.util.DateUtil.getDaysBetween(event.targetDate)
        if (d >= 0) "剩余" else "已过"
    }
    val dateTypeLabel = if (event.isCountUp) "正数日" else "倒数日"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = cardOpacity),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(cardBrush)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isExpanded = !isExpanded }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 左侧颜色条
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(primaryColor)
                )

                Spacer(Modifier.width(14.dp))

                // 内容区域
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(top = 12.dp, bottom = 12.dp, end = 4.dp)
                ) {
                    // 标题
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // 描述（展开时显示）
                    AnimatedVisibility(
                        visible = isExpanded && event.description.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(250)),
                        exit = fadeOut(animationSpec = tween(150)),
                    ) {
                        Text(
                            text = event.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 天数显示
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = daysLabel,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Text(
                            text = "$days",
                            fontSize = daysTextSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 2.sp,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Text(
                            text = "天",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.alignByBaseline(),
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 日期信息（展开时显示）
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(animationSpec = tween(250)),
                        exit = fadeOut(animationSpec = tween(150)),
                    ) {
                        Text(
                            text = "$dateTypeLabel · ${DateUtil.formatDateDisplay(event.targetDate)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }

                // 右侧编辑按钮（展开时显示）
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(250)),
                    exit = fadeOut(animationSpec = tween(150)),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onEditClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
                } else {
                    Modifier
                }
            ).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(description, style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
        trailing()
    }
}
