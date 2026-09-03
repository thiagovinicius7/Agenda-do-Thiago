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

class BillReminderReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val billId = intent.getStringExtra(EXTRA_BILL_ID) ?: "bill_default"
    val billTitle = intent.getStringExtra(EXTRA_BILL_TITLE) ?: "Conta a Pagar"
    val billAmount = intent.getStringExtra(EXTRA_BILL_AMOUNT) ?: ""
    val dueInfo = intent.getStringExtra(EXTRA_DUE_INFO) ?: "Vencimento próximo"

    showNotification(context, billId, billTitle, billAmount, dueInfo)
  }

  private fun showNotification(
    context: Context,
    billId: String,
    billTitle: String,
    billAmount: String,
    dueInfo: String
  ) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = CHANNEL_ID

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelId,
        "Lembretes de Contas a Pagar",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notificações de vencimento de contas fixas e variáveis"
        enableLights(true)
        enableVibration(true)
        setShowBadge(true)
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
      }
      notificationManager.createNotificationChannel(channel)
    }

    val openIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("open_section", "CONTAS")
      putExtra("open_bill_id", billId)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      ("bill_$billId").hashCode(),
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val bodyText = if (billAmount.isNotBlank()) {
      "Valor: $billAmount · $dueInfo. Abra para conferir ou dar baixa."
    } else {
      "$dueInfo. Abra para conferir ou dar baixa."
    }

    val notification = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Bloco · Vencimento: $billTitle")
      .setContentText(bodyText)
      .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    try {
      notificationManager.notify(("bill_$billId").hashCode(), notification)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  companion object {
    const val CHANNEL_ID = "bloco_bill_reminders"
    const val EXTRA_BILL_ID = "extra_bill_id"
    const val EXTRA_BILL_TITLE = "extra_bill_title"
    const val EXTRA_BILL_AMOUNT = "extra_bill_amount"
    const val EXTRA_DUE_INFO = "extra_due_info"
  }
}
