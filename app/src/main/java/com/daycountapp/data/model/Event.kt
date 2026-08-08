package com.daycountapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetDate: Long, // 目标日期时间戳
    val isCountUp: Boolean = false, // false=倒数，true=正数
    val isHidden: Boolean = false, // true=隐藏，不显示在主列表
    val category: String = "",
    val isPinned: Boolean = false,
    val colorPreset: Int = 0, // 事件主配色索引
    val colorPresetEnd: Int = -1, // 事件副配色索引（渐变用，-1表示纯色）
    val gradientDirection: Int = 0, // 渐变方向: 0=从左到右, 1=从上到下, 2=从左上到右下, 3=从左下到右上
    val backgroundUri: String? = null, // 事件背景图片URI
    val backgroundOpacity: Float = 0.3f, // 事件背景不透明度
    val customColorArgb: Int = 0, // 自定义颜色 ARGB 值（colorPreset==14 时使用）
    val customColorEndArgb: Int = 0, // 自定义副色 ARGB 值（secondaryIndex==14 时使用）
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false, // true=回收站
    val deleteTime: Long = 0L, // 删除时间，用于回收站排序
    val isPreview: Boolean = false, // true=预览卡片，不出现在主界面
    val sortOrder: Int = 0, // 排序顺序，用于拖拽排序
)
