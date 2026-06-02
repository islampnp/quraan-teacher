package com.quraan.teacher.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import androidx.hilt.work.HiltWorker
import com.quraan.teacher.app.MainActivity
import com.quraan.teacher.app.R
import com.quraan.teacher.app.data.repository.ProgressRepository
import com.quraan.teacher.app.data.repository.StudentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class WeeklyReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val studentRepository: StudentRepository,
    private val progressRepository: ProgressRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "weekly_reports"
        const val NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val weekStart = calendar.timeInMillis
        val weekEnd = System.currentTimeMillis()

        val studentCount = progressRepository.getDistinctStudentCountInRange(weekStart, weekEnd)
        val totalAyahs = progressRepository.getTotalMemorizedInRange(weekStart, weekEnd)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("تقرير الأسبوع")
            .setContentText("$studentCount طلاب حضروا - $totalAyahs آية محفوظة")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("تقرير الأسبوع:\nعدد الطلاب الحاضرين: $studentCount\nإجمالي الآيات المحفوظة: $totalAyahs")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "التقارير الأسبوعية",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "تقارير أداء الطلاب الأسبوعية"
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
