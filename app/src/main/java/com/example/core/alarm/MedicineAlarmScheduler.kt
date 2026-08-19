package com.example.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object MedicineAlarmScheduler {

    /**
     * Schedules a local device alarm for the specified hour and minute.
     * If the time is earlier than the current time, schedules for tomorrow.
     */
    fun scheduleAlarm(
        context: Context,
        reminderId: String,
        medicineName: String,
        dosage: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time already passed today, schedule for next day
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra(MedicineAlarmReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(MedicineAlarmReceiver.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(MedicineAlarmReceiver.EXTRA_DOSAGE, dosage)
        }

        val requestCode = (reminderId.hashCode() + (hour * 60 + minute)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // If exact alarm permission is restricted, fallback to standard alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancels scheduled alarm for this reminder time.
     */
    fun cancelAlarm(context: Context, reminderId: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineAlarmReceiver::class.java)
        val requestCode = (reminderId.hashCode() + (hour * 60 + minute)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Parses times string (e.g., "08:00 AM, 08:00 PM" or "14:30") and schedules alarms for all times.
     */
    fun scheduleAllTimes(
        context: Context,
        reminderId: String,
        medicineName: String,
        dosage: String,
        timesString: String
    ) {
        val parts = timesString.split(",").map { it.trim() }
        for (timeStr in parts) {
            val (hour, minute) = parseHourMinute(timeStr)
            scheduleAlarm(context, reminderId, medicineName, dosage, hour, minute)
        }
    }

    fun parseHourMinute(timeStr: String): Pair<Int, Int> {
        val clean = timeStr.uppercase().trim()
        val isPm = clean.contains("PM")
        val isAm = clean.contains("AM")
        val digits = clean.replace("AM", "").replace("PM", "").trim()
        val parts = digits.split(":")

        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }
}
