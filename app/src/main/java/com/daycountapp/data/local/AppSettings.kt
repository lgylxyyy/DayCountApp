package com.daycountapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppSettings(
    private val context: Context,
) {
    companion object {
        // === 事件配色（独立存储） ===
        val EVENT_COLOR_INDEX = intPreferencesKey("event_color_index")
        val EVENT_CUSTOM_COLOR_ARGB = intPreferencesKey("event_custom_color_argb")
        val EVENT_GRADIENT_START_INDEX = intPreferencesKey("event_gradient_start_index")
        val EVENT_GRADIENT_END_INDEX = intPreferencesKey("event_gradient_end_index")
        val EVENT_GRADIENT_START_ARGB = intPreferencesKey("event_gradient_start_argb")
        val EVENT_GRADIENT_END_ARGB = intPreferencesKey("event_gradient_end_argb")
        val EVENT_GRADIENT_DIRECTION = intPreferencesKey("event_gradient_direction")

        // === 主题配色（独立存储） ===
        val THEME_COLOR_INDEX = intPreferencesKey("theme_color_index")
        val THEME_CUSTOM_COLOR_ARGB = intPreferencesKey("theme_custom_color_argb")
        val THEME_GRADIENT_START_INDEX = intPreferencesKey("theme_gradient_start_index")
        val THEME_GRADIENT_END_INDEX = intPreferencesKey("theme_gradient_end_index")
        val THEME_GRADIENT_START_ARGB = intPreferencesKey("theme_gradient_start_argb")
        val THEME_GRADIENT_END_ARGB = intPreferencesKey("theme_gradient_end_argb")
        val THEME_GRADIENT_DIRECTION = intPreferencesKey("theme_gradient_direction")

        // === 其他设置 ===
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FOLLOW_SYSTEM_DARK_MODE = booleanPreferencesKey("follow_system_dark_mode")
        val BACKGROUND_URI = stringPreferencesKey("background_uri")
        val BACKGROUND_OPACITY = floatPreferencesKey("background_opacity")
        val BACKGROUND_BLUR = floatPreferencesKey("background_blur")
        val BAR_OPACITY = floatPreferencesKey("bar_opacity")
        val BACKGROUND_HISTORY = stringPreferencesKey("background_history")
        val ACTIVE_BACKGROUND = stringPreferencesKey("active_background")
        val APP_BACKGROUND_ENABLED = booleanPreferencesKey("app_background_enabled")
        val DARK_MODE_BACKGROUND = booleanPreferencesKey("dark_mode_background")
        val CARD_OPACITY = floatPreferencesKey("card_opacity")
        val USE_PREVIEW_AS_DEFAULT = booleanPreferencesKey("use_preview_as_default")
        val WIDGET_TRANSPARENCY = floatPreferencesKey("widget_transparency")
        val WIDGET_THEME_COLOR = intPreferencesKey("widget_theme_color")
        val WIDGET_SIZE = stringPreferencesKey("widget_size")
        val ANIMATION_ENABLED = booleanPreferencesKey("animation_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    // ==================== 事件配色 ====================
    val eventColorIndex: Flow<Int> = context.settingsDataStore.data.map { it[EVENT_COLOR_INDEX] ?: 0 }

    suspend fun setEventColorIndex(v: Int) {
        context.settingsDataStore.edit { it[EVENT_COLOR_INDEX] = v }
    }

    val eventCustomColorArgb: Flow<Int> = context.settingsDataStore.data.map { it[EVENT_CUSTOM_COLOR_ARGB] ?: 0 }

    suspend fun setEventCustomColorArgb(v: Int) {
        context.settingsDataStore.edit { it[EVENT_CUSTOM_COLOR_ARGB] = v }
    }

    val eventGradientStartIndex: Flow<Int> = context.settingsDataStore.data.map { it[EVENT_GRADIENT_START_INDEX] ?: -1 }
    val eventGradientEndIndex: Flow<Int> = context.settingsDataStore.data.map { it[EVENT_GRADIENT_END_INDEX] ?: -1 }
    val eventGradientStartArgb: Flow<Int> = context.settingsDataStore.data.map { it[EVENT_GRADIENT_START_ARGB] ?: 0 }
    val eventGradientEndArgb: Flow<Int> = context.settingsDataStore.data.map { it[EVENT_GRADIENT_END_ARGB] ?: 0 }
    val eventGradientDirection: Flow<Int> = context.settingsDataStore.data.map { it[EVENT_GRADIENT_DIRECTION] ?: 0 }

    suspend fun setEventGradient(
        startIdx: Int,
        endIdx: Int,
        startArgb: Int,
        endArgb: Int,
        direction: Int,
    ) {
        context.settingsDataStore.edit {
            it[EVENT_GRADIENT_START_INDEX] = startIdx
            it[EVENT_GRADIENT_END_INDEX] = endIdx
            it[EVENT_GRADIENT_START_ARGB] = startArgb
            it[EVENT_GRADIENT_END_ARGB] = endArgb
            it[EVENT_GRADIENT_DIRECTION] = direction
        }
    }

    suspend fun clearEventGradient() {
        context.settingsDataStore.edit {
            it[EVENT_GRADIENT_START_INDEX] = -1
            it[EVENT_GRADIENT_END_INDEX] = -1
            it[EVENT_GRADIENT_START_ARGB] = 0
            it[EVENT_GRADIENT_END_ARGB] = 0
            it[EVENT_GRADIENT_DIRECTION] = 0
        }
    }

    // ==================== 主题配色 ====================
    val themeColorIndex: Flow<Int> = context.settingsDataStore.data.map { it[THEME_COLOR_INDEX] ?: 0 }

    suspend fun setThemeColorIndex(v: Int) {
        context.settingsDataStore.edit { it[THEME_COLOR_INDEX] = v }
    }

    val themeCustomColorArgb: Flow<Int> = context.settingsDataStore.data.map { it[THEME_CUSTOM_COLOR_ARGB] ?: 0 }

    suspend fun setThemeCustomColorArgb(v: Int) {
        context.settingsDataStore.edit { it[THEME_CUSTOM_COLOR_ARGB] = v }
    }

    val themeGradientStartIndex: Flow<Int> = context.settingsDataStore.data.map { it[THEME_GRADIENT_START_INDEX] ?: -1 }
    val themeGradientEndIndex: Flow<Int> = context.settingsDataStore.data.map { it[THEME_GRADIENT_END_INDEX] ?: -1 }
    val themeGradientStartArgb: Flow<Int> = context.settingsDataStore.data.map { it[THEME_GRADIENT_START_ARGB] ?: 0 }
    val themeGradientEndArgb: Flow<Int> = context.settingsDataStore.data.map { it[THEME_GRADIENT_END_ARGB] ?: 0 }
    val themeGradientDirection: Flow<Int> = context.settingsDataStore.data.map { it[THEME_GRADIENT_DIRECTION] ?: 0 }

    suspend fun setThemeGradient(
        startIdx: Int,
        endIdx: Int,
        startArgb: Int,
        endArgb: Int,
        direction: Int,
    ) {
        context.settingsDataStore.edit {
            it[THEME_GRADIENT_START_INDEX] = startIdx
            it[THEME_GRADIENT_END_INDEX] = endIdx
            it[THEME_GRADIENT_START_ARGB] = startArgb
            it[THEME_GRADIENT_END_ARGB] = endArgb
            it[THEME_GRADIENT_DIRECTION] = direction
        }
    }

    suspend fun clearThemeGradient() {
        context.settingsDataStore.edit {
            it[THEME_GRADIENT_START_INDEX] = -1
            it[THEME_GRADIENT_END_INDEX] = -1
            it[THEME_GRADIENT_START_ARGB] = 0
            it[THEME_GRADIENT_END_ARGB] = 0
            it[THEME_GRADIENT_DIRECTION] = 0
        }
    }

    // ==================== 其他 ====================
    val darkMode: Flow<Boolean> = context.settingsDataStore.data.map { it[DARK_MODE] ?: false }

    suspend fun setDarkMode(v: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE] = v }
    }

    val followSystemDarkMode: Flow<Boolean> = context.settingsDataStore.data.map { it[FOLLOW_SYSTEM_DARK_MODE] ?: true }

    suspend fun setFollowSystemDarkMode(v: Boolean) {
        context.settingsDataStore.edit { it[FOLLOW_SYSTEM_DARK_MODE] = v }
    }

    val backgroundUri: Flow<String> = context.settingsDataStore.data.map { it[BACKGROUND_URI] ?: "" }

    suspend fun setBackgroundUri(v: String) {
        context.settingsDataStore.edit { it[BACKGROUND_URI] = v }
    }

    val backgroundOpacity: Flow<Float> = context.settingsDataStore.data.map { it[BACKGROUND_OPACITY] ?: 0.3f }

    suspend fun setBackgroundOpacity(v: Float) {
        context.settingsDataStore.edit { it[BACKGROUND_OPACITY] = v }
    }

    /** 背景图片模糊度 */
    val backgroundBlur: Flow<Float> = context.settingsDataStore.data.map { it[BACKGROUND_BLUR] ?: 0f }

    suspend fun setBackgroundBlur(v: Float) {
        context.settingsDataStore.edit { it[BACKGROUND_BLUR] = v }
    }

    /** 磨砂效果遮罩不透明度 */
    val barOpacity: Flow<Float> = context.settingsDataStore.data.map { it[BAR_OPACITY] ?: 0.3f }

    suspend fun setBarOpacity(v: Float) {
        context.settingsDataStore.edit { it[BAR_OPACITY] = v }
    }

    /** 应用背景主开关 */
    val appBackgroundEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[APP_BACKGROUND_ENABLED] ?: true }

    suspend fun setAppBackgroundEnabled(v: Boolean) {
        context.settingsDataStore.edit { it[APP_BACKGROUND_ENABLED] = v }
    }

    /** 暗色模式下显示背景 */
    val darkModeBackground: Flow<Boolean> = context.settingsDataStore.data.map { it[DARK_MODE_BACKGROUND] ?: true }

    suspend fun setDarkModeBackground(v: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE_BACKGROUND] = v }
    }

    /** 卡片透明度 */
    val cardOpacity: Flow<Float> = context.settingsDataStore.data.map { it[CARD_OPACITY] ?: 0.9f }

    suspend fun setCardOpacity(v: Float) {
        context.settingsDataStore.edit { it[CARD_OPACITY] = v }
    }

    /** 将预览卡片设为默认样式 */
    val usePreviewAsDefault: Flow<Boolean> = context.settingsDataStore.data.map { it[USE_PREVIEW_AS_DEFAULT] ?: false }

    suspend fun setUsePreviewAsDefault(v: Boolean) {
        context.settingsDataStore.edit { it[USE_PREVIEW_AS_DEFAULT] = v }
    }

    // ==================== 背景历史管理 ====================

    /** 获取背景历史列表 */
    val backgroundHistory: Flow<List<String>> = context.settingsDataStore.data.map { prefs ->
        val json = prefs[BACKGROUND_HISTORY] ?: "[]"
        try {
            val array = JSONArray(json)
            List(array.length()) { array.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** 获取当前使用的背景 */
    val activeBackground: Flow<String> = context.settingsDataStore.data.map { it[ACTIVE_BACKGROUND] ?: "" }

    /** 设置当前使用的背景 */
    suspend fun setActiveBackground(uri: String) {
        context.settingsDataStore.edit { it[ACTIVE_BACKGROUND] = uri }
    }

    /** 添加背景到历史 */
    suspend fun addBackgroundToHistory(uri: String) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[BACKGROUND_HISTORY] ?: "[]"
            val array = try {
                JSONArray(current)
            } catch (_: Exception) {
                JSONArray()
            }
            // 避免重复
            for (i in 0 until array.length()) {
                if (array.getString(i) == uri) return@edit
            }
            array.put(uri)
            prefs[BACKGROUND_HISTORY] = array.toString()
        }
    }

    /** 从历史中删除背景 */
    suspend fun removeBackgroundFromHistory(uri: String) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[BACKGROUND_HISTORY] ?: "[]"
            val array = try {
                JSONArray(current)
            } catch (_: Exception) {
                JSONArray()
            }
            val newArray = JSONArray()
            for (i in 0 until array.length()) {
                if (array.getString(i) != uri) {
                    newArray.put(array.getString(i))
                }
            }
            prefs[BACKGROUND_HISTORY] = newArray.toString()
            // 如果删除的是当前背景，清空当前背景
            if (prefs[ACTIVE_BACKGROUND] == uri) {
                prefs[ACTIVE_BACKGROUND] = ""
            }
        }
    }

    /** 置顶背景（移到第一位） */
    suspend fun moveBackgroundToTop(uri: String) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[BACKGROUND_HISTORY] ?: "[]"
            val array = try {
                JSONArray(current)
            } catch (_: Exception) {
                JSONArray()
            }
            val newArray = JSONArray()
            newArray.put(uri) // 先放要置顶的
            for (i in 0 until array.length()) {
                val item = array.getString(i)
                if (item != uri) {
                    newArray.put(item)
                }
            }
            prefs[BACKGROUND_HISTORY] = newArray.toString()
        }
    }

    /** 预设背景列表（后续添加图片） */
    val presetBackgrounds: List<String> = emptyList()

    val widgetTransparency: Flow<Float> = context.settingsDataStore.data.map { it[WIDGET_TRANSPARENCY] ?: 0.8f }

    suspend fun setWidgetTransparency(v: Float) {
        context.settingsDataStore.edit { it[WIDGET_TRANSPARENCY] = v }
    }

    val widgetThemeColor: Flow<Int> = context.settingsDataStore.data.map { it[WIDGET_THEME_COLOR] ?: 0 }

    suspend fun setWidgetThemeColor(v: Int) {
        context.settingsDataStore.edit { it[WIDGET_THEME_COLOR] = v }
    }

    val widgetSize: Flow<String> = context.settingsDataStore.data.map { it[WIDGET_SIZE] ?: "medium" }

    suspend fun setWidgetSize(v: String) {
        context.settingsDataStore.edit { it[WIDGET_SIZE] = v }
    }

    val animationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[ANIMATION_ENABLED] ?: true }

    suspend fun setAnimationEnabled(v: Boolean) {
        context.settingsDataStore.edit { it[ANIMATION_ENABLED] = v }
    }

    val vibrationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[VIBRATION_ENABLED] ?: true }

    suspend fun setVibrationEnabled(v: Boolean) {
        context.settingsDataStore.edit { it[VIBRATION_ENABLED] = v }
    }
}
