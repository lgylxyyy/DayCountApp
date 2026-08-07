package com.daycountapp.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daycountapp.DayCountApp
import com.daycountapp.data.PasswordManager
import com.daycountapp.data.PasswordType
import com.daycountapp.ui.components.NumericKeypad
import com.daycountapp.ui.components.PatternLockView
import com.daycountapp.util.VibrationManager

@Composable
fun PasswordSetupScreen(
    onNavigateBack: () -> Unit,
    onPasswordChanged: (PasswordType) -> Unit = {},
) {
    val app = DayCountApp.instance
    var passwordType by remember { mutableStateOf(PasswordManager.getType()) }
    var showPasswordInput by remember { mutableStateOf(false) }

    // 拦截返回键：子页面返回时回到密码类型选择，而非直接回设置页
    BackHandler(enabled = showPasswordInput) {
        showPasswordInput = false
    }

    val titleText = when {
        showPasswordInput && passwordType == PasswordType.PIN -> "设置数字密码"
        showPasswordInput && passwordType == PasswordType.PATTERN -> "设置图案密码"
        else -> "设置隐私密码"
    }

    // 顶部栏：返回按钮 + 标题（背景透明，让底层背景透过来）
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
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
                if (showPasswordInput) {
                    showPasswordInput = false  // 子页面返回到密码类型选择
                } else {
                    onNavigateBack()  // 主页面返回到设置
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }

    // 密码保存辅助函数
    fun savePassword(type: PasswordType, value: String) {
        PasswordManager.setPassword(type, value)
        PasswordManager.save(app)
        passwordType = type
        onPasswordChanged(type)
        showPasswordInput = false
    }

    if (showPasswordInput) {
        when (passwordType) {
            PasswordType.PIN -> PinSetupScreen(
                onNavigateBack = { showPasswordInput = false },
                onComplete = { pin -> savePassword(PasswordType.PIN, pin) },
            )
            PasswordType.PATTERN -> PatternSetupScreen(
                onNavigateBack = { showPasswordInput = false },
                onComplete = { pattern -> savePassword(PasswordType.PATTERN, pattern) },
            )
            PasswordType.FINGERPRINT -> {
                // 跳转到系统指纹设置
                try {
                    app.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (_: Exception) {}
                PasswordManager.setFingerprint()
                PasswordManager.save(app)
                passwordType = PasswordType.FINGERPRINT
                onPasswordChanged(PasswordType.FINGERPRINT)
                showPasswordInput = false
            }
            else -> {}
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "密码类型",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(24.dp))

        PasswordTypeOption(
            title = "无密码",
            description = "直接进入隐藏事件页面",
            selected = passwordType == PasswordType.NONE,
            onClick = {
                passwordType = PasswordType.NONE
                PasswordManager.clearPassword()
                PasswordManager.save(app)
                onPasswordChanged(PasswordType.NONE)
            },
        )

        Spacer(Modifier.height(8.dp))

        PasswordTypeOption(
            title = "数字密码",
            description = "使用 4 位数字密码",
            selected = passwordType == PasswordType.PIN,
            onClick = {
                passwordType = PasswordType.PIN
                showPasswordInput = true
            },
        )

        Spacer(Modifier.height(8.dp))

        PasswordTypeOption(
            title = "图案密码",
            description = "绘制解锁图案",
            selected = passwordType == PasswordType.PATTERN,
            onClick = {
                passwordType = PasswordType.PATTERN
                showPasswordInput = true
            },
        )

        Spacer(Modifier.height(8.dp))

        PasswordTypeOption(
            title = "指纹解锁",
            description = "使用系统指纹验证",
            selected = passwordType == PasswordType.FINGERPRINT,
            onClick = { showPasswordInput = true },
        )
    }
}

@Composable
private fun PasswordTypeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                VibrationManager.vibrate(10L)
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = { VibrationManager.vibrate(10L); onClick() })
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PinSetupScreen(
    onNavigateBack: () -> Unit,
    onComplete: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var confirmInput by remember { mutableStateOf("") }
    var isConfirmPhase by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (isConfirmPhase) "请再次输入密码" else "请输入密码",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // 密码输入圆点
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            repeat(4) { index ->
                Surface(
                    modifier = Modifier.size(14.dp),
                    shape = MaterialTheme.shapes.small,
                    color = if (index < (if (isConfirmPhase) confirmInput else input).length) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    },
                ) {}
            }
        }

        if (error) {
            Spacer(Modifier.height(8.dp))
            Text("密码不匹配", color = MaterialTheme.colorScheme.error)
        } else {
            Spacer(Modifier.height(28.dp))
        }

        // 数字键盘
        NumericKeypad(
            modifier = Modifier.fillMaxWidth(0.8f),
            onKeyClick = {
                val current = if (isConfirmPhase) confirmInput else input
                if (current.length < 4) {
                    if (isConfirmPhase) confirmInput += it else input += it
                }
            },
            onDeleteClick = {
                if (isConfirmPhase) {
                    if (confirmInput.isNotEmpty()) confirmInput = confirmInput.dropLast(1)
                } else {
                    if (input.isNotEmpty()) input = input.dropLast(1)
                }
            },
            onConfirmClick = {
                if (!isConfirmPhase) {
                    if (input.length == 4) {
                        isConfirmPhase = true
                        error = false
                    } else {
                        error = true
                    }
                } else {
                    if (confirmInput == input) {
                        VibrationManager.vibrate(10L)
                        onComplete(input)
                    } else {
                        VibrationManager.vibrate(50L)
                        error = true
                        input = ""
                        confirmInput = ""
                        isConfirmPhase = false
                    }
                }
            },
        )
    }
}

@Composable
private fun PatternSetupScreen(
    onNavigateBack: () -> Unit,
    onComplete: (String) -> Unit,
) {
    var firstPattern by remember { mutableStateOf("") }
    var isConfirmPhase by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (isConfirmPhase) "请再次绘制图案" else "请绘制解锁图案（至少4个点）",
            style = MaterialTheme.typography.bodyLarge,
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(40.dp))

        PatternLockView(
            modifier = Modifier.fillMaxWidth(0.8f),
            onPatternComplete = { pattern ->
                val patternStr = pattern.joinToString(",")
                if (!isConfirmPhase) {
                    if (pattern.size >= 4) {
                        firstPattern = patternStr
                        isConfirmPhase = true
                        error = false
                    } else {
                        error = true
                    }
                } else {
                    if (patternStr == firstPattern) {
                        VibrationManager.vibrate(10L)
                        onComplete(patternStr)
                    } else {
                        VibrationManager.vibrate(50L)
                        error = false
                        firstPattern = ""
                        isConfirmPhase = false
                    }
                }
            },
        )
    }
}
