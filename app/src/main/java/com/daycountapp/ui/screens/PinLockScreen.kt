package com.daycountapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycountapp.data.PasswordManager
import com.daycountapp.ui.components.NumericKeypad
import com.daycountapp.util.VibrationManager

@Composable
fun PinLockScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "请输入密码",
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
                    color = if (index < input.length) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    },
                ) {}
            }
        }

        if (error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "密码错误",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
            )
        } else {
            Spacer(Modifier.height(28.dp))
        }

        // 数字键盘
        NumericKeypad(
            modifier = Modifier.fillMaxWidth(0.8f),
            onKeyClick = {
                if (input.length < 4) {
                    input += it
                    if (input.length == 4) {
                        if (PasswordManager.verify(input)) {
                            VibrationManager.vibrate(10L)
                            onSuccess()
                        } else {
                            VibrationManager.vibrate(50L)
                            error = true
                            input = ""
                        }
                    }
                }
            },
            onDeleteClick = {
                if (input.isNotEmpty()) {
                    input = input.dropLast(1)
                    error = false
                }
            },
            onConfirmClick = {
                if (input.length == 4) {
                    if (PasswordManager.verify(input)) {
                        VibrationManager.vibrate(10L)
                        onSuccess()
                    } else {
                        VibrationManager.vibrate(50L)
                        error = true
                        input = ""
                    }
                }
            },
        )
    }
}
