package com.daycountapp.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import com.daycountapp.DayCountApp
import com.daycountapp.util.VibrationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(
    imageUri: Uri,
    onNavigateBack: () -> Unit,
    onCropComplete: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = DayCountApp.instance

    // 屏幕比例
    val density = LocalDensity.current
    var screenSize by remember { mutableStateOf(IntSize(0, 0)) }
    val screenAspectRatio = if (screenSize.height > 0) {
        screenSize.width.toFloat() / screenSize.height.toFloat()
    } else {
        0.5f // 默认竖屏比例
    }

    // 图片状态
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageOffsetX by remember { mutableFloatStateOf(0f) }
    var imageOffsetY by remember { mutableFloatStateOf(0f) }
    var imageScale by remember { mutableFloatStateOf(1f) }
    var isLoading by remember { mutableStateOf(true) }

    // 加载图片
    LaunchedEffect(imageUri) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val input = context.contentResolver.openInputStream(imageUri)
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val loadedBitmap = BitmapFactory.decodeStream(input, null, options)
                input?.close()
                bitmap = loadedBitmap
            } catch (_: Exception) {
                // 加载失败
            }
        }
        isLoading = false
    }

    // 裁剪并保存
    fun cropAndSave() {
        val currentBitmap = bitmap ?: return
        scope.launch {
            val croppedUri = withContext(Dispatchers.IO) {
                try {
                    // 计算裁剪区域（居中裁剪，按屏幕比例）
                    val cropWidth = currentBitmap.width
                    val cropHeight = (cropWidth / screenAspectRatio).toInt().coerceAtMost(currentBitmap.height)

                    val offsetY = ((currentBitmap.height - cropHeight) / 2).coerceAtLeast(0)

                    val croppedBitmap = Bitmap.createBitmap(
                        currentBitmap,
                        0,
                        offsetY,
                        cropWidth,
                        cropHeight
                    )

                    // 保存到内部存储
                    val backgroundsDir = File(context.filesDir, "backgrounds")
                    if (!backgroundsDir.exists()) {
                        backgroundsDir.mkdirs()
                    }
                    val file = File(backgroundsDir, "bg_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { out ->
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }

                    // 获取 content URI
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (_: Exception) {
                    null
                }
            }

            if (croppedUri != null) {
                // 添加到历史
                app.appSettings.addBackgroundToHistory(croppedUri.toString())
                // 设为当前背景
                app.appSettings.setActiveBackground(croppedUri.toString())
                onCropComplete(croppedUri)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 获取屏幕尺寸
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    screenSize = coordinates.size
                }
        )

        // 图片显示区域
        bitmap?.let { bmp ->
            // 计算图片显示尺寸（保持比例，填充裁剪区域）
            val cropAreaWidth = screenSize.width.toFloat()
            val cropAreaHeight = cropAreaWidth / screenAspectRatio

            val imageAspect = bmp.width.toFloat() / bmp.height.toFloat()
            val displayWidth: Float
            val displayHeight: Float

            if (imageAspect > screenAspectRatio) {
                // 图片更宽，以高度为准
                displayHeight = cropAreaHeight
                displayWidth = displayHeight * imageAspect
            } else {
                // 图片更高，以宽度为准
                displayWidth = cropAreaWidth
                displayHeight = displayWidth / imageAspect
            }

            val minScale = min(cropAreaWidth / displayWidth, cropAreaHeight / displayHeight)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (imageScale * zoom).coerceIn(minScale, 5f)
                            imageScale = newScale
                            imageOffsetX += pan.x
                            imageOffsetY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "裁剪图片",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            translationX = imageOffsetX
                            translationY = imageOffsetY
                        }
                )
            }

            // 裁剪框遮罩（半透明黑色区域）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {})
        }

        // 加载中
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                VibrationManager.vibrate(10L)
                onNavigateBack()
            }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "裁剪背景",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                VibrationManager.vibrate(10L)
                cropAndSave()
            }) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "确认",
                    tint = Color.White
                )
            }
        }

        // 底部提示
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "拖动图片调整位置",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
