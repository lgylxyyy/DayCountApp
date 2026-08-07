package com.daycountapp.widget

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class DayCountWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = DayCountViewsFactory()

    class DayCountViewsFactory : RemoteViewsService.RemoteViewsFactory {
        override fun onCreate() {}

        override fun onDataSetChanged() {}

        override fun onDestroy() {}

        override fun getCount(): Int = 0

        override fun getViewAt(position: Int): RemoteViews? = null

        override fun getLoadingView(): RemoteViews? = null

        override fun getViewTypeCount(): Int = 1

        override fun getItemId(position: Int): Long = position.toLong()

        override fun hasStableIds(): Boolean = true
    }
}
