package com.example.util

import com.example.data.model.Bill
import com.example.data.model.BillPayment
import com.example.data.model.BillRepeatType
import com.example.data.model.BillStatus
import com.example.data.model.BillWithStatus
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object BillCalculations {

  fun formatCurrency(amount: Double): String {
    val ptBr = Locale("pt", "BR")
    val formatter = NumberFormat.getCurrencyInstance(ptBr)
    return formatter.format(amount)
  }

  fun getDueDateForPeriod(bill: Bill, referenceDate: LocalDate = LocalDate.now()): LocalDate {
    val startDate = LocalDate.ofEpochDay(bill.startDateEpochDay)

    return when (bill.repeatType) {
      BillRepeatType.MENSAL -> {
        val year = referenceDate.year
        val month = referenceDate.monthValue
        val maxDays = referenceDate.lengthOfMonth()
        val day = bill.dueDayOfMonth.coerceIn(1, maxDays)
        val calculated = LocalDate.of(year, month, day)
        if (calculated.isBefore(startDate)) startDate else calculated
      }
      BillRepeatType.SEMANAL -> {
        val targetDayOfWeek = DayOfWeek.of(bill.dueDayOfWeek.coerceIn(1, 7))
        val startOfWeek = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekTarget = startOfWeek.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
        if (currentWeekTarget.isBefore(startDate)) {
          startDate.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
        } else {
          currentWeekTarget
        }
      }
      BillRepeatType.QUINZENAL -> {
        if (referenceDate.isBefore(startDate)) {
          startDate
        } else {
          val daysDiff = ChronoUnit.DAYS.between(startDate, referenceDate)
          val intervals = daysDiff / 15
          val currentCycleDate = startDate.plusDays(intervals * 15)
          if (currentCycleDate.isBefore(referenceDate) && ChronoUnit.DAYS.between(currentCycleDate, referenceDate) > 7) {
            startDate.plusDays((intervals + 1) * 15)
          } else {
            currentCycleDate
          }
        }
      }
      BillRepeatType.ANUAL -> {
        val calculated = startDate.withYear(referenceDate.year)
        calculated
      }
      BillRepeatType.LIVRE -> {
        if (bill.customIntervalDays > 0 && !referenceDate.isBefore(startDate)) {
          val daysDiff = ChronoUnit.DAYS.between(startDate, referenceDate)
          val intervals = daysDiff / bill.customIntervalDays
          startDate.plusDays(intervals * bill.customIntervalDays)
        } else {
          startDate
        }
      }
    }
  }

  fun getCycleKey(bill: Bill, dueDate: LocalDate): String {
    return when (bill.repeatType) {
      BillRepeatType.MENSAL -> "${dueDate.year}-${dueDate.monthValue.toString().padStart(2, '0')}"
      BillRepeatType.SEMANAL -> "${dueDate.year}-W${dueDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
      BillRepeatType.QUINZENAL -> "${bill.id}_${dueDate}"
      BillRepeatType.ANUAL -> "${dueDate.year}"
      BillRepeatType.LIVRE -> "${bill.id}_${dueDate}"
    }
  }

  fun computeBillStatus(
    bill: Bill,
    payments: List<BillPayment>,
    today: LocalDate = LocalDate.now(),
    referenceMonth: LocalDate = today
  ): BillWithStatus {
    val dueDate = getDueDateForPeriod(bill, referenceMonth)
    val cycleKey = getCycleKey(bill, dueDate)

    val payment = payments.find { it.billId == bill.id && it.cycleKey == cycleKey && it.isPaid }
    val isPaid = payment != null
    val paidEpoch = payment?.paidDateEpochDay

    val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate)

    val status = when {
      isPaid -> BillStatus.PAGA
      dueDate.isEqual(today) -> BillStatus.VENCE_HOJE
      dueDate.isBefore(today) -> BillStatus.ATRASADA
      else -> BillStatus.PENDENTE
    }

    return BillWithStatus(
      bill = bill,
      nextDueDate = dueDate,
      isPaidForCurrentCycle = isPaid,
      paidDateEpochDay = paidEpoch,
      status = status,
      cycleKey = cycleKey,
      daysUntilDue = daysUntilDue
    )
  }

  fun formatRepeatLabel(bill: Bill): String {
    return when (bill.repeatType) {
      BillRepeatType.MENSAL -> "Mensal · Dia ${bill.dueDayOfMonth}"
      BillRepeatType.SEMANAL -> {
        val dayName = when (bill.dueDayOfWeek) {
          1 -> "Segunda"
          2 -> "Terça"
          3 -> "Quarta"
          4 -> "Quinta"
          5 -> "Sexta"
          6 -> "Sábado"
          else -> "Domingo"
        }
        "Semanal · $dayName"
      }
      BillRepeatType.QUINZENAL -> "Quinzenal (15 dias)"
      BillRepeatType.ANUAL -> "Anual"
      BillRepeatType.LIVRE -> {
        if (bill.customIntervalDays > 0) "A cada ${bill.customIntervalDays} dias" else "Data Única"
      }
    }
  }
}
