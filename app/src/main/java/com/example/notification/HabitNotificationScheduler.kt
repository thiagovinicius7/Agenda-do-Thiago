package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object HabitNotificationScheduler {

  fun scheduleHabitReminder(
    context: Context,
    habitId: String,
    habitName: String,
    reminderTime: String
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val (hour, minute) = parseHourMinute(reminderTime)

    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)

      // If the time has already passed today, schedule for tomorrow
      if (timeInMillis <= System.currentTimeMillis()) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, HabitReminderReceiver::class.java).apply {
      putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
      putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
      putExtra(HabitReminderReceiver.EXTRA_REMINDER_TIME, reminderTime)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      habitId.hashCode(),
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
        alarmManager.set(
          AlarmManager.RTC_WAKEUP,
          calendar.timeInMillis,
          pendingIntent
        )
      }
    } catch (e: SecurityException) {
      // In case exact alarm permission is restricted, fallback to inexact repeating
      alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
      )
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun cancelHabitReminder(context: Context, habitId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, HabitReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      habitId.hashCode(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  fun sendTestNotificationNow(context: Context, habitName: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = HabitReminderReceiver.CHANNEL_ID

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
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      System.currentTimeMillis().toInt(),
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Bloco · Lembrete de $habitName")
      .setContentText("Notificação configurada com sucesso! Você receberá os avisos no horário escolhido.")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
  }

  private fun parseHourMinute(timeString: String): Pair<Int, Int> {
    val parts = timeString.split(":").mapNotNull { it.trim().toIntOrNull() }
    val hour = parts.getOrNull(0)?.coerceIn(0, 23) ?: 8
    val minute = parts.getOrNull(1)?.coerceIn(0, 59) ?: 0
    return Pair(hour, minute)
  }
}
