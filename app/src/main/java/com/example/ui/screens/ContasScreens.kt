package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.data.model.BillRepeatType
import com.example.data.model.BillWithStatus
import com.example.ui.components.ModernistCheckbox
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import com.example.util.BillCalculations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ContasScreen(
  bills: List<BillWithStatus>,
  currentCategoryFilter: String,
  onSelectCategoryFilter: (String) -> Unit,
  onOpenBill: (String) -> Unit,
  onCreateBill: () -> Unit,
  onTogglePayment: (BillWithStatus) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Filter bills
  val filteredBills = remember(bills, currentCategoryFilter) {
    if (currentCategoryFilter == "todas") {
      bills
    } else {
      bills.filter { it.bill.category.equals(currentCategoryFilter, ignoreCase = true) }
    }
  }

  // Calculate totals
  val pendingBills = bills.filter { !it.isPaidForCurrentCycle }
  val pendingAmount = pendingBills.sumOf { it.bill.amount }
  val paidBills = bills.filter { it.isPaidForCurrentCycle }
  val paidAmount = paidBills.sumOf { it.bill.amount }
  val overdueCount = bills.count { !it.isPaidForCurrentCycle && it.daysUntilDue < 0 }

  // Unique categories
  val categories = remember(bills) {
    val set = linkedSetOf("todas", "Moradia", "Serviços", "Finanças", "Assinaturas", "Outros")
    bills.forEach { if (it.bill.category.isNotBlank()) set.add(it.bill.category) }
    set.toList()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
  ) {
    // Header Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Contas a Pagar",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 30.sp,
          letterSpacing = (-0.02).sp,
          color = colors.text
        )
        Text(
          text = "Controle de despesas fixas e recorrentes",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Normal,
          fontSize = 12.sp,
          color = colors.textSecondary
        )
      }

      Box(
        modifier = Modifier
          .border(1.5.dp, colors.accent, RectangleShape)
          .background(colors.accent)
          .clickable(onClick = onCreateBill)
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .testTag("btn_nova_conta"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "+ NOVA CONTA",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          color = Color.White
        )
      }
    }

    Ruler2dp()

    // Summary Statistics Cards (Brutalist Grid)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.surface)
    ) {
      // Pending / A Pagar
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(14.dp)
      ) {
        Text(
          text = "A PAGAR",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          letterSpacing = 0.8.sp,
          color = if (overdueCount > 0) colors.accent else colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = BillCalculations.formatCurrency(pendingAmount),
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 18.sp,
          color = colors.text
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${pendingBills.size} pendente(s)",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = colors.textTertiary
          )
          if (overdueCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .background(colors.accent)
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = "$overdueCount VENCIDA",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color.White
              )
            }
          }
        }
      }

      Box(
        modifier = Modifier
          .width(1.dp)
          .height(72.dp)
          .background(colors.rulerWeak)
      )

      // Paid / Pago
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(14.dp)
      ) {
        Text(
          text = "PAGO NO CICLO",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          letterSpacing = 0.8.sp,
          color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = BillCalculations.formatCurrency(paidAmount),
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 18.sp,
          color = colors.text
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${paidBills.size} conta(s) quitadas",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Medium,
          fontSize = 11.sp,
          color = colors.textTertiary
        )
      }
    }

    Ruler1dp()

    // Category Filter Pills Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.canvas)
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { cat ->
        val isSelected = currentCategoryFilter.equals(cat, ignoreCase = true)
        val count = if (cat == "todas") bills.size else bills.count { it.bill.category.equals(cat, ignoreCase = true) }
        Box(
          modifier = Modifier
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) colors.text else colors.rulerWeak,
              shape = RectangleShape
            )
            .background(if (isSelected) colors.text else Color.Transparent)
            .clickable { onSelectCategoryFilter(cat) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("filter_bill_$cat")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = cat.uppercase(Locale.ROOT),
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              letterSpacing = 0.5.sp,
              color = if (isSelected) colors.canvas else colors.text
            )
            Text(
              text = "$count",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Medium,
              fontSize = 10.sp,
              color = if (isSelected) colors.canvas.copy(alpha = 0.7f) else colors.textTertiary
            )
          }
        }
      }
    }

    Ruler1dp()

    // Bills List
    if (filteredBills.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "NENHUMA CONTA ENCONTRADA",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp,
            color = colors.textSecondary
          )
          Text(
            text = "Cadastre suas contas fixas (aluguel, água, energia, internet, cartão) para receber lembretes e acompanhar pagamentos.",
            fontFamily = ArchivoFont,
            fontSize = 12.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .border(1.dp, colors.text, RectangleShape)
              .clickable(onClick = onCreateBill)
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = "+ CADASTRAR PRIMEIRA CONTA",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = colors.text
            )
          }
        }
      }
    } else {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        filteredBills.forEachIndexed { index, billStatus ->
          BillRowItem(
            billWithStatus = billStatus,
            onTogglePayment = { onTogglePayment(billStatus) },
            onClick = { onOpenBill(billStatus.bill.id) },
            onCopyCode = { code ->
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Código de barras / PIX", code)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Código copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
            }
          )
          if (index < filteredBills.size - 1) {
            Ruler1dp()
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
private fun BillRowItem(
  billWithStatus: BillWithStatus,
  onTogglePayment: () -> Unit,
  onClick: () -> Unit,
  onCopyCode: (String) -> Unit
) {
  val colors = LocalBlocoColors.current
  val bill = billWithStatus.bill
  val isPaid = billWithStatus.isPaidForCurrentCycle
  val dueDate = billWithStatus.nextDueDate
  val daysUntil = billWithStatus.daysUntilDue

  val relativeLabel = when {
    isPaid -> "Pago"
    daysUntil == 0L -> "Vence hoje"
    daysUntil == 1L -> "Vence amanhã"
    daysUntil > 1L -> "Em $daysUntil dias"
    daysUntil == -1L -> "Atrasada (1 dia)"
    else -> "Atrasada (${-daysUntil} dias)"
  }

  val recurrenceLabel = when (bill.repeatType) {
    BillRepeatType.MENSAL -> "MENSAL · DIA ${bill.dueDayOfMonth}"
    BillRepeatType.QUINZENAL -> "QUINZENAL"
    BillRepeatType.ANUAL -> "ANUAL"
    BillRepeatType.SEMANAL -> {
      val dayName = when (bill.dueDayOfWeek) {
        1 -> "SEG"
        2 -> "TER"
        3 -> "QUA"
        4 -> "QUI"
        5 -> "SEX"
        6 -> "SÁB"
        else -> "DOM"
      }
      "SEMANAL · $dayName"
    }
    BillRepeatType.LIVRE -> "A CADA ${bill.customIntervalDays} DIAS"
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(if (isPaid) colors.surface.copy(alpha = 0.5f) else colors.canvas)
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag("bill_item_${bill.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Checkbox to mark as paid
    ModernistCheckbox(
      checked = isPaid,
      onCheckedChange = onTogglePayment,
      size = 22.dp,
      modifier = Modifier.testTag("checkbox_bill_${bill.id}")
    )

    Spacer(modifier = Modifier.width(14.dp))

    // Bill Details
    Column(
      modifier = Modifier.weight(1f)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = bill.title,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None,
          color = if (isPaid) colors.textSecondary else colors.text,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Category Tag
        Box(
          modifier = Modifier
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = bill.category.uppercase(Locale.ROOT),
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 9.sp,
            color = colors.textSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Recurrence & Reminder Info
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = recurrenceLabel,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Normal,
          fontSize = 11.sp,
          color = colors.textTertiary
        )

        if (bill.reminderTime.isNotBlank() && !bill.reminderTime.equals("Desativado", ignoreCase = true)) {
          Text(
            text = "· 🔔 ${bill.reminderDaysBefore}d antes (${bill.reminderTime})",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = colors.textTertiary
          )
        }
      }

      // Barcode / Copy affordance if present
      if (bill.barcode.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable { onCopyCode(bill.barcode) }
            .padding(vertical = 2.dp)
        ) {
          Text(
            text = "📋 Copiar código de barras / chave",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = colors.accent
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Value & Due Date
    Column(
      horizontalAlignment = Alignment.End
    ) {
      Text(
        text = if (bill.isVariableAmount) "Variável" else BillCalculations.formatCurrency(bill.amount),
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 15.sp,
        textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None,
        color = if (isPaid) colors.textSecondary else colors.text
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Relative badge
      Box(
        modifier = Modifier
          .then(
            if (isPaid) {
              Modifier
                .background(colors.surface)
                .border(1.dp, colors.rulerWeak, RectangleShape)
            } else if (daysUntil < 0) {
              Modifier.background(colors.accent)
            } else if (daysUntil == 0L) {
              Modifier.background(colors.text)
            } else {
              Modifier.border(1.dp, colors.rulerWeak, RectangleShape)
            }
          )
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = if (isPaid) "PAGO" else relativeLabel.uppercase(Locale.ROOT),
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          color = when {
            isPaid -> colors.textSecondary
            daysUntil < 0 -> Color.White
            daysUntil == 0L -> colors.canvas
            else -> colors.textSecondary
          }
        )
      }
    }
  }
}

@Composable
fun BillDetailScreen(
  billWithStatus: BillWithStatus?,
  onBack: () -> Unit,
  onSave: (
    id: String?,
    title: String,
    amount: Double,
    isVariableAmount: Boolean,
    category: String,
    repeatType: BillRepeatType,
    dueDayOfMonth: Int,
    dueDayOfWeek: Int,
    startDateEpochDay: Long,
    customIntervalDays: Int,
    reminderDaysBefore: Int,
    reminderTime: String,
    notes: String,
    barcode: String
  ) -> Unit,
  onDelete: (String) -> Unit,
  onTogglePayment: (BillWithStatus) -> Unit,
  onTestNotification: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val existingBill = billWithStatus?.bill
  val colors = LocalBlocoColors.current
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var title by remember { mutableStateOf(existingBill?.title ?: "") }
  var amountText by remember { mutableStateOf(existingBill?.amount?.let { if (it > 0) String.format(Locale.US, "%.2f", it) else "" } ?: "") }
  var isVariableAmount by remember { mutableStateOf(existingBill?.isVariableAmount ?: false) }
  var category by remember { mutableStateOf(existingBill?.category ?: "Moradia") }
  var repeatType by remember { mutableStateOf(existingBill?.repeatType ?: BillRepeatType.MENSAL) }
  var dueDayOfMonth by remember { mutableIntStateOf(existingBill?.dueDayOfMonth ?: 10) }
  var dueDayOfWeek by remember { mutableIntStateOf(existingBill?.dueDayOfWeek ?: 1) }
  var customIntervalDays by remember { mutableIntStateOf(existingBill?.customIntervalDays ?: 30) }
  var reminderDaysBefore by remember { mutableIntStateOf(existingBill?.reminderDaysBefore ?: 1) }
  var reminderTime by remember { mutableStateOf(existingBill?.reminderTime ?: "09:00") }
  var notes by remember { mutableStateOf(existingBill?.notes ?: "") }
  var barcode by remember { mutableStateOf(existingBill?.barcode ?: "") }

  val predefinedCategories = listOf("Moradia", "Serviços", "Finanças", "Assinaturas", "Saúde", "Educação", "Outros")
  val reminderAdvanceOptions = listOf(
    Pair(0, "No próprio dia"),
    Pair(1, "1 dia antes"),
    Pair(2, "2 dias antes"),
    Pair(3, "3 dias antes"),
    Pair(5, "5 dias antes"),
    Pair(7, "7 dias antes")
  )
  val reminderTimeOptions = listOf("08:00", "09:00", "12:00", "18:00", "20:00", "Desativado")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable(onClick = onBack)
          .padding(vertical = 4.dp)
      ) {
        Text(
          text = "← VOLTAR",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          letterSpacing = 0.5.sp,
          color = colors.text
        )
      }

      Text(
        text = if (existingBill != null) "EDITAR CONTA" else "NOVA CONTA",
        style = SectionLabelStyle,
        color = colors.text
      )

      if (existingBill != null) {
        Box(
          modifier = Modifier
            .clickable { onDelete(existingBill.id) }
            .padding(vertical = 4.dp)
            .testTag("btn_delete_bill")
        ) {
          Text(
            text = "EXCLUIR",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = colors.accent
          )
        }
      } else {
        Spacer(modifier = Modifier.width(48.dp))
      }
    }

    Ruler2dp()

    // Form content
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. Title Input
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "NOME DA CONTA *",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          color = colors.textSecondary
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.text, RectangleShape)
            .background(colors.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
          BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = TextStyle(
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp,
              color = colors.text
            ),
            cursorBrush = SolidColor(colors.text),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_bill_title"),
            decorationBox = { innerTextField ->
              if (title.isEmpty()) {
                Text(
                  text = "Ex: Aluguel, Internet Fibra, Energia, Cartão...",
                  fontFamily = ArchivoFont,
                  fontSize = 15.sp,
                  color = colors.textTertiary
                )
              }
              innerTextField()
            }
          )
        }
      }

      // 2. Amount & Variable Option
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "VALOR (R$)",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = colors.textSecondary
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.clickable { isVariableAmount = !isVariableAmount }
          ) {
            ModernistCheckbox(
              checked = isVariableAmount,
              onCheckedChange = { isVariableAmount = !isVariableAmount },
              size = 16.dp
            )
            Text(
              text = "Valor variável todo mês",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Medium,
              fontSize = 11.sp,
              color = colors.text
            )
          }
        }

        if (!isVariableAmount) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.5.dp, colors.text, RectangleShape)
              .background(colors.surface)
              .padding(horizontal = 14.dp, vertical = 12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "R$ ",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.textSecondary
              )
              BasicTextField(
                value = amountText,
                onValueChange = { amountText = it.replace(',', '.') },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = colors.text
                ),
                cursorBrush = SolidColor(colors.text),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_bill_amount"),
                decorationBox = { innerTextField ->
                  if (amountText.isEmpty()) {
                    Text(
                      text = "0,00",
                      fontFamily = ArchivoFont,
                      fontSize = 16.sp,
                      color = colors.textTertiary
                    )
                  }
                  innerTextField()
                }
              )
            }
          }
        }
      }

      // 3. Category Selector
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "CATEGORIA",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          color = colors.textSecondary
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          predefinedCategories.forEach { cat ->
            val isSelected = category.equals(cat, ignoreCase = true)
            Box(
              modifier = Modifier
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) colors.text else colors.rulerWeak,
                  shape = RectangleShape
                )
                .background(if (isSelected) colors.text else Color.Transparent)
                .clickable { category = cat }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = cat,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isSelected) colors.canvas else colors.text
              )
            }
          }
        }
      }

      // 4. Repetition & Due Date
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "REPETIÇÃO / VENCIMENTO",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          color = colors.textSecondary
        )

        // Repeat Type Buttons (MENSAL, QUINZENAL, SEMANAL, ANUAL, LIVRE)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            BillRepeatType.MENSAL to "MENSAL",
            BillRepeatType.QUINZENAL to "QUINZENAL",
            BillRepeatType.SEMANAL to "SEMANAL",
            BillRepeatType.LIVRE to "LIVRE"
          ).forEach { (rType, label) ->
            val isSelected = (repeatType == rType)
            Box(
              modifier = Modifier
                .weight(1f)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) colors.accent else colors.rulerWeak,
                  shape = RectangleShape
                )
                .background(if (isSelected) colors.accent else Color.Transparent)
                .clickable { repeatType = rType }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (isSelected) Color.White else colors.text
              )
            }
          }
        }

        // Sub-parameters based on repeat type
        when (repeatType) {
          BillRepeatType.MENSAL -> {
            Column(modifier = Modifier.padding(top = 6.dp)) {
              Text(
                text = "Dia de vencimento todo mês (1 a 31):",
                fontFamily = ArchivoFont,
                fontSize = 12.sp,
                color = colors.textSecondary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                (1..31).forEach { day ->
                  val isSelected = (dueDayOfMonth == day)
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) colors.text else colors.rulerWeak,
                        shape = RectangleShape
                      )
                      .background(if (isSelected) colors.text else colors.surface)
                      .clickable { dueDayOfMonth = day },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$day",
                      fontFamily = ArchivoFont,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = if (isSelected) colors.canvas else colors.text
                    )
                  }
                }
              }
            }
          }
          BillRepeatType.SEMANAL -> {
            Column(modifier = Modifier.padding(top = 6.dp)) {
              Text(
                text = "Dia da semana de vencimento:",
                fontFamily = ArchivoFont,
                fontSize = 12.sp,
                color = colors.textSecondary
              )
              Spacer(modifier = Modifier.height(6.dp))
              val daysWeek = listOf(
                Pair(1, "SEG"),
                Pair(2, "TER"),
                Pair(3, "QUA"),
                Pair(4, "QUI"),
                Pair(5, "SEX"),
                Pair(6, "SÁB"),
                Pair(7, "DOM")
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                daysWeek.forEach { (dInt, dName) ->
                  val isSelected = (dueDayOfWeek == dInt)
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) colors.text else colors.rulerWeak,
                        shape = RectangleShape
                      )
                      .background(if (isSelected) colors.text else colors.surface)
                      .clickable { dueDayOfWeek = dInt }
                      .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = dName,
                      fontFamily = ArchivoFont,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      color = if (isSelected) colors.canvas else colors.text
                    )
                  }
                }
              }
            }
          }
          BillRepeatType.QUINZENAL -> {
            Column(modifier = Modifier.padding(top = 6.dp)) {
              Text(
                text = "Repete a cada 15 dias a partir do dia de cadastro.",
                fontFamily = ArchivoFont,
                fontSize = 12.sp,
                color = colors.textSecondary
              )
            }
          }
          BillRepeatType.ANUAL -> {
            Column(modifier = Modifier.padding(top = 6.dp)) {
              Text(
                text = "Repete uma vez ao ano no mesmo dia.",
                fontFamily = ArchivoFont,
                fontSize = 12.sp,
                color = colors.textSecondary
              )
            }
          }
          BillRepeatType.LIVRE -> {
            Column(modifier = Modifier.padding(top = 6.dp)) {
              Text(
                text = "Intervalo personalizado em dias:",
                fontFamily = ArchivoFont,
                fontSize = 12.sp,
                color = colors.textSecondary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Box(
                modifier = Modifier
                  .width(140.dp)
                  .border(1.5.dp, colors.text, RectangleShape)
                  .background(colors.surface)
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                BasicTextField(
                  value = customIntervalDays.toString(),
                  onValueChange = { customIntervalDays = it.toIntOrNull() ?: 30 },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  textStyle = TextStyle(
                    fontFamily = ArchivoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.text
                  ),
                  cursorBrush = SolidColor(colors.text)
                )
              }
            }
          }
        }
      }

      // 5. Notification & Reminder Settings
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, colors.rulerWeak, RectangleShape)
          .background(colors.surface)
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "🔔 NOTIFICAÇÃO E ALERTA",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = colors.text
          )

          Box(
            modifier = Modifier
              .border(1.dp, colors.rulerWeak, RectangleShape)
              .clickable {
                val testAmt = if (isVariableAmount) "Variável" else "R$ ${amountText.ifBlank { "0,00" }}"
                onTestNotification(title.ifBlank { "Minha Conta" }, testAmt)
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "TESTAR AGORA",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              color = colors.accent
            )
          }
        }

        // Advance days
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Avisar com antecedência:",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            reminderAdvanceOptions.forEach { pair ->
              val advDays = pair.first
              val advLabel = pair.second
              val isSelected = (reminderDaysBefore == advDays)
              Box(
                modifier = Modifier
                  .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) colors.text else colors.rulerWeak,
                    shape = RectangleShape
                  )
                  .background(if (isSelected) colors.text else Color.Transparent)
                  .clickable { reminderDaysBefore = advDays }
                  .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Text(
                  text = advLabel,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = if (isSelected) colors.canvas else colors.text
                )
              }
            }
          }
        }

        // Reminder Time
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Horário do lembrete:",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            reminderTimeOptions.forEach { timeOpt ->
              val isSelected = (reminderTime == timeOpt)
              Box(
                modifier = Modifier
                  .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) colors.accent else colors.rulerWeak,
                    shape = RectangleShape
                  )
                  .background(if (isSelected) colors.accent else Color.Transparent)
                  .clickable { reminderTime = timeOpt }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = timeOpt,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = if (isSelected) Color.White else colors.text
                )
              }
            }
          }
        }
      }

      // 6. Barcode / PIX / Payment Code
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "CÓDIGO DE BARRAS / CHAVE PIX (OPCIONAL)",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          color = colors.textSecondary
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
          BasicTextField(
            value = barcode,
            onValueChange = { barcode = it },
            textStyle = TextStyle(
              fontFamily = ArchivoFont,
              fontSize = 13.sp,
              color = colors.text
            ),
            cursorBrush = SolidColor(colors.text),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_bill_barcode"),
            decorationBox = { innerTextField ->
              if (barcode.isEmpty()) {
                Text(
                  text = "Cole a linha digitável do boleto ou chave PIX para copiar rápido...",
                  fontFamily = ArchivoFont,
                  fontSize = 12.sp,
                  color = colors.textTertiary
                )
              }
              innerTextField()
            }
          )
        }
      }

      // 7. Notes
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "OBSERVAÇÕES / DETALHES",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          color = colors.textSecondary
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .background(colors.surface)
            .padding(12.dp)
        ) {
          BasicTextField(
            value = notes,
            onValueChange = { notes = it },
            textStyle = TextStyle(
              fontFamily = ArchivoFont,
              fontSize = 13.sp,
              color = colors.text
            ),
            cursorBrush = SolidColor(colors.text),
            modifier = Modifier
              .fillMaxSize()
              .testTag("input_bill_notes"),
            decorationBox = { innerTextField ->
              if (notes.isEmpty()) {
                Text(
                  text = "Anotações adicionais, link do portal, débito automático...",
                  fontFamily = ArchivoFont,
                  fontSize = 12.sp,
                  color = colors.textTertiary
                )
              }
              innerTextField()
            }
          )
        }
      }

      // Save Button
      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.text)
          .clickable {
            if (title.isBlank()) {
              Toast.makeText(context, "Por favor, digite o nome da conta", Toast.LENGTH_SHORT).show()
              return@clickable
            }
            val amt = amountText.toDoubleOrNull() ?: 0.0
            onSave(
              existingBill?.id,
              title.trim(),
              amt,
              isVariableAmount,
              category.trim(),
              repeatType,
              dueDayOfMonth,
              dueDayOfWeek,
              existingBill?.startDateEpochDay ?: LocalDate.now().toEpochDay(),
              customIntervalDays,
              reminderDaysBefore,
              reminderTime,
              notes.trim(),
              barcode.trim()
            )
          }
          .padding(vertical = 14.dp)
          .testTag("btn_save_bill"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (existingBill != null) "SALVAR ALTERAÇÕES" else "CADASTRAR CONTA",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          letterSpacing = 0.8.sp,
          color = colors.canvas
        )
      }
    }
  }
}
