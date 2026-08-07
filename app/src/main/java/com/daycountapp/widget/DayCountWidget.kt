package com.daycountapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.daycountapp.DayCountApp
import com.daycountapp.MainActivity
import com.daycountapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DayCountWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val app = context.applicationContext as DayCountApp

        CoroutineScope(Dispatchers.IO).launch {
            val events = app.eventRepository.allEvents

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                views.setTextViewText(R.id.widget_title, "DayCount")
                views.setTextViewText(R.id.widget_days, "-- 天")

                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
