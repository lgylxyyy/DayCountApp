package com.daycountapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycountapp.DayCountApp
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.util.VibrationManager

@Composable
fun NumericKeypad(
    modifier: Modifier = Modifier,
    onKeyClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    val app = DayCountApp.instance
    val themeGradientStartArgb by app.appSettings.themeGradientStartArgb.collectAsState(initial = 0)
    val themeGradientEndArgb by app.appSettings.themeGradientEndArgb.collectAsState(initial = 0)
    val themeGradientDirection by app.appSettings.themeGradientDirection.collectAsState(initial = 0)

    val primaryColor = MaterialTheme.colorScheme.primary

    // 按钮背景渐变
    val buttonBackground: Brush = if (themeGradientStartArgb != 0 && themeGradientEndArgb != 0) {
        val startColor = Color(themeGradientStartArgb)
        val endColor = Color(themeGradientEndArgb)
        val direction = GradientDirection.fromId(themeGradientDirection)
        GradientDirection.createBrush(direction, listOf(startColor, endColor))
    } else {
        Brush.verticalGradient(
            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (key in listOf("1", "2", "3")) {
                KeyButton(key, buttonBackground) {
                    VibrationManager.vibrate(10L)
                    onKeyClick(key)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (key in listOf("4", "5", "6")) {
                KeyButton(key, buttonBackground) {
                    VibrationManager.vibrate(10L)
                    onKeyClick(key)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (key in listOf("7", "8", "9")) {
                KeyButton(key, buttonBackground) {
                    VibrationManager.vibrate(10L)
                    onKeyClick(key)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DeleteButton(buttonBackground) {
                VibrationManager.vibrate(10L)
                onDeleteClick()
            }
            KeyButton("0", buttonBackground) {
                VibrationManager.vibrate(10L)
                onKeyClick("0")
            }
            ConfirmButton(buttonBackground) {
                VibrationManager.vibrate(10L)
                onConfirmClick()
            }
        }
    }
}

/**
 * 数字按钮 - 纯白文字，背景有按下动画
 */
@Composable
private fun KeyButton(
    key: String,
    backgroundBrush: Brush,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "pressAlpha"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .pointerInput(key) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // 背景层 - 有按下动画
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(pressAlpha)
                .background(backgroundBrush)
        )
        // 文字层 - 始终纯白，不受 alpha 影响
        Text(
            key,
            fontSize = 32.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}

/**
 * 删除按钮 - 半透明背景，纯白图标
 */
@Composable
private fun DeleteButton(
    backgroundBrush: Brush,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "deletePressAlpha"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // 背景层 - 半透明区分于数字键
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(pressAlpha * 0.3f)  // 30%透明度
                .background(backgroundBrush)
        )
        // 图标层 - 始终纯白
        Icon(
            Icons.AutoMirrored.Filled.Backspace,
            "删除",
            modifier = Modifier.size(26.dp),
            tint = Color.White
        )
    }
}

/**
 * 确认按钮 - 纯白箭头图标
 */
@Composable
private fun ConfirmButton(
    backgroundBrush: Brush,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "confirmPressAlpha"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // 背景层
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(pressAlpha)
                .background(backgroundBrush)
        )
        // 图标层 - 始终纯白
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            "确认",
            modifier = Modifier.size(28.dp),
            tint = Color.White
        )
    }
}
