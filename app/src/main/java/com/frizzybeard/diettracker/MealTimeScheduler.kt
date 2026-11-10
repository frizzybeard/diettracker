
package com.frizzybeard.diettracker

import android.Manifest
import android.app.AlarmManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver as AndroidBroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

object MealReminderScheduler {

    private const val CHANNEL_ID = "meal_reminders"
    private const val CHANNEL_NAME = "Meal reminders"

    const val EXTRA_REMINDER_KEY = "reminder_key"

    private val reminderIds = mapOf(
        "breakfast" to 1001,
        "lunch" to 1002,
        "dinner" to 1003
    )

    private val reminderTitles = mapOf(
        "breakfast" to "Breakfast reminder",
        "lunch" to "Lunch reminder",
        "dinner" to "Dinner reminder"
    )

    fun schedule(context: Context, key: String) {
        val config = MealReminderPreferences.load(
            context,
            key,
            defaultHour(key),
            0
        )

        if (!config.enabled) {
            cancel(context, key)
            return
        }

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = getPendingIntent(context, key) ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.hour)
            set(Calendar.MINUTE, config.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancel(context: Context, key: String) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = getPendingIntent(context, key) ?: return

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun rescheduleAll(context: Context) {
        reminderIds.keys.forEach { key ->
            schedule(context, key)
        }
    }

    private fun getPendingIntent(
        context: Context,
        key: String
    ): PendingIntent? {
        val requestCode = reminderIds[key] ?: return null

        val intent = Intent(context, MealReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_KEY, key)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    internal fun showNotification(context: Context, key: String) {
        createNotificationChannel(context)

        if (
            Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val title = reminderTitles[key] ?: "Meal reminder"
        val notificationId = reminderIds[key] ?: return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Time to record your meal in DietTracker.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    private fun defaultHour(key: String): Int {
        return when (key) {
            "breakfast" -> 8
            "lunch" -> 13
            "dinner" -> 20
            else -> 8
        }
    }
}

class MealReminderReceiver : AndroidBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val key = intent.getStringExtra(
            MealReminderScheduler.EXTRA_REMINDER_KEY
        ) ?: return

        MealReminderScheduler.showNotification(context, key)

        // Schedule the next occurrence for the following day.
        MealReminderScheduler.schedule(context, key)
    }
}

class MealReminderBootReceiver : AndroidBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED
        ) {
            MealReminderScheduler.rescheduleAll(context)
        }
    }
}
