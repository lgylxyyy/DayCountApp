package com.daycountapp.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 渐变方向枚举
 * id 值与 Event.gradientDirection 保持一致
 */
enum class GradientDirection(
    val id: Int,
    val label: String,
    val icon: String,
) {
    LEFT_TO_RIGHT(0, "从左到右", "→"),
    TOP_TO_BOTTOM(1, "从上到下", "↓"),
    TOP_LEFT_TO_BOTTOM_RIGHT(2, "从左上到右下", "↘"),
    BOTTOM_LEFT_TO_TOP_RIGHT(3, "从左下到右上", "↗"),
    ;

    companion object {
        fun fromId(id: Int): GradientDirection = entries.find { it.id == id } ?: LEFT_TO_RIGHT

        /** 根据方向创建渐变色 Brush */
        fun createBrush(
            direction: GradientDirection,
            colors: List<Color>,
        ): Brush {
            if (colors.size < 2) return Brush.horizontalGradient(colors)
            return when (direction) {
                LEFT_TO_RIGHT -> {
                    Brush.horizontalGradient(colors)
                }

                TOP_TO_BOTTOM -> {
                    Brush.verticalGradient(colors)
                }

                TOP_LEFT_TO_BOTTOM_RIGHT -> {
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset.Zero,
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                }

                BOTTOM_LEFT_TO_TOP_RIGHT -> {
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f),
                    )
                }
            }
        }

        fun createBrush(
            directionId: Int,
            colors: List<Color>,
        ): Brush = createBrush(fromId(directionId), colors)
    }
}
