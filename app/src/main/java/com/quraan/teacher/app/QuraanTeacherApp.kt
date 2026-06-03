package com.quraan.teacher.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.quraan.teacher.app.data.local.AppDatabase
import com.quraan.teacher.app.worker.WeeklyReportWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class QuraanTeacherApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var database: AppDatabase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Seed data on first launch
        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seedDatabase(this@QuraanTeacherApp, database)
        }

        // Schedule weekly report
        scheduleWeeklyReport()
    }

    private fun scheduleWeeklyReport() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val weeklyReport = PeriodicWorkRequestBuilder<WeeklyReportWorker>(
            7, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.DAYS)
            .addTag("weekly_report")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weekly_report",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyReport
        )
    }

    companion object {
        lateinit var instance: QuraanTeacherApp
            private set
    }
}
