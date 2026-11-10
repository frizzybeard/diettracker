
package com.frizzybeard.diettracker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

data class MealReminderConfig(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

object MealReminderPreferences {

    private const val PREFS_NAME = "meal_reminder_preferences"
    private const val NOTIFICATION_PERMISSION_REQUEST = 2001

    private fun preferences(context: Context) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun load(
        context: Context,
        key: String,
        defaultHour: Int,
        defaultMinute: Int
    ): MealReminderConfig {

        val prefs = preferences(context)

        return MealReminderConfig(
            enabled = prefs.getBoolean("${key}_enabled", false),
            hour = prefs.getInt("${key}_hour", defaultHour),
            minute = prefs.getInt("${key}_minute", defaultMinute)
        )
    }

    fun saveEnabled(
        context: Context,
        key: String,
        enabled: Boolean
    ) {
        preferences(context)
            .edit()
            .putBoolean("${key}_enabled", enabled)
            .apply()

        if (enabled) {
            requestNotificationPermissionIfNeeded(context)
            MealReminderScheduler.schedule(context, key)
        } else {
            MealReminderScheduler.cancel(context, key)
        }
    }

    fun saveTime(
        context: Context,
        key: String,
        hour: Int,
        minute: Int
    ) {
        preferences(context)
            .edit()
            .putInt("${key}_hour", hour)
            .putInt("${key}_minute", minute)
            .apply()

        val config = load(context, key, hour, minute)

        if (config.enabled) {
            MealReminderScheduler.schedule(context, key)
        }
    }

    private fun requestNotificationPermissionIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < 33) return

        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (
            ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val activity = context as? Activity ?: return

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            NOTIFICATION_PERMISSION_REQUEST
        )
    }
}
