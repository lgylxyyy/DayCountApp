package com.daycountapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.daycountapp.DayCountApp
import com.daycountapp.data.model.Event
import com.daycountapp.ui.theme.CUSTOM_COLOR_INDEX
import com.daycountapp.ui.theme.EventColors
import com.daycountapp.ui.theme.GradientDirection
import com.daycountapp.util.DateUtil

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = DayCountApp.instance
    val cardOpacity by app.appSettings.cardOpacity.collectAsState(initial = 0.9f)

    val cardColor =
        when {
            event.colorPreset == CUSTOM_COLOR_INDEX && event.customColorArgb != 0 -> {
                Color(event.customColorArgb)
            }

            event.colorPreset in EventColors.indices -> {
                EventColors[event.colorPreset]
            }

            else -> {
                MaterialTheme.colorScheme.primary
            }
        }

    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardOpacity),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Event background image overlay
            if (!event.backgroundUri.isNullOrEmpty()) {
                AsyncImage(
                    model = event.backgroundUri,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .alpha(event.backgroundOpacity)
                            .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Color indicator (supports gradient with custom colors)
                val gradientEndColor =
                    when {
                        event.colorPresetEnd == CUSTOM_COLOR_INDEX && event.customColorEndArgb != 0 -> {
                            Color(event.customColorEndArgb)
                        }

                        event.colorPresetEnd in EventColors.indices -> {
                            EventColors[event.colorPresetEnd]
                        }

                        else -> {
                            null
                        }
                    }
                val hasGradient = gradientEndColor != null
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (hasGradient) {
                                    Modifier.background(
                                        brush =
                                            GradientDirection.createBrush(
                                                event.gradientDirection,
                                                listOf(cardColor, gradientEndColor!!),
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                } else {
                                    Modifier.background(cardColor, RoundedCornerShape(12.dp))
                                },
                            ),
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (event.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = cardColor,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val daysBetween = DateUtil.getDaysBetween(event.targetDate)
                    val daysText =
                        if (event.isCountUp) {
                            "已过 ${DateUtil.getDaysPassed(event.targetDate)} 天"
                        } else {
                            if (daysBetween >= 0) {
                                "剩余 $daysBetween 天"
                            } else {
                                "已过 ${-daysBetween} 天"
                            }
                        }

                    Text(
                        text = daysText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
