package com.daycountapp.util

import android.graphics.RenderEffect
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 对指定View应用高斯模糊效果
 * 仅在Android 12 (API 31) 及以上版本生效，低版本不做处理
 *
 * @param radius 模糊半径（像素值）
 */
fun View.applyBlurEffect(radius: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val blurEffect = RenderEffect.createBlurEffect(
            radius.toFloat(),
            radius.toFloat(),
            android.graphics.Shader.TileMode.CLAMP
        )
        setRenderEffect(blurEffect)
    }
}

/**
 * 清除View上的模糊效果
 */
fun View.clearBlurEffect() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setRenderEffect(null)
    }
}

/**
 * 获取状态栏高度
 */
fun getStatusBarHeight(view: View): Int {
    val windowInsets = ViewCompat.getRootWindowInsets(view)
    return windowInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
}

/**
 * 获取导航栏高度
 */
fun getNavigationBarHeight(view: View): Int {
    val windowInsets = ViewCompat.getRootWindowInsets(view)
    return windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
}
