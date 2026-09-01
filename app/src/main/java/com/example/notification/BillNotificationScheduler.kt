package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.Bill
import com.example.util.BillCalculations
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

object BillNotificationScheduler {

  fun scheduleBillReminder(
    context: Context,
    bill: Bill
  ) {
    if (bill.reminderTime.equals("Desativado", ignoreCase = true) || bill.reminderDaysBefore < 0) {
      cancelBillReminder(context, bill.id)
      return
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val nextDueDate = BillCalculations.getDueDateForPeriod(bill, LocalDate.now())
    val reminderDate = nextDueDate.minusDays(bill.reminderDaysBefore.toLong())

    val (hour, minute) = parseHourMinute(bill.reminderTime)

    val calendar = Calendar.getInstance().apply {
      set(Calendar.YEAR, reminderDate.year)
      set(Calendar.MONTH, reminderDate.monthValue - 1)
      set(Calendar.DAY_OF_MONTH, reminderDate.dayOfMonth)
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    // If already passed for current due date, compute for following cycle
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
      val followingDueDate = BillCalculations.getDueDateForPeriod(bill, nextDueDate.plusDays(1))
      val nextReminderDate = followingDueDate.minusDays(bill.reminderDaysBefore.toLong())
      calendar.apply {
        set(Calendar.YEAR, nextReminderDate.year)
        set(Calendar.MONTH, nextReminderDate.monthValue - 1)
        set(Calendar.DAY_OF_MONTH, nextReminderDate.dayOfMonth)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
    }

    val dueInfoText = when (bill.reminderDaysBefore) {
      0 -> "Vence hoje"
      1 -> "Vence amanhã"
      else -> "Vence em ${bill.reminderDaysBefore} dias (${nextDueDate.dayOfMonth}/${nextDueDate.monthValue})"
    }

    val amountFormatted = if (bill.amount > 0) BillCalculations.formatCurrency(bill.amount) else ""

    val intent = Intent(context, BillReminderReceiver::class.java).apply {
      putExtra(BillReminderReceiver.EXTRA_BILL_ID, bill.id)
      putExtra(BillReminderReceiver.EXTRA_BILL_TITLE, bill.title)
      putExtra(BillReminderReceiver.EXTRA_BILL_AMOUNT, amountFormatted)
      putExtra(BillReminderReceiver.EXTRA_DUE_INFO, dueInfoText)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      ("bill_${bill.id}").hashCode(),
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
    } catch (_: Exception) {
      try {
        alarmManager.set(
          AlarmManager.RTC_WAKEUP,
          calendar.timeInMillis,
          pendingIntent
        )
      } catch (_: Exception) {}
    }
  }

  fun sendTestNotificationNow(
    context: Context,
    billTitle: String,
    amount: String
  ) {
    val intent = Intent(context, BillReminderReceiver::class.java).apply {
      putExtra(BillReminderReceiver.EXTRA_BILL_ID, "test_${System.currentTimeMillis()}")
      putExtra(BillReminderReceiver.EXTRA_BILL_TITLE, billTitle)
      putExtra(BillReminderReceiver.EXTRA_BILL_AMOUNT, amount)
      putExtra(BillReminderReceiver.EXTRA_DUE_INFO, "Vence hoje (Teste de notificação)")
    }
    context.sendBroadcast(intent)
  }

  fun cancelBillReminder(context: Context, billId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, BillReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      ("bill_$billId").hashCode(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  private fun parseHourMinute(timeStr: String): Pair<Int, Int> {
    return try {
      val parts = timeStr.trim().split(":")
      val h = parts[0].toInt().coerceIn(0, 23)
      val m = if (parts.size > 1) parts[1].toInt().coerceIn(0, 59) else 0
      Pair(h, m)
    } catch (_: Exception) {
      Pair(9, 0)
    }
  }
}
