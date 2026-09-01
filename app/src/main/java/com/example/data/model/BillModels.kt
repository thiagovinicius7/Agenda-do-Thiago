package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

enum class BillRepeatType {
  MENSAL,
  SEMANAL,
  QUINZENAL,
  ANUAL,
  LIVRE
}

enum class BillStatus {
  PAGA,
  VENCE_HOJE,
  ATRASADA,
  PENDENTE
}

@Entity(tableName = "bills")
data class Bill(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val title: String,
  val amount: Double = 0.0,
  val isVariableAmount: Boolean = false,
  val category: String = "Geral",
  val repeatType: BillRepeatType = BillRepeatType.MENSAL,
  val dueDayOfMonth: Int = 10,
  val dueDayOfWeek: Int = 1, // 1 = Monday, 7 = Sunday
  val startDateEpochDay: Long = LocalDate.now().toEpochDay(),
  val customIntervalDays: Int = 30,
  val reminderDaysBefore: Int = 1, // 0 = no dia, 1 = 1 dia antes, 2 = 2 dias antes, 3 = 3 dias antes, 7 = 1 semana antes, -1 = desativado
  val reminderTime: String = "09:00",
  val notes: String = "",
  val barcode: String = "",
  val isArchived: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bill_payments")
data class BillPayment(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val billId: String,
  val cycleKey: String,
  val dueDateEpochDay: Long,
  val paidDateEpochDay: Long = LocalDate.now().toEpochDay(),
  val paidAmount: Double = 0.0,
  val isPaid: Boolean = true,
  val notes: String = ""
)

data class BillWithStatus(
  val bill: Bill,
  val nextDueDate: LocalDate,
  val isPaidForCurrentCycle: Boolean,
  val paidDateEpochDay: Long?,
  val status: BillStatus,
  val cycleKey: String,
  val daysUntilDue: Long
)
