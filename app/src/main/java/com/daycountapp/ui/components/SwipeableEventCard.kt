package com.daycountapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.daycountapp.DayCountApp
import com.daycountapp.data.model.Event
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.DestructiveRed
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.util.DateUtil

enum class CardVariant {
    MAIN,
    RECYCLE_BIN,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableEventCard(
    event: Event,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onHide: () -> Unit,
    onEdit: () -> Unit = {},
    onLongPress: () -> Unit = {},
    showHideButton: Boolean = false,
    animationDelay: Int = 0,
    variant: CardVariant = CardVariant.MAIN,
) {
    val app = DayCountApp.instance
    val cardOpacity by app.appSettings.cardOpacity.collectAsState(initial = 0.9f)

    val shape = RoundedCornerShape(14.dp)
    val hasDescription = event.description.isNotEmpty()
    val isRecycleBin = variant == CardVariant.RECYCLE_BIN

    val primaryColor =
        when {
            event.colorPreset == CUSTOM_COLOR_INDEX && event.customColorArgb != 0 -> Color(event.customColorArgb)
            event.colorPreset in EventColors.indices -> EventColors[event.colorPreset]
            else -> MaterialTheme.colorScheme.primary
        }

    val secondaryColor =
        when {
            event.colorPresetEnd == CUSTOM_COLOR_INDEX && event.customColorEndArgb != 0 -> Color(event.customColorEndArgb)
            event.colorPresetEnd in EventColors.indices && event.colorPresetEnd >= 0 -> EventColors[event.colorPresetEnd]
            else -> primaryColor
        }

    val days =
        if (event.isCountUp) {
            DateUtil.getDaysPassed(event.targetDate)
        } else {
            val d = DateUtil.getDaysBetween(event.targetDate)
            if (d >= 0) d else -d
        }
    val daysLabel =
        if (event.isCountUp) {
            "已过"
        } else {
            val d = DateUtil.getDaysBetween(event.targetDate)
            if (d >= 0) "剩余" else "已过"
        }
    val dateTypeLabel = if (event.isCountUp) "正数日" else "倒数日"

    val animSpec = tween<androidx.compose.ui.unit.Dp>(450)
    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) 157.dp else 76.dp,
        animationSpec = animSpec,
        label = "cardHeight",
    )
    val barHeight by animateDpAsState(
        targetValue = if (isExpanded) 94.dp else 44.dp,
        animationSpec = animSpec,
        label = "barHeight",
    )
    val daysTextSize by animateFloatAsState(
        targetValue = if (isExpanded) 36f else 14f,
        animationSpec = tween(450),
        label = "daysSize",
    )

    val bgBrush =
        GradientDirection.createBrush(
            event.gradientDirection,
            listOf(primaryColor.copy(alpha = 0.08f), secondaryColor.copy(alpha = 0.08f)),
        )

    // 按钮区宽度
    // 回收站：未展开显示1个删除按钮，展开显示恢复+删除
    // 主界面：未展开无按钮，普通展开显示编辑，长按展开显示隐藏+删除
    val showTwoButtons = showHideButton || (isRecycleBin && isExpanded)
    val buttonAreaWidth =
        if (showTwoButtons) {
            88.dp
        } else if (isExpanded && !isRecycleBin) {
            44.dp
        } else {
            0.dp
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = shape,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = cardOpacity),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .clip(shape)
                    .background(bgBrush),
        ) {
            if (!event.backgroundUri.isNullOrEmpty()) {
                AsyncImage(
                    model = event.backgroundUri,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .alpha(if (isExpanded) 0.5f else 0f),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(5.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(primaryColor),
                )

                Spacer(Modifier.width(14.dp))

                // 内容区域：combinedClickable（回收站禁用长按）
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(top = 12.dp, bottom = 12.dp, end = 4.dp)
                            .combinedClickable(
                                onClick = { onToggle() },
                                onLongClick = {
                                    if (isExpanded && !isRecycleBin) {
                                        onLongPress()
                                    }
                                },
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                            ),
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    AnimatedVisibility(
                        visible = isExpanded && hasDescription,
                        enter = fadeIn(animationSpec = tween(250)),
                        exit = fadeOut(animationSpec = tween(150)),
                    ) {
                        Text(
                            text = event.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (!hasDescription) {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = daysLabel,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Text(
                            text = "$days",
                            fontSize = daysTextSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 2.sp,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Text(
                            text = "天",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.alignByBaseline(),
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(animationSpec = tween(250)),
                        exit = fadeOut(animationSpec = tween(150)),
                    ) {
                        Text(
                            text = "$dateTypeLabel · ${DateUtil.formatDateDisplay(event.targetDate)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }

                // 按钮区
                Box(
                    modifier =
                        Modifier
                            .width(buttonAreaWidth)
                            .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isRecycleBin && isExpanded) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(end = 4.dp)
                                    .zIndex(999f),
                        ) {
                            IconButton(onClick = onHide) {
                                Icon(
                                    Icons.Default.Restore,
                                    contentDescription = "恢复",
                                    tint = primaryColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "彻底删除",
                                    tint = DestructiveRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    } else if (isExpanded) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(end = 4.dp)
                                    .zIndex(999f),
                        ) {
                            if (showHideButton) {
                                IconButton(onClick = onHide) {
                                    Icon(
                                        Icons.Default.VisibilityOff,
                                        contentDescription = "隐藏",
                                        tint = primaryColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                IconButton(onClick = onDelete) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = DestructiveRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            } else {
                                IconButton(onClick = onEdit) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "编辑",
                                        tint = primaryColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (event.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "置顶",
                    tint = primaryColor,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 12.dp)
                            .size(18.dp),
                )
            }
        }
    }
}
