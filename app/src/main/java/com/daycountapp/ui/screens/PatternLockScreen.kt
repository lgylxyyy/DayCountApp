package com.daycountapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daycountapp.data.PasswordManager
import com.daycountapp.ui.components.PatternLockView
import com.daycountapp.util.VibrationManager

@Composable
fun PatternLockScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    var error by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (error) "图案错误，请重试" else "请绘制解锁图案",
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(40.dp))

        PatternLockView(
            modifier = Modifier.fillMaxWidth(0.8f),
            onPatternComplete = { pattern ->
                val patternStr = pattern.joinToString(",")
                if (PasswordManager.verify(patternStr)) {
                    VibrationManager.vibrate(10L)
                    onSuccess()
                } else {
                    VibrationManager.vibrate(50L)
                    error = true
                }
            },
            onPatternChanged = { error = false },
        )
    }
}
