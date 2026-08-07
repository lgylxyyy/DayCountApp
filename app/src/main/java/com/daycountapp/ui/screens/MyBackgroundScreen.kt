package com.daycountapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.daycountapp.DayCountApp
import com.daycountapp.util.VibrationManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBackgroundScreen(
    onNavigateBack: () -> Unit,
    onPreviewImage: (String) -> Unit,
) {
    val app = DayCountApp.instance
    val scope = rememberCoroutineScope()

    // 读取数据
    val presetBackgrounds = app.appSettings.presetBackgrounds
    val historyBackgrounds by app.appSettings.backgroundHistory.collectAsState(initial = emptyList())
    val activeBackground by app.appSettings.activeBackground.collectAsState(initial = "")

    // 选中的图片
    var selectedUri by remember { mutableStateOf<String?>(null) }

    // 删除确认对话框
    var showDeleteDialog by remember { mutableStateOf(false) }
    var uriToDelete by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog && uriToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                uriToDelete = null
            },
            title = { Text("删除背景") },
            text = { Text("确定要删除这个背景吗？") },
            confirmButton = {
                TextButton(onClick = {
                    VibrationManager.vibrate(10L)
                    scope.launch {
                        app.appSettings.removeBackgroundFromHistory(uriToDelete!!)
                    }
                    showDeleteDialog = false
                    uriToDelete = null
                    selectedUri = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    uriToDelete = null
                }) {
                    Text("取消")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 顶部栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    VibrationManager.vibrate(10L)
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
                Text(
                    text = "我的背景",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        // 内容区域
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 预设背景区域
            if (presetBackgrounds.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "预设背景",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                items(presetBackgrounds) { uri ->
                    BackgroundGridItem(
                        uri = uri,
                        isActive = uri == activeBackground,
                        isSelected = uri == selectedUri,
                        onClick = {
                            VibrationManager.vibrate(10L)
                            selectedUri = if (selectedUri == uri) null else uri
                        },
                        onDoubleClick = {
                            VibrationManager.vibrate(10L)
                            onPreviewImage(uri)
                        },
                        onLongPress = {
                            VibrationManager.vibrate(50L)
                            selectedUri = uri
                        },
                        onSetCurrent = {
                            VibrationManager.vibrate(10L)
                            scope.launch {
                                app.appSettings.setActiveBackground(uri)
                            }
                            selectedUri = null
                        },
                        onPinToTop = {
                            VibrationManager.vibrate(10L)
                            scope.launch {
                                app.appSettings.moveBackgroundToTop(uri)
                            }
                            selectedUri = null
                        },
                        onDelete = {
                            VibrationManager.vibrate(10L)
                            uriToDelete = uri
                            showDeleteDialog = true
                        },
                    )
                }
            }

            // 历史背景区域
            if (historyBackgrounds.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "历史背景",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                items(historyBackgrounds) { uri ->
                    BackgroundGridItem(
                        uri = uri,
                        isActive = uri == activeBackground,
                        isSelected = uri == selectedUri,
                        onClick = {
                            VibrationManager.vibrate(10L)
                            selectedUri = if (selectedUri == uri) null else uri
                        },
                        onDoubleClick = {
                            VibrationManager.vibrate(10L)
                            onPreviewImage(uri)
                        },
                        onLongPress = {
                            VibrationManager.vibrate(50L)
                            selectedUri = uri
                        },
                        onSetCurrent = {
                            VibrationManager.vibrate(10L)
                            scope.launch {
                                app.appSettings.setActiveBackground(uri)
                            }
                            selectedUri = null
                        },
                        onPinToTop = {
                            VibrationManager.vibrate(10L)
                            scope.launch {
                                app.appSettings.moveBackgroundToTop(uri)
                            }
                            selectedUri = null
                        },
                        onDelete = {
                            VibrationManager.vibrate(10L)
                            uriToDelete = uri
                            showDeleteDialog = true
                        },
                    )
                }
            }

            // 空状态
            if (presetBackgrounds.isEmpty() && historyBackgrounds.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无背景\n点击\"选择新背景\"添加",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundGridItem(
    uri: String,
    isActive: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongPress: () -> Unit,
    onSetCurrent: () -> Unit,
    onPinToTop: () -> Unit,
    onDelete: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "borderColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // 图片 - 使用 pointerInput 处理所有手势
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onDoubleTap = { onDoubleClick() },
                        onLongPress = { onLongPress() }
                    )
                }
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = "背景图片",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 当前使用标记
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "当前使用",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 操作按钮（选中时悬浮在图片底部）
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = onPinToTop,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "置顶",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onSetCurrent,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "设为当前使用",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
