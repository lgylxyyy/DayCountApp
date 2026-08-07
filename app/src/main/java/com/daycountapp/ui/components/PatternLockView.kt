package com.daycountapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.sqrt

@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    onPatternComplete: (List<Int>) -> Unit,
    onPatternChanged: (List<Int>) -> Unit = {},
) {
    var selectedPoints by remember { mutableStateOf(listOf<Int>()) }
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    var isDrawing by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val point = getNearestPoint(offset, size.width.toFloat())
                        if (point != -1 && point !in selectedPoints) {
                            selectedPoints = listOf(point)
                            isDrawing = true
                            currentPosition = offset
                            onPatternChanged(selectedPoints)
                        }
                    },
                    onDrag = { change, _ ->
                        currentPosition = change.position
                        val point = getNearestPoint(change.position, size.width.toFloat())
                        if (point != -1 && point !in selectedPoints) {
                            selectedPoints = selectedPoints + point
                            onPatternChanged(selectedPoints)
                        }
                    },
                    onDragEnd = {
                        isDrawing = false
                        if (selectedPoints.size >= 4) onPatternComplete(selectedPoints)
                        selectedPoints = listOf()
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / 3
            val radius = cellSize * 0.08f
            val dotPositions = (0..8).map { i ->
                Offset(
                    (i % 3) * cellSize + cellSize / 2,
                    (i / 3) * cellSize + cellSize / 2,
                )
            }

            // 绘制连线（副色，透明度较高）
            if (selectedPoints.size > 1) {
                for (i in 0 until selectedPoints.size - 1) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.5f),
                        start = dotPositions[selectedPoints[i]],
                        end = dotPositions[selectedPoints[i + 1]],
                        strokeWidth = 8f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            // 当前拖拽线（副色，更加淡化）
            if (isDrawing && selectedPoints.isNotEmpty()) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.25f),
                    start = dotPositions[selectedPoints.last()],
                    end = currentPosition,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round,
                )
            }

            // 绘制点
            for (i in 0..8) {
                val center = dotPositions[i]
                val isSelected = i in selectedPoints
                drawCircle(
                    color = if (isSelected) primaryColor else secondaryColor.copy(alpha = 0.25f),
                    radius = if (isSelected) radius * 1.5f else radius,
                    center = center,
                )
            }
        }
    }
}

private fun getNearestPoint(position: Offset, size: Float): Int {
    val cellSize = size / 3
    val threshold = cellSize * 0.4f
    for (i in 0..8) {
        val cx = (i % 3) * cellSize + cellSize / 2
        val cy = (i / 3) * cellSize + cellSize / 2
        val dist = sqrt((position.x - cx) * (position.x - cx) + (position.y - cy) * (position.y - cy))
        if (dist < threshold) return i
    }
    return -1
}