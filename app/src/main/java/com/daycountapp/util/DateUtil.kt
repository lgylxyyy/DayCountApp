package com.daycountapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getDaysBetween(targetDateMillis: Long): Long {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val target = Calendar.getInstance()
        target.timeInMillis = targetDateMillis
        target.set(Calendar.HOUR_OF_DAY, 0)
        target.set(Calendar.MINUTE, 0)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)

        val diff = target.timeInMillis - today.timeInMillis
        return diff / (24 * 60 * 60 * 1000)
    }

    fun getDaysRemaining(targetDateMillis: Long): Long {
        val days = getDaysBetween(targetDateMillis)
        return if (days >= 0) days else 0
    }

    fun getDaysPassed(targetDateMillis: Long): Long {
        val days = getDaysBetween(targetDateMillis)
        return if (days < 0) -days else 0
    }

    fun isCountdown(targetDateMillis: Long): Boolean = getDaysBetween(targetDateMillis) >= 0

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDateDisplay(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy\u5E74M\u6708d\u65E5", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
