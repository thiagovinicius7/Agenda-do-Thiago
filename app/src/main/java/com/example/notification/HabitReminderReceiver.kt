package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class HabitReminderReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: "habit_default"
    val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Hábito"
    val reminderTime = intent.getStringExtra(EXTRA_REMINDER_TIME) ?: "08:00"

    showNotification(context, habitId, habitName)

    // Reschedule for next day so notification fires daily at the chosen time
    HabitNotificationScheduler.scheduleHabitReminder(
      context = context,
      habitId = habitId,
      habitName = habitName,
      reminderTime = reminderTime
    )
  }

  private fun showNotification(context: Context, habitId: String, habitName: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = CHANNEL_ID

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelId,
        "Lembretes de Hábitos",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notificações diárias para realização de hábitos e metas"
        enableLights(true)
        enableVibration(true)
      }
      notificationManager.createNotificationChannel(channel)
    }

    val openIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("open_habit_id", habitId)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      habitId.hashCode(),
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Bloco · Hora de: $habitName")
      .setContentText("Mantenha sua constância ativa! Abra para registrar o dia de hoje.")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify(habitId.hashCode(), notification)
  }

  companion object {
    const val CHANNEL_ID = "bloco_habit_reminders"
    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_HABIT_NAME = "extra_habit_name"
    const val EXTRA_REMINDER_TIME = "extra_reminder_time"
  }
}
