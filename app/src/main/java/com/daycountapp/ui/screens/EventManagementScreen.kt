package com.daycountapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.daycountapp.DayCountApp
import com.daycountapp.data.model.Event
import com.daycountapp.ui.components.DragListState
import com.daycountapp.ui.components.SwipeableEventCard
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.DestructiveRed
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.ui.theme.GradientIconEnd
import com.daycountapp.ui.theme.GradientIconStart
import com.daycountapp.ui.theme.TextSecondary
import com.daycountapp.ui.viewmodel.EventViewModel
import com.daycountapp.util.VibrationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val CARD_HEIGHT_DP = 76
private const val CARD_GAP_DP = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventManagementScreen(
    viewModel: EventViewModel,
    onNavigateToForm: () -> Unit,
    onEventClick: (Long) -> Unit,
    onNavigateToHidden: () -> Unit,
) {
    val allEvents by viewModel.allEvents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }
    var filterMode by remember { mutableIntStateOf(0) }
    var expandedEventId by remember { mutableStateOf(-1L) }
    var expandedLongPressId by remember { mutableStateOf(-1L) }

    val app = DayCountApp.instance
    val scope = rememberCoroutineScope()

    // 震动开关同步
    val vibrationEnabled by app.appSettings.vibrationEnabled.collectAsState(initial = true)
    LaunchedEffect(vibrationEnabled) {
        VibrationManager.updateEnabled(vibrationEnabled)
    }

    val themeGradientStartIdx by app.appSettings.themeGradientStartIndex.collectAsState(initial = -1)
    val themeGradientEndIdx by app.appSettings.themeGradientEndIndex.collectAsState(initial = -1)
    val themeGradientStartArgb by app.appSettings.themeGradientStartArgb.collectAsState(initial = 0)
    val themeGradientEndArgb by app.appSettings.themeGradientEndArgb.collectAsState(initial = 0)
    val themeGradientDirection by app.appSettings.themeGradientDirection.collectAsState(initial = 0)
    val isGradientTheme = themeGradientStartIdx >= 0
    val gradientBrush =
        if (isGradientTheme) {
            val start =
                when {
                    themeGradientStartIdx == CUSTOM_COLOR_INDEX && themeGradientStartArgb != 0 -> Color(themeGradientStartArgb)
                    themeGradientStartIdx in EventColors.indices -> EventColors[themeGradientStartIdx]
                    else -> GradientIconStart
                }
            val end =
                when {
                    themeGradientEndIdx == CUSTOM_COLOR_INDEX && themeGradientEndArgb != 0 -> Color(themeGradientEndArgb)
                    themeGradientEndIdx in EventColors.indices -> EventColors[themeGradientEndIdx]
                    else -> GradientIconEnd
                }
            GradientDirection.createBrush(themeGradientDirection, listOf(start, end))
        } else {
            null
        }

    val displayEvents = remember { mutableStateListOf<Event>() }
    val listState = rememberLazyListState()
    val dragState = remember { DragListState() }
    val density = LocalDensity.current

    LaunchedEffect(allEvents, filterMode, searchQuery) {
        val filtered =
            when (filterMode) {
                1 -> allEvents.filter { !it.isCountUp }
                2 -> allEvents.filter { it.isCountUp }
                else -> allEvents
            }
        val result =
            if (searchQuery.isBlank()) {
                filtered
            } else {
                filtered.filter {
                    it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                }
            }
        displayEvents.clear()
        displayEvents.addAll(result)
    }

    // 自动滚动：当拖拽到边缘时自动滚动列表
    LaunchedEffect(dragState.autoScrollSpeed) {
        if (dragState.autoScrollSpeed != 0f && dragState.isDragging) {
            val scrollDirection = if (dragState.autoScrollSpeed > 0) 1 else -1
            while (isActive) {
                val firstVisible = listState.firstVisibleItemIndex
                val targetIndex = (firstVisible + scrollDirection).coerceIn(0, displayEvents.size - 1)
                listState.animateScrollToItem(targetIndex)
                delay(100)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 过滤按钮
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val filters = listOf("全部", "倒数日", "正数日")
                filters.forEachIndexed { index, label ->
                    val isSelected = filterMode == index
                    val bgModifier =
                        if (isSelected && gradientBrush != null) {
                            Modifier.background(brush = gradientBrush, shape = RoundedCornerShape(22.dp))
                        } else {
                            Modifier.background(
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                            .copy(
                                                alpha = 0.5f,
                                            )
                                    },
                                shape = RoundedCornerShape(22.dp),
                            )
                        }
                    Surface(
                        modifier =
                            Modifier.weight(1f).then(bgModifier).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                VibrationManager.vibrate(10L)
                                filterMode = index
                            },
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        tonalElevation = if (isSelected) 4.dp else 0.dp,
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索事件...", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)),
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "清除",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            )

            if (displayEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "未找到匹配的事件" else "还没有事件",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )
                        if (searchQuery.isBlank()) {
                            Text(
                                text = "点击右下角 + 按钮新建事件",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(displayEvents.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    // 计算起始索引
                                    val itemHeight = with(density) {
                                        (CARD_HEIGHT_DP + CARD_GAP_DP).dp.toPx()
                                    }
                                    val index = (offset.y / itemHeight).toInt()
                                    if (index in displayEvents.indices) {
                                        dragState.onDragStart(index)
                                        VibrationManager.vibrate(20L)  // 震动反馈：进入拖拽模式
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragState.onDragOffset(dragAmount.y)

                                    // 自动滚动检测
                                    val listHeight = size.height.toFloat()
                                    val edgeThreshold = 100f
                                    when {
                                        dragState.dragOffset < -listHeight + edgeThreshold -> {
                                            dragState.autoScrollSpeed = -20f
                                        }
                                        dragState.dragOffset > listHeight - edgeThreshold -> {
                                            dragState.autoScrollSpeed = 20f
                                        }
                                        else -> dragState.autoScrollSpeed = 0f
                                    }

                                    // 计算目标位置并交换
                                    dragState.draggedIndex?.let { fromIndex ->
                                        val itemHeight = with(density) {
                                            (CARD_HEIGHT_DP + CARD_GAP_DP).dp.toPx()
                                        }
                                        val targetIndex = ((fromIndex * itemHeight + dragState.dragOffset) / itemHeight)
                                            .toInt()
                                            .coerceIn(0, displayEvents.size - 1)

                                        if (targetIndex != fromIndex) {
                                            viewModel.reorderEvents(fromIndex, targetIndex, displayEvents)
                                            dragState.draggedIndex = targetIndex
                                            dragState.dragOffset = 0f
                                            VibrationManager.vibrate(10L)  // 震动反馈：卡片交换
                                        }
                                    }
                                },
                                onDragEnd = {
                                    dragState.onDragEnd()
                                },
                            )
                        },
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                ) {
                    itemsIndexed(displayEvents, key = { _, event -> event.id }) { index, event ->
                        val isExpanded = expandedEventId == event.id
                        val isLongPressMode = expandedLongPressId == event.id
                        val isDragging = dragState.draggedIndex == index && dragState.isDragging

                        val cardClickHandler: () -> Unit = {
                            if (isLongPressMode) {
                                expandedLongPressId = -1L
                            } else {
                                expandedEventId = if (isExpanded) -1L else event.id
                                expandedLongPressId = -1L
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(0, if (isDragging) dragState.dragOffset.toInt() else 0) }
                                .graphicsLayer {
                                    scaleX = if (isDragging) 1.05f else 1f
                                    scaleY = if (isDragging) 1.05f else 1f
                                    alpha = if (isDragging) 0.9f else 1f
                                    shadowElevation = if (isDragging) 16f else 0f
                                }
                                .animateItem()
                        ) {
                            SwipeableEventCard(
                                event = event,
                                isExpanded = isExpanded,
                                onToggle = cardClickHandler,
                                onEdit = { onEventClick(event.id) },
                                onDelete = {
                                    eventToDelete = event
                                    showDeleteDialog = true
                                },
                                onHide = { viewModel.hideEvent(event) },
                                onLongPress = {
                                    VibrationManager.vibrate(50L)
                                    expandedLongPressId = event.id
                                },
                                showHideButton = isLongPressMode,
                                animationDelay = 0,
                            )
                        }
                    }
                }
            }
        }

        // FAB
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(64.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(brush = gradientBrush ?: SolidColor(MaterialTheme.colorScheme.primary))
                    .clickable {
                        VibrationManager.vibrate(10L)
                        onNavigateToForm()
                    },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    if (showDeleteDialog && eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确定删除") },
            text = { Text("确定要删除「${eventToDelete!!.title}」吗？事件将移入回收站。") },
            confirmButton = {
                Button(
                    onClick = {
                        eventToDelete?.let { e: Event -> viewModel.softDeleteEvent(e) }
                        showDeleteDialog = false
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed),
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } },
        )
    }
}
