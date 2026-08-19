package com.example.core.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MediBridgeApp
import com.example.R

class MedicineAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "medibridge_medicine_alarms"
        const val CHANNEL_NAME = "Medicine Dosage Alarms"
        const val EXTRA_MEDICINE_NAME = "extra_med_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Prescribed Medicine"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: "1 Dose"

        // 1. Start continuous audio alarm & vibration
        MedicineAlarmPlayer.startRinging(context, medicineName, dosage)

        // 2. Play speech announcement if TTS manager is available
        try {
            val app = context.applicationContext as? MediBridgeApp
            app?.ttsManager?.speak("Time to take your medicine: $medicineName, $dosage", "en")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Show Heads-up High Priority Notification
        showAlarmNotification(context, medicineName, dosage)
    }

    private fun showAlarmNotification(context: Context, medicineName: String, dosage: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent reminder alarms for taking medicines on schedule"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            medicineName.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Medicine Alarm: $medicineName")
            .setContentText("It is time to take your dose ($dosage).")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(medicineName.hashCode(), notification)
    }
}
