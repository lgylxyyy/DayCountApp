package com.daycountapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daycountapp.DayCountApp
import com.daycountapp.data.DayFileSerializer
import com.daycountapp.data.model.Event
import com.daycountapp.ui.theme.DestructiveRed
import com.daycountapp.ui.theme.TextSecondary
import com.daycountapp.ui.viewmodel.EventViewModel
import com.daycountapp.util.VibrationManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHidden: () -> Unit,
    onNavigateToRecycleBin: () -> Unit = {},
    onNavigateToPasswordSetup: () -> Unit = {},
) {
    val app = DayCountApp.instance
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory(app.eventRepository))

    val darkMode by app.appSettings.darkMode.collectAsState(initial = null)
    val followSystemDarkMode by app.appSettings.followSystemDarkMode.collectAsState(initial = null)
    val animationEnabled by app.appSettings.animationEnabled.collectAsState(initial = null)
    val vibrationEnabled by app.appSettings.vibrationEnabled.collectAsState(initial = null)

    // 等待所有设置加载完成后再显示开关，避免闪烁
    if (darkMode == null || followSystemDarkMode == null || animationEnabled == null || vibrationEnabled == null) {
        return
    }

    // 加载完成，使用非空值
    val darkModeValue = darkMode!!
    val followSystemDarkModeValue = followSystemDarkMode!!
    val animationEnabledValue = animationEnabled!!
    val vibrationEnabledValue = vibrationEnabled!!
    val allEvents by viewModel.allEvents.collectAsState(initial = emptyList())

    // 同步震动开关到 VibrationManager
    LaunchedEffect(vibrationEnabledValue) {
        VibrationManager.updateEnabled(vibrationEnabledValue)
    }

    var showClearDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importedEvents by remember { mutableStateOf<List<Event>?>(null) }

    // 预见性返回
    BackHandler {
        VibrationManager.vibrate(10L)
        onNavigateBack()
    }

    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream"),
        ) { uri ->
            uri?.let {
                scope.launch {
                    val content = DayFileSerializer.serialize(allEvents)
                    context.contentResolver.openOutputStream(it)?.use { stream -> stream.write(content.toByteArray()) }
                    Toast.makeText(context, "已导出 ${allEvents.size} 个事件", Toast.LENGTH_SHORT).show()
                }
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let {
                scope.launch {
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@launch
                    val content = String(bytes)
                    val result = DayFileSerializer.deserialize(content)
                    result
                        .onSuccess { events ->
                            showImportDialog = true
                            importedEvents = events
                        }.onFailure { e ->
                            Toast.makeText(context, "文件格式错误: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }

    if (showImportDialog && importedEvents != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入 ${importedEvents?.size ?: 0} 个事件") },
            text = { Text("选择导入方式：覆盖将清除现有数据，追加将添加到列表末尾") },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        importedEvents?.let { viewModel.importOverwrite(it) }
                        showImportDialog = false
                    }) { Text("覆盖") }
                    TextButton(onClick = {
                        importedEvents?.let { viewModel.importAppend(it) }
                        showImportDialog = false
                    }) { Text("追加") }
                }
            },
            dismissButton = { TextButton(onClick = { showImportDialog = false }) { Text("取消") } },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            SettingsItem(
                title = "跟随系统",
                description = "深色模式跟随系统设置",
                trailing = {
                    Switch(
                        checked = followSystemDarkModeValue,
                        onCheckedChange = {
                            VibrationManager.vibrate(10L)
                            scope.launch {
                                app.appSettings.setFollowSystemDarkMode(it)
                                if (it) app.appSettings.setDarkMode(false)
                            }
                        },
                        colors = switchColors(),
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "深色模式",
                description = "强制使用深色模式，不受系统影响",
                trailing = {
                    Switch(
                        checked = darkModeValue,
                        onCheckedChange = {
                            VibrationManager.vibrate(10L)
                            scope.launch {
                                app.appSettings.setDarkMode(it)
                                if (it) app.appSettings.setFollowSystemDarkMode(false)
                            }
                        },
                        colors = switchColors(),
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "列表动画",
                description = "事件列表进入时的动画效果",
                trailing = {
                    Switch(
                        checked = animationEnabledValue,
                        onCheckedChange = {
                            VibrationManager.vibrate(10L)
                            scope.launch { app.appSettings.setAnimationEnabled(it) }
                        },
                        colors = switchColors(),
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "关闭震动",
                description = "关闭所有震动反馈",
                trailing = {
                    Switch(
                        checked = vibrationEnabledValue,
                        onCheckedChange = {
                            VibrationManager.vibrate(10L)
                            scope.launch { app.appSettings.setVibrationEnabled(it) }
                        },
                        colors = switchColors(),
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "回收站",
                description = "查看已删除的事件",
                trailing = {},
                onClick = {
                    VibrationManager.vibrate(10L)
                    onNavigateToRecycleBin()
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "隐私密码",
                description = "设置、更改或删除隐私密码",
                trailing = {},
                onClick = {
                    VibrationManager.vibrate(10L)
                    onNavigateToPasswordSetup()
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "导出事件",
                description = "将所有事件导出为.day文件",
                trailing = {},
                onClick = {
                    VibrationManager.vibrate(10L)
                    exportLauncher.launch("daycount_events.day")
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "导入事件",
                description = "从.day文件导入事件",
                trailing = {},
                onClick = {
                    VibrationManager.vibrate(10L)
                    importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))

            SettingsItem(
                title = "清除数据",
                description = "删除所有事件数据，此操作不可撤销",
                trailing = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DestructiveRed, modifier = Modifier.size(24.dp))
                },
                onClick = {
                    VibrationManager.vibrate(10L)
                    showClearDialog = true
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.3f))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确定清除数据") },
            text = { Text("这将删除所有事件，此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            app.eventRepository.allEvents.collect { events ->
                                events.forEach { app.eventRepository.deleteEvent(it) }
                            }
                        }
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed),
                ) { Text("确定清除") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } },
        )
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
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
                    } else {
                        Modifier
                    },
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

@Composable
private fun switchColors() =
    SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    )
