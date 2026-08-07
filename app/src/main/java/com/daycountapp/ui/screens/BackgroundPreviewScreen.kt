package com.daycountapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.daycountapp.DayCountApp
import com.daycountapp.util.VibrationManager

@Composable
fun BackgroundPreviewScreen(
    imageUri: String,
    onNavigateBack: () -> Unit,
) {
    val app = DayCountApp.instance
    val backgroundOpacity by app.appSettings.backgroundOpacity.collectAsState(initial = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    VibrationManager.vibrate(10L)
                    onNavigateBack()
                }
            }
    ) {
        // 图片（带透明度）
        Image(
            painter = rememberAsyncImagePainter(model = imageUri),
            contentDescription = "背景预览",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundOpacity)
        )

        // 返回按钮
        IconButton(
            onClick = {
                VibrationManager.vibrate(10L)
                onNavigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }

        // 底部提示
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "点击任意位置返回",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
