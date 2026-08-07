package com.daycountapp.util

import android.content.Context
import androidx.work.*
import com.daycountapp.DayCountApp
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as DayCountApp
        val events = app.eventRepository.allEvents
        return Result.success()
    }

    companion object {
        fun scheduleDaily(context: Context) {
            val request =
                PeriodicWorkRequestBuilder<ReminderWorker>(
                    24,
                    TimeUnit.HOURS,
                ).setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build(),
                ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daycount_daily_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
