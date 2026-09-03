package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
          )
        } else {
          alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
          )
        }
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
    } catch (e: Exception) {
      try {
        alarmManager.set(
          AlarmManager.RTC_WAKEUP,
          calendar.timeInMillis,
          pendingIntent
        )
      } catch (ex: Exception) {
        ex.printStackTrace()
      }
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
    // Check system notification permission
    val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val hasPostPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }

    if (!areEnabled || !hasPostPermission) {
      Toast.makeText(
        context,
        "Permissão de notificação necessária. Ative nas configurações do aparelho.",
        Toast.LENGTH_LONG
      ).show()
    }

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
        setShowBadge(true)
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
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
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Bloco · Lembrete de $habitName")
      .setContentText("Notificação configurada com sucesso! Você receberá os avisos no horário escolhido.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Notificação de teste recebida com sucesso! Você receberá os avisos de $habitName no horário configurado."
        )
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    try {
      notificationManager.notify(System.currentTimeMillis().toInt(), notification)
      Toast.makeText(context, "Notificação de teste enviada!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Erro ao enviar notificação: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  private fun parseHourMinute(timeString: String): Pair<Int, Int> {
    val parts = timeString.split(":").mapNotNull { it.trim().toIntOrNull() }
    val hour = parts.getOrNull(0)?.coerceIn(0, 23) ?: 8
    val minute = parts.getOrNull(1)?.coerceIn(0, 59) ?: 0
    return Pair(hour, minute)
  }
}
