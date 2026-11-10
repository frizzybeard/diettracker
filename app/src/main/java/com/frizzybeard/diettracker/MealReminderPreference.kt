
package com.frizzybeard.diettracker

import android.content.Context

data class MealReminderConfig(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

object MealReminderPreferences {

    private const val PREFS_NAME = "meal_reminder_preferences"

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
    }
}
