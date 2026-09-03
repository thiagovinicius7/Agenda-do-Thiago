package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.BlocoDatabase
import com.example.data.model.BillPayment
import com.example.data.model.HabitMark
import com.example.data.model.HabitMarkStatus
import com.example.util.BillCalculations
import com.example.util.HabitCalculations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class BlocoTodayWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        for (appWidgetId in appWidgetIds) {
          updateWidgetSync(context, appWidgetManager, appWidgetId)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        pendingResult.finish()
      }
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)

    when (intent.action) {
      ACTION_UPDATE_ALL, ACTION_REFRESH, AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
          try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
              ComponentName(context, BlocoTodayWidgetProvider::class.java)
            )
            for (id in ids) {
              updateWidgetSync(context, appWidgetManager, id)
            }
          } catch (e: Exception) {
            e.printStackTrace()
          } finally {
            pendingResult.finish()
          }
        }
      }

      ACTION_TOGGLE_HABIT -> {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val todayEpoch = LocalDate.now().toEpochDay()
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
          try {
            val database = BlocoDatabase.getDatabase(context)
            val marks = database.habitDao().getAllHabitMarks().first()
            val existing = marks.find { it.habitId == habitId && it.dateEpochDay == todayEpoch }
            if (existing != null) {
              database.habitDao().deleteMark(habitId, todayEpoch)
            } else {
              database.habitDao().insertMark(
                HabitMark(habitId = habitId, dateEpochDay = todayEpoch, status = HabitMarkStatus.DONE)
              )
            }
            updateAllWidgets(context)
          } catch (e: Exception) {
            e.printStackTrace()
          } finally {
            pendingResult.finish()
          }
        }
      }

      ACTION_TOGGLE_BILL -> {
        val billId = intent.getStringExtra(EXTRA_BILL_ID) ?: return
        val cycleKey = intent.getStringExtra(EXTRA_CYCLE_KEY) ?: return
        val dueEpoch = intent.getLongExtra(EXTRA_DUE_EPOCH, LocalDate.now().toEpochDay())
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
          try {
            val database = BlocoDatabase.getDatabase(context)
            val payments = database.billDao().getAllPayments().first()
            val existing = payments.find { it.billId == billId && it.cycleKey == cycleKey }
            if (existing != null) {
              database.billDao().deletePayment(billId, cycleKey)
            } else {
              database.billDao().insertPayment(
                BillPayment(
                  billId = billId,
                  cycleKey = cycleKey,
                  dueDateEpochDay = dueEpoch,
                  paidDateEpochDay = LocalDate.now().toEpochDay(),
                  paidAmount = amount,
                  isPaid = true
                )
              )
            }
            updateAllWidgets(context)
          } catch (e: Exception) {
            e.printStackTrace()
          } finally {
            pendingResult.finish()
          }
        }
      }
    }
  }

  companion object {
    const val ACTION_UPDATE_ALL = "com.example.widget.ACTION_UPDATE_ALL"
    const val ACTION_REFRESH = "com.example.widget.ACTION_REFRESH"
    const val ACTION_TOGGLE_HABIT = "com.example.widget.ACTION_TOGGLE_HABIT"
    const val ACTION_TOGGLE_BILL = "com.example.widget.ACTION_TOGGLE_BILL"

    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_BILL_ID = "extra_bill_id"
    const val EXTRA_CYCLE_KEY = "extra_cycle_key"
    const val EXTRA_DUE_EPOCH = "extra_due_epoch"
    const val EXTRA_AMOUNT = "extra_amount"

    fun updateAllWidgets(context: Context) {
      try {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(
          ComponentName(context, BlocoTodayWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
          CoroutineScope(Dispatchers.IO).launch {
            for (id in ids) {
              updateWidgetSync(context, appWidgetManager, id)
            }
          }
        }
        val intent = Intent(context, BlocoTodayWidgetProvider::class.java).apply {
          action = ACTION_UPDATE_ALL
        }
        context.sendBroadcast(intent)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    suspend fun updateWidgetSync(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
    ) {
      try {
        val database = BlocoDatabase.getDatabase(context)
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val ptBr = Locale("pt", "BR")
        val dateFmt = DateTimeFormatter.ofPattern("EEE, d 'de' MMM", ptBr)
        val formattedDate = today.format(dateFmt).uppercase(ptBr)

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_today)
        remoteViews.setTextViewText(R.id.widget_title, "BLOCO · HOJE")
        remoteViews.setTextViewText(R.id.widget_date, formattedDate)

        // 1. Root & Header click opens App to Hoje
        val openHojeIntent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          putExtra("open_section", "HOJE")
        }
        val pendingOpenHoje = PendingIntent.getActivity(
          context,
          appWidgetId * 10 + 1,
          openHojeIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingOpenHoje)
        remoteViews.setOnClickPendingIntent(R.id.header_title_container, pendingOpenHoje)
        remoteViews.setOnClickPendingIntent(R.id.tv_empty, pendingOpenHoje)

        // 2. Refresh Button click
        val refreshIntent = Intent(context, BlocoTodayWidgetProvider::class.java).apply {
          action = ACTION_REFRESH
        }
        val pendingRefresh = PendingIntent.getBroadcast(
          context,
          appWidgetId * 10 + 2,
          refreshIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.btn_refresh, pendingRefresh)

        // 3. "＋ NOTA" Button click -> directly opens Mural to create new note
        val insertNoteIntent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          putExtra("open_action", "create_note")
        }
        val pendingInsertNote = PendingIntent.getActivity(
          context,
          appWidgetId * 10 + 3,
          insertNoteIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.btn_insert_note, pendingInsertNote)

        // Clear items container
        remoteViews.removeAllViews(R.id.items_container)

        // Fetch Data from local database
        val habits = database.habitDao().getActiveHabits().first()
        val marks = database.habitDao().getAllHabitMarks().first()
        val marksByHabit = marks.groupBy { it.habitId }

        val bills = database.billDao().getAllActiveBills().first()
        val payments = database.billDao().getAllPayments().first()
        val paymentsByBill = payments.groupBy { it.billId }

        val events = database.calendarDao().getAllEvents().first()
        val calendars = database.calendarDao().getAllCalendars().first()
        val selectedCalIds = calendars.filter { it.isSelected }.map { it.id }.toSet()

        // Filter Habits scheduled for today according to their recurrence rule
        val todayHabits = habits.filter { habit ->
          val started = todayEpoch >= habit.startDateEpochDay
          val notEnded = (habit.durationDays <= 0) || (todayEpoch < (habit.startDateEpochDay + habit.durationDays))
          val inRule = HabitCalculations.isDateInRule(habit, todayEpoch)
          val notPaused = !HabitCalculations.isDatePaused(habit, todayEpoch)
          started && notEnded && inRule && notPaused
        }

        // Filter Bills: include bills due today, overdue, or pending
        val todayBills = bills.map { bill ->
          BillCalculations.computeBillStatus(bill, paymentsByBill[bill.id] ?: emptyList(), today)
        }.filter { billStatus ->
          !billStatus.isPaidForCurrentCycle || billStatus.nextDueDate == today
        }.take(5)

        // Filter Events occurring today
        val todayEvents = events.filter { event ->
          if (calendars.isNotEmpty() && event.calendarId.isNotEmpty() && event.calendarId !in selectedCalIds) {
            false
          } else {
            val eventDate = Instant.ofEpochMilli(event.startEpochMillis)
              .atZone(ZoneId.systemDefault())
              .toLocalDate()
            eventDate == today
          }
        }.sortedBy { it.startEpochMillis }.take(4)

        var itemCount = 0

        // A) Habits Section
        if (todayHabits.isNotEmpty()) {
          val sectionView = RemoteViews(context.packageName, R.layout.widget_section_header)
          sectionView.setTextViewText(R.id.tv_section_title, "⚡ HÁBITOS DE HOJE (${todayHabits.size})")
          remoteViews.addView(R.id.items_container, sectionView)

          for (habit in todayHabits) {
            itemCount++
            val habitMarks = marksByHabit[habit.id] ?: emptyList()
            val isDone = habitMarks.any { it.dateEpochDay == todayEpoch && it.status == HabitMarkStatus.DONE }
            val calc = HabitCalculations.calculate(habit, habitMarks, todayEpoch)

            val habitRow = RemoteViews(context.packageName, R.layout.widget_item_habit)
            habitRow.setTextViewText(R.id.tv_habit_name, habit.name)
            habitRow.setTextViewText(
              R.id.tv_habit_info,
              if (isDone) "Concluído hoje ✦ Seq: ${calc.currentStreak}d" else "Pendente para hoje · Seq: ${calc.currentStreak}d"
            )

            // Checkbox icon
            habitRow.setImageViewResource(
              R.id.btn_check_habit,
              if (isDone) R.drawable.widget_check_on else R.drawable.widget_check_off
            )

            // Checkbox click action
            val toggleIntent = Intent(context, BlocoTodayWidgetProvider::class.java).apply {
              action = ACTION_TOGGLE_HABIT
              putExtra(EXTRA_HABIT_ID, habit.id)
            }
            val pendingToggle = PendingIntent.getBroadcast(
              context,
              habit.id.hashCode(),
              toggleIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            habitRow.setOnClickPendingIntent(R.id.btn_check_habit, pendingToggle)

            // Content click opens habit in app
            val openHabitIntent = Intent(context, MainActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
              putExtra("open_habit_id", habit.id)
            }
            val pendingOpenHabit = PendingIntent.getActivity(
              context,
              ("open_h_${habit.id}").hashCode(),
              openHabitIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            habitRow.setOnClickPendingIntent(R.id.habit_content, pendingOpenHabit)

            remoteViews.addView(R.id.items_container, habitRow)
          }
        }

        // B) Bills Section
        if (todayBills.isNotEmpty()) {
          val sectionView = RemoteViews(context.packageName, R.layout.widget_section_header)
          sectionView.setTextViewText(R.id.tv_section_title, "💳 CONTAS (${todayBills.size})")
          remoteViews.addView(R.id.items_container, sectionView)

          for (billStatus in todayBills) {
            itemCount++
            val bill = billStatus.bill
            val isPaid = billStatus.isPaidForCurrentCycle
            val dueDate = billStatus.nextDueDate
            val cycleKey = billStatus.cycleKey

            val billRow = RemoteViews(context.packageName, R.layout.widget_item_bill)
            billRow.setTextViewText(R.id.tv_bill_title, bill.title)

            val amountText = if (bill.amount > 0) BillCalculations.formatCurrency(bill.amount) else "Sem valor"
            val statusText = when {
              isPaid -> "Paga ✦ $amountText"
              billStatus.daysUntilDue < 0 -> "Atrasada (${-billStatus.daysUntilDue}d) · $amountText"
              billStatus.daysUntilDue == 0L -> "Vence hoje · $amountText"
              else -> "Vence em ${billStatus.daysUntilDue}d · $amountText"
            }
            billRow.setTextViewText(R.id.tv_bill_info, statusText)

            // Checkbox icon
            billRow.setImageViewResource(
              R.id.btn_check_bill,
              if (isPaid) R.drawable.widget_check_on else R.drawable.widget_check_off
            )

            // Checkbox click action
            val toggleBillIntent = Intent(context, BlocoTodayWidgetProvider::class.java).apply {
              action = ACTION_TOGGLE_BILL
              putExtra(EXTRA_BILL_ID, bill.id)
              putExtra(EXTRA_CYCLE_KEY, cycleKey)
              putExtra(EXTRA_DUE_EPOCH, dueDate.toEpochDay())
              putExtra(EXTRA_AMOUNT, bill.amount)
            }
            val pendingToggleBill = PendingIntent.getBroadcast(
              context,
              ("toggle_bill_${bill.id}_$cycleKey").hashCode(),
              toggleBillIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            billRow.setOnClickPendingIntent(R.id.btn_check_bill, pendingToggleBill)

            // Content click opens contas in app
            val openBillIntent = Intent(context, MainActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
              putExtra("open_section", "CONTAS")
              putExtra("open_bill_id", bill.id)
            }
            val pendingOpenBill = PendingIntent.getActivity(
              context,
              ("open_bill_${bill.id}").hashCode(),
              openBillIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            billRow.setOnClickPendingIntent(R.id.bill_content, pendingOpenBill)

            remoteViews.addView(R.id.items_container, billRow)
          }
        }

        // C) Events Section
        if (todayEvents.isNotEmpty()) {
          val sectionView = RemoteViews(context.packageName, R.layout.widget_section_header)
          sectionView.setTextViewText(R.id.tv_section_title, "📅 AGENDA DE HOJE (${todayEvents.size})")
          remoteViews.addView(R.id.items_container, sectionView)

          val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
          for (event in todayEvents) {
            itemCount++
            val eventRow = RemoteViews(context.packageName, R.layout.widget_item_event)
            val startZdt = Instant.ofEpochMilli(event.startEpochMillis).atZone(ZoneId.systemDefault())
            val timeStr = startZdt.format(timeFmt)

            eventRow.setTextViewText(R.id.tv_event_time, timeStr)
            eventRow.setTextViewText(R.id.tv_event_title, event.title)

            // Content click opens agenda in app
            val openAgendaIntent = Intent(context, MainActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
              putExtra("open_section", "AGENDA")
            }
            val pendingOpenAgenda = PendingIntent.getActivity(
              context,
              ("open_ev_${event.id}").hashCode(),
              openAgendaIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            eventRow.setOnClickPendingIntent(R.id.event_row, pendingOpenAgenda)

            remoteViews.addView(R.id.items_container, eventRow)
          }
        }

        // Empty state visibility
        if (itemCount == 0) {
          remoteViews.setViewVisibility(R.id.tv_empty, View.VISIBLE)
          remoteViews.setTextViewText(
            R.id.tv_empty,
            "Tudo em dia para hoje! ✦\nToque para abrir o Bloco T ou criar uma nova nota."
          )
        } else {
          remoteViews.setViewVisibility(R.id.tv_empty, View.GONE)
        }

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
      } catch (e: Exception) {
        e.printStackTrace()
        // Fallback UI to prevent "Problem loading widget" error
        try {
          val fallbackViews = RemoteViews(context.packageName, R.layout.widget_today)
          fallbackViews.setTextViewText(R.id.widget_title, "BLOCO · HOJE")
          fallbackViews.setTextViewText(R.id.widget_date, "Hoje")
          fallbackViews.setViewVisibility(R.id.tv_empty, View.VISIBLE)
          fallbackViews.setTextViewText(R.id.tv_empty, "Toque para abrir o Bloco T.")
          val openHojeIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_section", "HOJE")
          }
          val pendingOpenHoje = PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 1,
            openHojeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          fallbackViews.setOnClickPendingIntent(R.id.widget_root, pendingOpenHoje)
          appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
        } catch (_: Exception) {}
      }
    }
  }
}
