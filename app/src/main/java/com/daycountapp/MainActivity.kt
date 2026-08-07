package com.daycountapp

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import coil.compose.rememberAsyncImagePainter
import com.daycountapp.data.PasswordManager
import com.daycountapp.data.PasswordType
import com.daycountapp.ui.screens.*
import com.daycountapp.ui.theme.DayCountTheme
import com.daycountapp.ui.theme.TextSecondary
import com.daycountapp.ui.viewmodel.EventViewModel
import com.daycountapp.util.VibrationManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DayCountTheme {
                DayCountMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCountMainScreen() {
    val scope = rememberCoroutineScope()
    val app = DayCountApp.instance
    val eventViewModel: EventViewModel = viewModel(factory = EventViewModel.Factory(app.eventRepository))

    val pageState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    var currentFormEventId by remember { mutableStateOf<Long?>(null) }
    val isFormOpen = currentFormEventId != null

    var showHiddenEvents by remember { mutableStateOf(false) }
    var showRecycleBin by remember { mutableStateOf(false) }
    var showPasswordSetup by remember { mutableStateOf(false) }
    var showPinLock by remember { mutableStateOf(false) }
    var showPatternLock by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // 背景相关状态
    var showMyBackground by remember { mutableStateOf(false) }
    var showImageCrop by remember { mutableStateOf(false) }
    var cropImageUri by remember { mutableStateOf<Uri?>(null) }
    var showBackgroundPreview by remember { mutableStateOf(false) }
    var previewImageUri by remember { mutableStateOf("") }
    var editingPreviewEventId by remember { mutableStateOf<Long?>(null) }

    // 直接内存状态跟踪密码类型
    var passwordTypeState by remember { mutableStateOf(PasswordManager.getType()) }

    // 读取背景设置
    val activeBackground by app.appSettings.activeBackground.collectAsState(initial = "")
    val backgroundOpacity by app.appSettings.backgroundOpacity.collectAsState(initial = 0.5f)
    val backgroundBlur by app.appSettings.backgroundBlur.collectAsState(initial = 0f)
    val barOpacity by app.appSettings.barOpacity.collectAsState(initial = 0.3f)
    val appBackgroundEnabled by app.appSettings.appBackgroundEnabled.collectAsState(initial = true)
    val darkModeBackground by app.appSettings.darkModeBackground.collectAsState(initial = true)

    // 同步 PasswordManager 状态到本地
    LaunchedEffect(Unit) {
        passwordTypeState = PasswordManager.getType()
    }

    // 所有子页面的状态
    val isSubPage = showPinLock || showPatternLock || showPasswordSetup ||
            showHiddenEvents || showRecycleBin || isFormOpen ||
            showMyBackground || showImageCrop || showBackgroundPreview ||
            editingPreviewEventId != null

    BackHandler(enabled = isSubPage) {
        when {
            showBackgroundPreview -> {
                showBackgroundPreview = false
                previewImageUri = ""
            }
            showImageCrop -> {
                showImageCrop = false
                cropImageUri = null
            }
            showMyBackground -> {
                showMyBackground = false
            }
            editingPreviewEventId != null -> {
                editingPreviewEventId = null
            }
            showPatternLock -> {
                showPatternLock = false
                pendingAction = null
            }
            showPinLock -> {
                showPinLock = false
                pendingAction = null
            }
            showPasswordSetup -> {
                showPasswordSetup = false
                passwordTypeState = PasswordManager.getType()
            }
            showRecycleBin -> {
                showRecycleBin = false
            }
            showHiddenEvents -> {
                showHiddenEvents = false
            }
            else -> {
                currentFormEventId = null
            }
        }
    }

    fun checkPasswordAndRun(action: () -> Unit) {
        val pmType = PasswordManager.getType()
        if (pmType != PasswordType.NONE) {
            passwordTypeState = pmType
        }
        val type = passwordTypeState
        when (type) {
            PasswordType.NONE -> action()
            PasswordType.FINGERPRINT -> action()
            PasswordType.PIN -> {
                pendingAction = action
                showPinLock = true
            }
            PasswordType.PATTERN -> {
                pendingAction = action
                showPatternLock = true
            }
        }
    }

    val navItems = listOf(
        BottomNavItem("事件管理", Icons.AutoMirrored.Filled.List),
        BottomNavItem("个性化", Icons.Default.Palette),
        BottomNavItem("设置", Icons.Default.Settings),
    )

    val settingsDarkMode by app.appSettings.darkMode.collectAsState(initial = false)
    val followSystemDarkMode by app.appSettings.followSystemDarkMode.collectAsState(initial = true)
    val isDarkTheme = if (followSystemDarkMode) isSystemInDarkTheme() else settingsDarkMode

    // 深色模式切换动画
    val backgroundColor by animateColorAsState(
        targetValue = if (isDarkTheme) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    // 判断是否显示背景图片
    val shouldShowBackground = when {
        !appBackgroundEnabled -> false
        activeBackground.isEmpty() -> false
        isDarkTheme && !darkModeBackground -> false
        else -> true
    }

    // 内容背景层 alpha（透明度滑块控制）
    val contentBackgroundAlpha = if (shouldShowBackground) backgroundOpacity else 1f

    // 标题文字（仅主页面使用）
    val titleText = when (pageState.currentPage) {
        0 -> "DayCount"
        1 -> "个性化"
        2 -> "设置"
        else -> "DayCount"
    }

    val titleFontSize by animateFloatAsState(
        targetValue = if (isSubPage) 18f else 28f,
        animationSpec = tween(300),
        label = "titleFontSize"
    )

    val titleAlignment = if (isSubPage) Alignment.Center else Alignment.CenterStart

    // 获取窗口Insets，处理状态栏和导航栏
    val view = LocalView.current
    val windowInsets = ViewCompat.getRootWindowInsets(view)
    val statusBarHeight = windowInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
    val navigationBarHeight = windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0

    // 顶部栏和底部栏高度
    val topBarHeight = 60.dp + statusBarHeight.toDp()
    val bottomBarHeight = 80.dp + navigationBarHeight.toDp()

    // 背景图片模糊度（用户可调，0~1映射到0~20dp）
    val bgBlurRadius = (backgroundBlur * 20f).dp

    // 屏幕尺寸（用于bar模糊层对齐）
    val density = LocalDensity.current
    val screenHeightPx = view.height

    // ========== 手动布局，不用Scaffold ==========
    Box(modifier = Modifier.fillMaxSize()) {

        // --- 第1层：全屏内容（背景图 + 遮罩 + 页面内容） ---
        PageContent(
            shouldShowBackground = shouldShowBackground,
            activeBackground = activeBackground,
            bgBlurRadius = bgBlurRadius,
            backgroundColor = backgroundColor,
            contentBackgroundAlpha = contentBackgroundAlpha,
            modifier = Modifier.fillMaxSize(),
            topPadding = if (!isSubPage) topBarHeight else 0.dp,
            bottomPadding = if (!isSubPage) bottomBarHeight else 0.dp,
        ) {
            when {
                showBackgroundPreview -> {
                    BackgroundPreviewScreen(
                        imageUri = previewImageUri,
                        onNavigateBack = {
                            showBackgroundPreview = false
                            previewImageUri = ""
                        }
                    )
                }
                showImageCrop -> {
                    cropImageUri?.let { uri ->
                        ImageCropScreen(
                            imageUri = uri,
                            onNavigateBack = {
                                showImageCrop = false
                                cropImageUri = null
                            },
                            onCropComplete = {
                                showImageCrop = false
                                cropImageUri = null
                            }
                        )
                    }
                }
                showMyBackground -> {
                    MyBackgroundScreen(
                        onNavigateBack = { showMyBackground = false },
                        onPreviewImage = { uri ->
                            previewImageUri = uri
                            showBackgroundPreview = true
                        }
                    )
                }
                editingPreviewEventId != null -> {
                    EventFormScreen(
                        viewModel = eventViewModel,
                        eventId = editingPreviewEventId,
                        onNavigateBack = { editingPreviewEventId = null },
                    )
                }
                showPinLock -> {
                    PinLockScreen(
                        onNavigateBack = {
                            showPinLock = false
                            pendingAction = null
                        },
                        onSuccess = {
                            showPinLock = false
                            pendingAction?.invoke()
                            pendingAction = null
                        },
                    )
                }
                showPatternLock -> {
                    PatternLockScreen(
                        onNavigateBack = {
                            showPatternLock = false
                            pendingAction = null
                        },
                        onSuccess = {
                            showPatternLock = false
                            pendingAction?.invoke()
                            pendingAction = null
                        },
                    )
                }
                showPasswordSetup -> {
                    PasswordSetupScreen(
                        onNavigateBack = {
                            showPasswordSetup = false
                            passwordTypeState = PasswordManager.getType()
                        },
                        onPasswordChanged = { type ->
                            passwordTypeState = type
                            PasswordManager.save(app)
                        },
                    )
                }
                showRecycleBin -> {
                    RecycleBinScreen(onNavigateBack = { showRecycleBin = false })
                }
                showHiddenEvents -> {
                    HiddenEventsScreen(
                        onNavigateBack = { showHiddenEvents = false },
                        onEditEvent = { id ->
                            showHiddenEvents = false
                            currentFormEventId = id
                        },
                    )
                }
                isFormOpen -> {
                    EventFormScreen(
                        viewModel = eventViewModel,
                        eventId = when (currentFormEventId) {
                            -1L -> null
                            else -> currentFormEventId
                        },
                        onNavigateBack = { currentFormEventId = null },
                    )
                }
                else -> {
                    HorizontalPager(state = pageState, modifier = Modifier.fillMaxSize()) { page ->
                        when (page) {
                            0 -> {
                                EventManagementScreen(
                                    viewModel = eventViewModel,
                                    onNavigateToForm = { currentFormEventId = -1L },
                                    onEventClick = { currentFormEventId = it },
                                    onNavigateToHidden = { checkPasswordAndRun { showHiddenEvents = true } },
                                )
                            }
                            1 -> PersonalizationScreen(
                                onNavigateBack = {},
                                onNavigateToMyBackground = { showMyBackground = true },
                                onNavigateToImageCrop = { uri ->
                                    cropImageUri = uri
                                    showImageCrop = true
                                },
                                onNavigateToPreviewEdit = { eventId ->
                                    editingPreviewEventId = eventId
                                },
                                isEditingPreview = editingPreviewEventId != null,
                            )
                            2 -> SettingsScreen(
                                onNavigateBack = {},
                                onNavigateToHidden = { checkPasswordAndRun { showHiddenEvents = true } },
                                onNavigateToRecycleBin = { checkPasswordAndRun { showRecycleBin = true } },
                                onNavigateToPasswordSetup = { checkPasswordAndRun { showPasswordSetup = true } },
                            )
                        }
                    }
                }
            }
        }

        // --- 第2层：顶部bar（仅主页面显示） ---
        // 不渲染独立背景，直接复用第1层的全屏背景，确保过渡自然
        if (!isSubPage) {
            // UI层：标题文字（背景完全透明，直接透出第1层）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .padding(horizontal = 16.dp)
                    .statusBarsPadding(),
                contentAlignment = titleAlignment,
            ) {
                Text(
                    text = titleText,
                    fontSize = titleFontSize.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onDoubleClick = {
                            if (pageState.currentPage == 0) {
                                checkPasswordAndRun {
                                    VibrationManager.vibrate(10L)
                                    showHiddenEvents = true
                                }
                            }
                        },
                        onClick = {},
                    ),
                )
            }
        }

        // --- 第3层：底部bar毛玻璃（裁剪真实内容 + 模糊） ---
        if (!isSubPage) {
            val bottomBarHeightPx = with(density) { bottomBarHeight.roundToPx() }
            val bottomBarOffsetY = (screenHeightPx - bottomBarHeightPx).toDp()

            // 模糊背景层：全屏背景 → clip到bar区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
                    .align(Alignment.BottomCenter)
                    .clipToBounds()
            ) {
                // 全屏大小背景（含背景模糊），裁剪后只显示底部bar区域
                if (shouldShowBackground) {
                    Image(
                        painter = rememberAsyncImagePainter(model = activeBackground),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeightPx.toDp())
                            .offset(y = -bottomBarOffsetY)
                            .blur(bgBlurRadius)
                    )
                }
            }
            // 遮罩层
            if (shouldShowBackground) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomBarHeight)
                        .align(Alignment.BottomCenter)
                        .background(backgroundColor.copy(alpha = barOpacity * 0.5f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomBarHeight)
                        .align(Alignment.BottomCenter)
                        .background(backgroundColor)
                )
            }
            // 顶部分割线
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .offset(y = (-bottomBarHeight))
            )
            // UI层：导航栏内容
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navigationBarHeight.toDp())
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navItems.forEachIndexed { index, item ->
                    val selected = index == pageState.currentPage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { scope.launch { pageState.animateScrollToPage(index) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary else TextSecondary,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 页面内容组件 —— 背景图 + 半透明遮罩 + 页面内容
 * 被渲染3次：全屏一次（不模糊），顶部bar区域一次（+模糊），底部bar区域一次（+模糊）
 *
 * @param topPadding 内容顶部留白（避开顶部bar）
 * @param bottomPadding 内容底部留白（避开底部bar）
 */
@Composable
private fun PageContent(
    shouldShowBackground: Boolean,
    activeBackground: String,
    bgBlurRadius: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    contentBackgroundAlpha: Float,
    modifier: Modifier = Modifier,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        // 背景图片
        if (shouldShowBackground) {
            Image(
                painter = rememberAsyncImagePainter(model = activeBackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(bgBlurRadius)
            )
        }
        // 半透明遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = contentBackgroundAlpha))
        )
        // 页面内容（带顶部和底部padding，避开bar区域）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding)
        ) {
            content()
        }
    }
}

// dp转换扩展函数
@Composable
fun Int.toDp(): androidx.compose.ui.unit.Dp {
    return (this / android.content.res.Resources.getSystem().displayMetrics.density).dp
}

data class BottomNavItem(val label: String, val icon: ImageVector)
