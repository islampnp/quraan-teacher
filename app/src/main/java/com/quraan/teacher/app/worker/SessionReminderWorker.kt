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
import com.quraan.teacher.app.MainActivity
import com.quraan.teacher.app.R
import com.quraan.teacher.app.data.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

class SessionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "session_reminders"
        const val NOTIFICATION_ID = 1001

        fun createOneTimeRequest(scheduleId: Long, delayMinutes: Long): OneTimeWorkRequest {
            val inputData = workDataOf("schedule_id" to scheduleId)
            return OneTimeWorkRequestBuilder<SessionReminderWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(inputData)
                .addTag("session_reminder")
                .build()
        }
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        val scheduleId = inputData.getLong("schedule_id", -1L)
        if (scheduleId == -1L) return Result.failure()

        val schedule = scheduleRepository.getScheduleById(scheduleId) ?: return Result.failure()

        val sessionTypeName = when (schedule.sessionType) {
            "NEW_MEMORIZATION" -> "حفظ جديد"
            "REVISION" -> "مراجعة"
            "TAJWEED" -> "تجويد"
            "QUIZ" -> "اختبار"
            else -> schedule.sessionType
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حصة قرآن قادمة")
            .setContentText("${schedule.subject.ifBlank { sessionTypeName }} - ${schedule.startTime}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "تأكيد الحضور", pendingIntent)
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
            "تذكير بالحصص",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات تذكير بمواعيد الحصص"
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
