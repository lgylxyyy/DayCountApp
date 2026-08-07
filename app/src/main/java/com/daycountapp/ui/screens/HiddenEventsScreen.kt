package com.daycountapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daycountapp.DayCountApp
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.ui.theme.GradientIconEnd
import com.daycountapp.ui.theme.GradientIconStart
import com.daycountapp.ui.viewmodel.EventViewModel
import com.daycountapp.util.VibrationManager

@Composable
fun HiddenEventsScreen(
    onNavigateBack: () -> Unit,
    onEditEvent: (Long) -> Unit,
) {
    val viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory(com.daycountapp.DayCountApp.instance.eventRepository))
    val hiddenEvents by viewModel.hiddenEvents.collectAsState(initial = emptyList())
    var expandedEventId by remember { mutableStateOf(-1L) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<com.daycountapp.data.model.Event?>(null) }

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
                        text = "隐藏事件",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                if (hiddenEvents.isNotEmpty()) {
                    Button(
                        onClick = {
                            VibrationManager.vibrate(10L)
                            viewModel.unhideAllEvents()
                        },
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
                                text = "取消隐藏全部",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        if (hiddenEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "暂无隐藏事件",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp),
            ) {
                itemsIndexed(hiddenEvents, key = { _, e -> e.id }) { _, event ->
                    val isExpanded = event.id == expandedEventId

                    com.daycountapp.ui.components.SwipeableEventCard(
                        event = event,
                        isExpanded = isExpanded,
                        onToggle = { expandedEventId = if (isExpanded) -1L else event.id },
                        onEdit = { onEditEvent(event.id) },
                        onDelete = {
                            eventToDelete = event
                            showDeleteDialog = true
                        },
                        onHide = {
                            viewModel.unhideEvent(event)
                            expandedEventId = -1L
                        },
                        variant = com.daycountapp.ui.components.CardVariant.RECYCLE_BIN,
                    )
                }
            }
        }
    }

    if (showDeleteDialog && eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除事件") },
            text = { Text("确定要删除「${eventToDelete!!.title}」吗？事件将移入回收站。") },
            confirmButton = {
                Button(
                    onClick = {
                        eventToDelete?.let { viewModel.deleteEvent(it) }
                        showDeleteDialog = false
                        eventToDelete = null
                        expandedEventId = -1L
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
        )
    }
}
