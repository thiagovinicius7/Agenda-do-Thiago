package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Bill
import com.example.data.model.BillPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

  @Query("SELECT * FROM bills WHERE isArchived = 0 ORDER BY dueDayOfMonth ASC, createdAt ASC")
  fun getAllActiveBills(): Flow<List<Bill>>

  @Query("SELECT * FROM bills ORDER BY createdAt ASC")
  fun getAllBills(): Flow<List<Bill>>

  @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
  suspend fun getBillById(id: String): Bill?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBill(bill: Bill)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBills(bills: List<Bill>)

  @Update
  suspend fun updateBill(bill: Bill)

  @Query("DELETE FROM bills WHERE id = :id")
  suspend fun deleteBillById(id: String)

  @Query("SELECT * FROM bill_payments")
  fun getAllPayments(): Flow<List<BillPayment>>

  @Query("SELECT * FROM bill_payments WHERE billId = :billId")
  fun getPaymentsForBill(billId: String): Flow<List<BillPayment>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: BillPayment)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayments(payments: List<BillPayment>)

  @Query("DELETE FROM bill_payments WHERE billId = :billId AND cycleKey = :cycleKey")
  suspend fun deletePayment(billId: String, cycleKey: String)

  @Query("DELETE FROM bill_payments WHERE billId = :billId")
  suspend fun deletePaymentsForBill(billId: String)

  @Query("DELETE FROM bills")
  suspend fun deleteAllBills()

  @Query("DELETE FROM bill_payments")
  suspend fun deleteAllPayments()
}
