package com.daycountapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun WheelDatePicker(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cal = remember { Calendar.getInstance().apply { timeInMillis = initialDate } }

    var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH)) }
    var userDay by remember { mutableIntStateOf(cal.get(Calendar.DAY_OF_MONTH)) }

    val daysInMonth =
        remember(selectedYear, selectedMonth) {
            val c = Calendar.getInstance()
            c.set(selectedYear, selectedMonth, 1)
            c.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

    val actualDay =
        remember(userDay, daysInMonth) {
            if (userDay > daysInMonth) daysInMonth else userDay
        }

    LaunchedEffect(selectedYear, selectedMonth, actualDay) {
        val c = Calendar.getInstance()
        c.set(selectedYear, selectedMonth, actualDay)
        onDateSelected(c.timeInMillis)
    }

    val years = remember { (1900..2100).toList() }
    val months = remember { (1..12).toList() }
    val days = remember(daysInMonth) { (1..daysInMonth).toList() }

    // 组件级拦截器：放在 WheelDatePicker 最外层
    // 只拦截 onPostScroll：子 LazyColumn 先消费自己能消费的，
    // 溢出部分（available）被本组件吸收，阻止其继续向外层 VerticalScroll 传递
    val scrollIntercept =
        remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset = available
            }
        }

    Column(modifier = modifier.nestedScroll(scrollIntercept)) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DateWheel(
                    items = years,
                    startIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                    format = { it.toString() },
                    onChanged = { selectedYear = years[it] },
                    modifier = Modifier.weight(1f),
                )
                DateWheel(
                    items = months,
                    startIndex = selectedMonth.coerceAtLeast(0),
                    format = { String.format("%02d", it) },
                    onChanged = { selectedMonth = months[it] - 1 },
                    modifier = Modifier.weight(1f),
                )
                DateWheel(
                    items = days,
                    startIndex = (actualDay - 1).coerceIn(0, (days.size - 1).coerceAtLeast(0)),
                    format = { String.format("%02d", it) },
                    onChanged = { if (it in days.indices) userDay = days[it] },
                    modifier = Modifier.weight(1f),
                )
            }
            GradientOverlay(modifier = Modifier.matchParentSize())
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf("\u5E74", "\u6708", "\u65E5").forEach { label ->
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DateWheel(
    items: List<Int>,
    startIndex: Int,
    format: (Int) -> String,
    onChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemH = 44.dp
    val rowCount = 5
    val scope = rememberCoroutineScope()

    val state =
        rememberLazyListState(
            initialFirstVisibleItemIndex = startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)),
        )
    val fling = rememberSnapFlingBehavior(state)

    // 抑制 scrollToItem 期间触发 onChanged
    var suppressing by remember { mutableStateOf(false) }

    // 选中项检测：用 derivedStateOf
    // 顶部 2 个 Spacer 让 firstVisibleItemIndex 恰好等于居中内容索引
    val centerIdx by remember {
        derivedStateOf {
            state.firstVisibleItemIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        }
    }
    val snapped = remember { mutableIntStateOf(startIndex) }
    LaunchedEffect(centerIdx) {
        if (!suppressing) {
            snapped.intValue = centerIdx
            onChanged(centerIdx)
        }
    }

    // items 变化时同步滚动，抑制期间不触发 onChanged
    LaunchedEffect(items.size) {
        suppressing = true
        try {
            state.animateScrollToItem(
                startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)),
            )
        } catch (_: Exception) {
        }
        suppressing = false
        snapped.intValue =
            state.firstVisibleItemIndex
                .coerceIn(0, (items.size - 1).coerceAtLeast(0))
    }

    Box(modifier = modifier.fillMaxHeight()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            flingBehavior = fling,
        ) {
            // 顶部 2 + 底部 2 个 Spacer
            items(2) { Spacer(Modifier.height(itemH)) }
            itemsIndexed(items) { index, value ->
                val isSel = index == snapped.intValue
                val alpha by animateFloatAsState(
                    targetValue = if (isSel) 1f else 0.35f,
                    animationSpec = tween(200),
                    label = "alpha",
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(itemH)
                            .alpha(alpha)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { scope.launch { state.animateScrollToItem(index) } },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = format(value),
                        fontSize = if (isSel) 22.sp else 16.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color =
                            if (isSel) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
            items(2) { Spacer(Modifier.height(itemH)) }
        }
    }
}

@Composable
private fun GradientOverlay(modifier: Modifier = Modifier) {
    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val t = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
    Box(modifier = modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.verticalGradient(listOf(bg, t)))
                .align(Alignment.TopCenter),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.verticalGradient(listOf(t, bg)))
                .align(Alignment.BottomCenter),
        )
    }
}
