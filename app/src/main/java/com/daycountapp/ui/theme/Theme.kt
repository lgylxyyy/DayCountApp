package com.daycountapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.daycountapp.DayCountApp
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX

private fun lightColorSchemeWithPrimary(primary: Color) =
    lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.15f),
        secondary = primary,
        onSecondary = Color.White,
        background = PageBackground,
        onBackground = TextPrimary,
        surface = CardBackground,
        onSurface = TextPrimary,
        surfaceVariant = PageBackground,
        onSurfaceVariant = TextSecondary,
        outline = Separator,
        outlineVariant = Separator,
        error = DestructiveRed,
        onError = Color.White,
    )

private fun darkColorSchemeWithPrimary(primary: Color) =
    darkColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.3f),
        secondary = primary,
        onSecondary = Color.White,
        background = DarkPageBackground,
        onBackground = DarkTextPrimary,
        surface = DarkCardBackground,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkPageBackground,
        onSurfaceVariant = DarkTextSecondary,
        outline = DarkSeparator,
        outlineVariant = DarkSeparator,
        error = DestructiveRed,
        onError = Color.White,
    )

@Composable
fun DayCountTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val app = DayCountApp.instance
    val settingsDarkMode by app.appSettings.darkMode.collectAsState(initial = false)
    val themeColorIndex by app.appSettings.themeColorIndex.collectAsState(initial = 0)
    val themeCustomArgb by app.appSettings.themeCustomColorArgb.collectAsState(initial = 0)
    val themeGradientStartIdx by app.appSettings.themeGradientStartIndex.collectAsState(initial = -1)
    val themeGradientEndIdx by app.appSettings.themeGradientEndIndex.collectAsState(initial = -1)
    val themeGradientStartArgb by app.appSettings.themeGradientStartArgb.collectAsState(initial = 0)
    val themeGradientEndArgb by app.appSettings.themeGradientEndArgb.collectAsState(initial = 0)
    val themeGradientDirection by app.appSettings.themeGradientDirection.collectAsState(initial = 0)
    val followSystemDarkMode by app.appSettings.followSystemDarkMode.collectAsState(initial = true)
    val effectiveDarkTheme = if (followSystemDarkMode) darkTheme else settingsDarkMode

    val userPrimary =
        when {
            // 过渡色模式：优先取主色
            themeGradientStartIdx >= 0 && themeColorIndex == CUSTOM_COLOR_INDEX -> {
                when {
                    themeGradientStartArgb != 0 -> Color(themeGradientStartArgb)
                    themeGradientStartIdx in EventColors.indices -> EventColors[themeGradientStartIdx]
                    else -> BrandBlue
                }
            }

            themeColorIndex in EventColors.indices -> {
                EventColors[themeColorIndex]
            }

            themeColorIndex == CUSTOM_COLOR_INDEX -> {
                if (themeCustomArgb != 0) Color(themeCustomArgb) else BrandBlue
            }

            else -> {
                BrandBlue
            }
        }

    val colorScheme =
        if (effectiveDarkTheme) {
            darkColorSchemeWithPrimary(userPrimary)
        } else {
            lightColorSchemeWithPrimary(userPrimary)
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !effectiveDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
