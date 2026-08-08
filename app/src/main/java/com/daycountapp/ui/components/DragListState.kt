package com.daycountapp.ui.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 拖拽列表状态管理类
 * 用于跟踪拖拽过程中的状态
 */
@Stable
class DragListState {
    /** 被拖拽的项目索引 */
    var draggedIndex by mutableStateOf<Int?>(null)

    /** 拖拽偏移量（像素） */
    var dragOffset by mutableFloatStateOf(0f)

    /** 是否正在拖拽 */
    var isDragging by mutableStateOf(false)
        private set

    /** 自动滚动速度（像素/帧） */
    var autoScrollSpeed by mutableFloatStateOf(0f)

    /**
     * 开始拖拽
     * @param index 被拖拽项目的索引
     */
    fun onDragStart(index: Int) {
        draggedIndex = index
        isDragging = true
        dragOffset = 0f
    }

    /**
     * 更新拖拽偏移量
     * @param offset Y轴偏移量增量
     */
    fun onDragOffset(offset: Float) {
        dragOffset += offset
    }

    /**
     * 结束拖拽
     */
    fun onDragEnd() {
        isDragging = false
        draggedIndex = null
        dragOffset = 0f
        autoScrollSpeed = 0f
    }
}
