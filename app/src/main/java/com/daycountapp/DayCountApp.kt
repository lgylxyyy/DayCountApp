package com.daycountapp

import android.app.Application
import com.daycountapp.data.PasswordManager
import com.daycountapp.data.local.AppDatabase
import com.daycountapp.data.local.AppSettings
import com.daycountapp.data.repository.EventRepository
import com.daycountapp.util.NotificationHelper
import com.daycountapp.util.ReminderWorker
import com.daycountapp.util.VibrationManager

class DayCountApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var eventRepository: EventRepository
        private set

    lateinit var appSettings: AppSettings
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        VibrationManager.init(this)
        PasswordManager.init(this)
        database = AppDatabase.getInstance(this)
        eventRepository = EventRepository(database.eventDao())
        appSettings = AppSettings(this)
        NotificationHelper.createNotificationChannel(this)
        try {
            ReminderWorker.scheduleDaily(this)
        } catch (e: Exception) {
            android.util.Log.w("DayCount", "WorkManager init failed, reminders disabled", e)
        }
    }

    companion object {
        lateinit var instance: DayCountApp
            private set
    }
}
