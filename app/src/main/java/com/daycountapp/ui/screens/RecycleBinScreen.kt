package com.daycountapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daycountapp.DayCountApp
import com.daycountapp.data.model.Event
import com.daycountapp.ui.components.CardVariant
import com.daycountapp.ui.components.SwipeableEventCard
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.ui.theme.GradientIconEnd
import com.daycountapp.ui.theme.GradientIconStart
import com.daycountapp.ui.viewmodel.EventViewModel
import com.daycountapp.util.VibrationManager

@Composable
fun RecycleBinScreen(onNavigateBack: () -> Unit) {
    val viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory(com.daycountapp.DayCountApp.instance.eventRepository))
    val deletedEvents by viewModel.deletedEvents.collectAsState(initial = emptyList())
    var expandedEventId by remember { mutableStateOf(-1L) }
    var showPermanentDeleteDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }
    var sortOrder by remember { mutableIntStateOf(0) }  // 0=删除时间，1=更新时间

    // 根据排序方式排序
    val sortedEvents = remember(deletedEvents, sortOrder) {
        when (sortOrder) {
            0 -> deletedEvents.sortedByDescending { it.deleteTime }  // 最新删除的在前
            1 -> deletedEvents.sortedByDescending { it.updateTime }  // 最近更新的在前
            else -> deletedEvents
        }
    }

    val app = DayCountApp.instance
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

    // 预见性返回
    BackHandler {
        VibrationManager.vibrate(10L)
        onNavigateBack()
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
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        VibrationManager.vibrate(10L)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "回收站",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                if (deletedEvents.isNotEmpty()) {
                    Button(
                        onClick = { /* TODO: clear all */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .background(
                                        brush = gradientBrush ?: SolidColor(MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(22.dp),
                                    ).padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "清空回收站",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        if (deletedEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "回收站为空",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // 排序按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val sortOptions = listOf("按删除时间", "按更新时间")
                sortOptions.forEachIndexed { index, label ->
                    val isSelected = sortOrder == index
                    val bgModifier =
                        if (isSelected && gradientBrush != null) {
                            Modifier.background(brush = gradientBrush, shape = RoundedCornerShape(22.dp))
                        } else {
                            Modifier.background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(22.dp),
                            )
                        }
                    Surface(
                        modifier = Modifier
                            .then(bgModifier)
                            .clickable {
                                VibrationManager.vibrate(10L)
                                sortOrder = index
                            },
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        tonalElevation = if (isSelected) 4.dp else 0.dp,
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp),
            ) {
                items(sortedEvents, key = { it.id }) { event ->
                    val isExpanded = expandedEventId == event.id

                    SwipeableEventCard(
                        event = event,
                        isExpanded = isExpanded,
                        onToggle = { expandedEventId = if (isExpanded) -1L else event.id },
                        onEdit = {},
                        onDelete = {
                            eventToDelete = event
                            showPermanentDeleteDialog = true
                        },
                        onHide = {
                            viewModel.restoreEvent(event)
                            expandedEventId = -1L
                        },
                        variant = CardVariant.RECYCLE_BIN,
                    )
                }
            }
        }
    }

    if (showPermanentDeleteDialog && eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { showPermanentDeleteDialog = false },
            title = { Text("永久删除") },
            text = { Text("确定要永久删除「${eventToDelete!!.title}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        eventToDelete?.let { viewModel.permanentDeleteEvent(it) }
                        showPermanentDeleteDialog = false
                        eventToDelete = null
                        expandedEventId = -1L
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showPermanentDeleteDialog = false }) { Text("取消") }
            },
        )
    }
}
