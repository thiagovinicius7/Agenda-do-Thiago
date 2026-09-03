package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.BlocoDatabase
import com.example.widget.BlocoTodayWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
      val pendingResult = goAsync()
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val database = BlocoDatabase.getDatabase(context)

          // 1. Reschedule Habits
          val habits = database.habitDao().getActiveHabits().first()
          for (habit in habits) {
            if (habit.reminderTime.isNotBlank() && habit.reminderTime != "Desativado") {
              HabitNotificationScheduler.scheduleHabitReminder(
                context = context,
                habitId = habit.id,
                habitName = habit.name,
                reminderTime = habit.reminderTime
              )
            }
          }

          // 2. Reschedule Bills
          val bills = database.billDao().getAllActiveBills().first()
          for (bill in bills) {
            if (bill.reminderTime.isNotBlank() && bill.reminderTime != "Desativado" && bill.reminderDaysBefore >= 0) {
              BillNotificationScheduler.scheduleBillReminder(context, bill)
            }
          }

          // 3. Refresh Widget
          BlocoTodayWidgetProvider.updateAllWidgets(context)
        } catch (e: Exception) {
          e.printStackTrace()
        } finally {
          pendingResult.finish()
        }
      }
    }
  }
}
