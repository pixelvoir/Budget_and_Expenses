@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.programmerden.budgetexpenses.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.programmerden.budgetexpenses.R
import com.programmerden.budgetexpenses.data.Transaction
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

private val NeonEmerald = Color(0xFF00E676)
private val SlateGrey = Color(0xFFE0E0E0)
private val VividCrimson = Color(0xFF1A1A1A)  // Neutral black for debits
private val RingYellow = Color(0xFFFFD54F)
private val WarmBlue = Color(0xFF4FC3F7)
private val WarmOrange = Color(0xFFFFB74D)
private val WarmPurple = Color(0xFFBA68C8)
private val NavyBlue = Color(0xFF0033A0)

private val CATEGORY_LIST = listOf("food", "travel", "shopping", "health", "bills", "transfers", "personal")

private fun categoryColor(category: String?): Color {
    return when (category?.lowercase()) {
        "food" -> Color(0xFFFF6B6B)           // Red
        "travel" -> Color(0xFF4ECDC4)         // Teal
        "shopping" -> Color(0xFFFFE66D)       // Yellow
        "health" -> Color(0xFF95E1D3)         // Mint
        "bills" -> Color(0xFF6C5CE7)          // Purple
        "transfers" -> Color(0xFF74B9FF)      // Blue
        "personal" -> Color(0xFFA29BFE)       // Light Purple
        else -> Color(0xFF95A5A6)             // Gray for uncategorized
    }
}

private enum class AppTab(val label: String) {
    Dash("Dash"),
    Flow("Flow"),
    Inflow("Inflow"),
    Trends("Trends"),
    Vault("Vault")
}

private data class MonthOption(
    val yearMonth: YearMonth,
    val label: String,
    val totalDebits: Double,
    val totalCredits: Double
)

private data class EditState(
    val transaction: Transaction,
    val amountText: String,
    val merchantText: String,
    val timestampText: String
)

private val editInputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
private val manualDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
private val monthDayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
@Composable
fun TransactionListScreen(
    transactions: List<Transaction>,
    darkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onUpdateTransaction: (Transaction) -> Unit,
    onAddManualTransaction: (Double, String, LocalDate, Boolean, String?) -> Unit,
    onDeleteTransaction: (Int) -> Unit,
    onClearAllData: () -> Unit,
    onRestoreSampleData: () -> Unit,
    viewModel: com.programmerden.budgetexpenses.ui.viewmodels.TransactionViewModel
) {
    val haptic = LocalHapticFeedback.current
    val zoneId = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zoneId) }

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Dash) }
    var selectedMonthIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedDay by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }
    var budgetLimit by rememberSaveable { mutableFloatStateOf(10000f) }
    var actionTarget by remember { mutableStateOf<Transaction?>(null) }
    var editState by remember { mutableStateOf<EditState?>(null) }
    var manualEntryOpen by remember { mutableStateOf(false) }
    var clearAllConfirm by remember { mutableStateOf(false) }

    val monthOptions = buildMonthOptions(transactions, zoneId)
    val safeMonthIndex = selectedMonthIndex.coerceIn(0, monthOptions.lastIndex.coerceAtLeast(0))
    val currentMonth = monthOptions.getOrNull(safeMonthIndex)?.yearMonth ?: YearMonth.from(today)

    LaunchedEffect(monthOptions) {
        if (monthOptions.isNotEmpty() && selectedMonthIndex !in monthOptions.indices) {
            selectedMonthIndex = 0
        } else if (monthOptions.isEmpty()) {
            selectedMonthIndex = 0
        }
    }

    LaunchedEffect(monthOptions, safeMonthIndex, today) {
        selectedMonthIndex = safeMonthIndex
        selectedDay = defaultDayForMonth(currentMonth, today).coerceIn(1, currentMonth.lengthOfMonth())
    }

    val selectedDate = currentMonth.atDay(selectedDay.coerceIn(1, currentMonth.lengthOfMonth()))
    val selectedDayTransactions = remember(transactions, selectedDate) {
        transactions.filter { transaction ->
            Instant.ofEpochMilli(transaction.timestamp).atZone(zoneId).toLocalDate() == selectedDate
        }
    }
    val selectedMonthDebitDaySet = remember(transactions, currentMonth) {
        transactions.filterNot { it.isCredit }
            .filter {
                Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().yearMonth() == currentMonth
            }
            .map {
                Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().dayOfMonth
            }
            .toSet()
    }
    val selectedMonthCreditDaySet = remember(transactions, currentMonth) {
        transactions.filter { it.isCredit }
            .filter {
                Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().yearMonth() == currentMonth
            }
            .map {
                Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().dayOfMonth
            }
            .toSet()
    }
    val selectedDayDebits = remember(selectedDayTransactions) { selectedDayTransactions.filterNot { it.isCredit } }
    val selectedDayCredits = remember(selectedDayTransactions) { selectedDayTransactions.filter { it.isCredit } }

    val totalDebits = remember(transactions) { transactions.filterNot { it.isCredit }.sumOf { it.amount } }
    val remainingAmount = budgetLimit - totalDebits.toFloat()
    val progress = if (budgetLimit <= 0f) 0f else (totalDebits.toFloat() / budgetLimit).coerceIn(0f, 1.25f)

    val currentMonthTransactions = remember(transactions, currentMonth) {
        transactions.filter { transaction ->
            Instant.ofEpochMilli(transaction.timestamp).atZone(zoneId).toLocalDate().yearMonth() == currentMonth
        }
    }
    val monthSpent = currentMonthTransactions.filterNot { it.isCredit }.sumOf { it.amount }

    val spentToday = remember(transactions, today) {
        transactions.filterNot { it.isCredit }
            .filter { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() == today }
            .sumOf { it.amount }
    }
    val weekStart = remember(today) {
        today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong())
    }
    val spentThisWeek = remember(transactions, weekStart) {
        transactions.filterNot { it.isCredit }
            .filter { transaction ->
                val transactionDate = Instant.ofEpochMilli(transaction.timestamp).atZone(zoneId).toLocalDate()
                !transactionDate.isBefore(weekStart)
            }
            .sumOf { it.amount }
    }

    val averageDays = remember(transactions, today) {
        if (transactions.isEmpty()) 0 else max(1, ChronoUnit.DAYS.between(
            transactions.minOf { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() },
            today
        ).toInt() + 1)
    }
    val averageWeeks = max(1, ceil(averageDays / 7.0).toInt())
    val averageMonths = max(1, ChronoUnit.MONTHS.between(
        YearMonth.from(transactions.minOfOrNull { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() } ?: today),
        YearMonth.from(today)
    ).toInt() + 1)

    val averagePerDay = if (transactions.isEmpty()) 0.0 else totalDebits / averageDays
    val averagePerWeek = if (transactions.isEmpty()) 0.0 else totalDebits / averageWeeks
    val averagePerMonth = if (transactions.isEmpty()) 0.0 else totalDebits / averageMonths

    BackHandler(enabled = selectedTab != AppTab.Dash) {
        selectedTab = AppTab.Dash
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedTab = it
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackgroundBrush(darkTheme))
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                    (slideInHorizontally(
                        animationSpec = tween(260),
                        initialOffsetX = { it * direction }
                    ) + fadeIn(animationSpec = tween(160))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { -it * direction }
                        ) + fadeOut(animationSpec = tween(140)))
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    AppTab.Dash -> DashTab(
                        transactions = transactions,
                        darkTheme = darkTheme,
                        budgetLimit = budgetLimit,
                        remainingAmount = remainingAmount,
                        progress = progress,
                        spentToday = spentToday,
                        spentThisWeek = spentThisWeek,
                        averagePerDay = averagePerDay,
                        averagePerWeek = averagePerWeek,
                        averagePerMonth = averagePerMonth,
                        monthSpent = monthSpent,
                        onOpenCurrentFlow = {
                            selectedMonthIndex = monthOptions.lastIndex.coerceAtLeast(0)
                            selectedDay = defaultDayForMonth(YearMonth.from(today), today)
                            selectedTab = AppTab.Flow
                        },
                        onOpenTrends = {
                            selectedTab = AppTab.Trends
                        },
                        onManualAddRequest = {
                            manualEntryOpen = true
                        }
                    )

                    AppTab.Flow -> TransactionFeedTab(
                        darkTheme = darkTheme,
                        title = "FLOW",
                        isCredit = false,
                        monthOptions = monthOptions,
                        selectedMonthIndex = selectedMonthIndex,
                        selectedDay = selectedDay,
                        onMonthSelected = { index ->
                            selectedMonthIndex = index
                            selectedDay = defaultDayForMonth(monthOptions[index].yearMonth, today)
                        },
                        onDaySelected = { selectedDay = it },
                        transactions = selectedDayDebits,
                        dayTotal = selectedDayDebits.sumOf { it.amount },
                        onTransactionClick = { actionTarget = it },
                        dayAvailability = selectedMonthDebitDaySet
                    )

                    AppTab.Inflow -> TransactionFeedTab(
                        darkTheme = darkTheme,
                        title = "INFLOW",
                        isCredit = true,
                        monthOptions = monthOptions,
                        selectedMonthIndex = selectedMonthIndex,
                        selectedDay = selectedDay,
                        onMonthSelected = { index ->
                            selectedMonthIndex = index
                            selectedDay = defaultDayForMonth(monthOptions[index].yearMonth, today)
                        },
                        onDaySelected = { selectedDay = it },
                        transactions = selectedDayCredits,
                        dayTotal = selectedDayCredits.sumOf { it.amount },
                        onTransactionClick = { actionTarget = it },
                        dayAvailability = selectedMonthCreditDaySet
                    )

                    AppTab.Trends -> TrendsTab(
                        darkTheme = darkTheme,
                        transactions = transactions,
                        monthOptions = monthOptions,
                        selectedMonthIndex = selectedMonthIndex,
                        selectedMonthIndexChanged = { index ->
                            selectedMonthIndex = index
                            selectedDay = defaultDayForMonth(monthOptions[index].yearMonth, today)
                        },
                        selectedMonth = currentMonth,
                        selectedMonthTransactions = currentMonthTransactions,
                        viewModel = viewModel
                    )

                    AppTab.Vault -> VaultTab(
                        darkTheme = darkTheme,
                        budgetLimit = budgetLimit,
                        onBudgetChange = { budgetLimit = it },
                        onBudgetTextChange = { text -> text.toFloatOrNull()?.let { budgetLimit = it } },
                        onThemeToggle = onThemeToggle,
                        onClearAllData = { clearAllConfirm = true },
                        onRestoreSampleData = onRestoreSampleData
                    )
                }
            }
        }
    }

    actionTarget?.let { transaction ->
        if (transaction.isCredit) {
            TransactionActionsDialog(
                transaction = transaction,
                darkTheme = darkTheme,
                onDismiss = { actionTarget = null },
                onEdit = {
                    actionTarget = null
                    editState = EditState(
                        transaction = transaction,
                        amountText = transaction.amount.toString(),
                        merchantText = transaction.merchant,
                        timestampText = Instant.ofEpochMilli(transaction.timestamp)
                            .atZone(zoneId)
                            .toLocalDateTime()
                            .format(editInputFormatter)
                    )
                },
                onDelete = {
                    actionTarget = null
                    onDeleteTransaction(transaction.id)
                }
            )
        } else {
            CategoryPickerDialog(
                darkTheme = darkTheme,
                transaction = transaction,
                onCategorySelected = { category ->
                    viewModel.updateTransactionCategory(transaction.id, category)
                    actionTarget = null
                },
                onDismiss = {
                    actionTarget = null
                },
                onEdit = {
                    actionTarget = null
                    editState = EditState(
                        transaction = transaction,
                        amountText = transaction.amount.toString(),
                        merchantText = transaction.merchant,
                        timestampText = Instant.ofEpochMilli(transaction.timestamp)
                            .atZone(zoneId)
                            .toLocalDateTime()
                            .format(editInputFormatter)
                    )
                },
                onDelete = {
                    actionTarget = null
                    onDeleteTransaction(transaction.id)
                }
            )
        }
    }

    editState?.let { state ->
        EditTransactionDialog(
            darkTheme = darkTheme,
            editState = state,
            onDismiss = { editState = null },
            onSave = { amount, merchant, timestamp ->
                onUpdateTransaction(
                    state.transaction.copy(
                        amount = amount,
                        merchant = merchant,
                        timestamp = timestamp
                    )
                )
                editState = null
            }
        )
    }

    if (clearAllConfirm) {
        ConfirmationDialog(
            darkTheme = darkTheme,
            title = "Clear all data",
            message = "This removes every stored transaction.",
            confirmText = "Delete",
            onDismiss = { clearAllConfirm = false },
            onConfirm = {
                clearAllConfirm = false
                onClearAllData()
            }
        )
    }

    if (manualEntryOpen) {
        ManualTransactionDialog(
            darkTheme = darkTheme,
            defaultDate = today,
            onDismiss = { manualEntryOpen = false },
            onSave = { amount, merchant, date, isCredit, category ->
                onAddManualTransaction(amount, merchant, date, isCredit, if (isCredit) null else category)
                category?.let { viewModel.mapMerchantToCategory(merchant, it) }
                manualEntryOpen = false
            }
        )
    }
}

@Composable
private fun DashTab(
    transactions: List<Transaction>,
    darkTheme: Boolean,
    budgetLimit: Float,
    remainingAmount: Float,
    progress: Float,
    spentToday: Double,
    spentThisWeek: Double,
    averagePerDay: Double,
    averagePerWeek: Double,
    averagePerMonth: Double,
    monthSpent: Double,
    onOpenCurrentFlow: () -> Unit,
    onOpenTrends: () -> Unit,
    onManualAddRequest: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Fold",
                    tint = contentColor(darkTheme),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = "Fold",
                    color = contentColor(darkTheme),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        item {
            GradientCard(darkTheme = darkTheme) {
                if (transactions.isEmpty()) {
                    EmptyPulseState()
                } else {
                    val ringColor = when {
                        progress < 0.60f -> NeonEmerald
                        progress <= 0.90f -> RingYellow
                        else -> VividCrimson
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressRing(
                                progress = progress,
                                ringColor = ringColor,
                                modifier = Modifier.size(240.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Remaining: ₹${money(remainingAmount.toDouble())}",
                                    color = contentColor(darkTheme),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Spent: ₹${money((budgetLimit - remainingAmount).toDouble())}",
                                    color = contentColor(darkTheme).copy(alpha = 0.72f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today: Total Spend + Average Per Day
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ClickableSummaryTile(
                    darkTheme = darkTheme,
                    label = "Today's Spend",
                    value = "₹${money(spentToday)}",
                    accent = VividCrimson,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCurrentFlow
                )
                ClickableSummaryTile(
                    darkTheme = darkTheme,
                    label = "Daily Average",
                    value = "₹${money(averagePerDay)}",
                    accent = NeonEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenTrends
                )
            }
        }

        // Week: Total Spend + Average Per Week
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ClickableSummaryTile(
                    darkTheme = darkTheme,
                    label = "Week's Spend",
                    value = "₹${money(spentThisWeek)}",
                    accent = WarmPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCurrentFlow
                )
                ClickableSummaryTile(
                    darkTheme = darkTheme,
                    label = "Weekly Average",
                    value = "₹${money(averagePerWeek)}",
                    accent = NeonEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenTrends
                )
            }
        }

        // Month: Total Spend + Average Per Month
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ClickableSummaryTile(
                    darkTheme = darkTheme,
                    label = "Monthly Spend",
                    value = "₹${money(monthSpent)}",
                    accent = RingYellow,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCurrentFlow
                )
                ClickableSummaryTile(
                    darkTheme = darkTheme,
                    label = "Monthly Average",
                    value = "₹${money(averagePerMonth)}",
                    accent = NeonEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenTrends
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            ManualEntryActionTile(
                darkTheme = darkTheme,
                onClick = onManualAddRequest
            )
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }
    }
}

@Composable
private fun ManualEntryActionTile(
    darkTheme: Boolean,
    onClick: () -> Unit
) {
    GradientCard(
        darkTheme = darkTheme,
        accent = WarmOrange,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add transaction",
                tint = contentColor(darkTheme),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Add Manual Transaction",
                color = contentColor(darkTheme),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Enter amount, merchant, date and credit/debit",
                color = contentColor(darkTheme).copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TransactionFeedTab(
    darkTheme: Boolean,
    title: String,
    isCredit: Boolean,
    monthOptions: List<MonthOption>,
    selectedMonthIndex: Int,
    selectedDay: Int,
    onMonthSelected: (Int) -> Unit,
    onDaySelected: (Int) -> Unit,
    transactions: List<Transaction>,
    dayTotal: Double,
    onTransactionClick: (Transaction) -> Unit,
    dayAvailability: Set<Int>
) {
    val currentMonth = monthOptions.getOrNull(selectedMonthIndex)?.yearMonth ?: YearMonth.now()
    val selectedDateLabel = currentMonth.atDay(selectedDay.coerceIn(1, currentMonth.lengthOfMonth())).format(monthDayFormatter)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            SummaryTile(
                darkTheme = darkTheme,
                label = selectedDateLabel,
                value = "₹${money(dayTotal)}",
                accent = if (isCredit) NeonEmerald else VividCrimson
            )
        }

        item {
            GradientCard(darkTheme = darkTheme) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = title,
                        color = contentColor(darkTheme),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Select Month",
                            color = contentColor(darkTheme).copy(alpha = 0.70f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        MonthSelector(
                            darkTheme = darkTheme,
                            monthOptions = monthOptions,
                            selectedIndex = selectedMonthIndex,
                            onSelected = onMonthSelected
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Select Day",
                            color = contentColor(darkTheme).copy(alpha = 0.70f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        DaySelector(
                            darkTheme = darkTheme,
                            yearMonth = currentMonth,
                            selectedDay = selectedDay,
                            onSelected = onDaySelected,
                            dayAvailability = dayAvailability
                        )
                    }
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                GradientCard(darkTheme = darkTheme) {
                    Text(
                        text = if (isCredit) "No credits for this day" else "No debits for this day",
                        color = contentColor(darkTheme)
                    )
                }
            }
        } else {
            item {
                SectionHeader(
                    darkTheme = darkTheme,
                    label = selectedDateLabel
                )
            }
            items(transactions, key = { it.id }) { transaction ->
                TransactionTile(
                    darkTheme = darkTheme,
                    transaction = transaction,
                    isCredit = isCredit,
                    onClick = { onTransactionClick(transaction) }
                )
            }
        }
    }
}

@Composable
private fun TrendsTab(
    darkTheme: Boolean,
    transactions: List<Transaction>,
    monthOptions: List<MonthOption>,
    selectedMonthIndex: Int,
    selectedMonthIndexChanged: (Int) -> Unit,
    selectedMonth: YearMonth,
    selectedMonthTransactions: List<Transaction>,
    viewModel: com.programmerden.budgetexpenses.ui.viewmodels.TransactionViewModel
) {
    val monthIndex = selectedMonthIndex.coerceIn(0, monthOptions.lastIndex.coerceAtLeast(0))
    val monthSpent = selectedMonthTransactions.filterNot { it.isCredit }.sumOf { it.amount }
    var showAllTime by remember { mutableStateOf(false) }

    val categorySpending = remember(selectedMonthTransactions, showAllTime, transactions) {
        if (showAllTime) {
            viewModel.getCategorySpending(transactions, null)
        } else {
            viewModel.getCategorySpending(transactions, selectedMonth)
        }
    }
    val totalCategorySpending = categorySpending.values.sum()
    val sortedCategories = categorySpending.toList().sortedByDescending { it.second }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            SummaryTile(
                darkTheme = darkTheme,
                label = if (showAllTime) "All Time Spend" else "Selected Month Spend",
                value = "₹${money(if (showAllTime) totalCategorySpending else monthSpent)}",
                accent = WarmBlue
            )
        }

        item {
            GradientCard(darkTheme = darkTheme) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Trends",
                        color = contentColor(darkTheme),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Select Month",
                            color = contentColor(darkTheme).copy(alpha = 0.70f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        MonthSelector(
                            darkTheme = darkTheme,
                            monthOptions = monthOptions,
                            selectedIndex = monthIndex,
                            onSelected = selectedMonthIndexChanged
                        )
                    }
                }
            }
        }

        item {
            GradientCard(darkTheme = darkTheme) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!showAllTime) MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.22f else 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = if (darkTheme) 0.40f else 0.86f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showAllTime = false }
                    ) {
                        Text(
                            text = "This Month",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            color = contentColor(darkTheme),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showAllTime) MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.22f else 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = if (darkTheme) 0.40f else 0.86f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showAllTime = true }
                    ) {
                        Text(
                            text = "All Time",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            color = contentColor(darkTheme),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Spending by Category",
                color = contentColor(darkTheme),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (sortedCategories.isEmpty()) {
            item {
                GradientCard(darkTheme = darkTheme) {
                    Text(
                        text = "No transactions yet",
                        color = contentColor(darkTheme),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(sortedCategories) { item ->
                val category = item.first
                val amount = item.second
                val percentage = if (totalCategorySpending > 0) (amount / totalCategorySpending) * 100 else 0.0
                CategorySpendingTile(
                    darkTheme = darkTheme,
                    category = category,
                    amount = amount,
                    percentage = percentage
                )
            }
        }
    }
}

@Composable
private fun VaultTab(
    darkTheme: Boolean,
    budgetLimit: Float,
    onBudgetChange: (Float) -> Unit,
    onBudgetTextChange: (String) -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onClearAllData: () -> Unit,
    onRestoreSampleData: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            GradientCard(darkTheme = darkTheme) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Budget Settings",
                        color = contentColor(darkTheme),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = budgetLimit.roundToInt().toString(),
                        onValueChange = onBudgetTextChange,
                        label = { Text("Monthly Budget") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Adjust Budget:",
                        color = contentColor(darkTheme).copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = budgetLimit.coerceAtLeast(500f),
                        onValueChange = onBudgetChange,
                        valueRange = 500f..100000f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "₹${money(budgetLimit.toDouble())}",
                        color = contentColor(darkTheme),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        item {
            GradientCard(darkTheme = darkTheme) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dark Mode",
                            color = contentColor(darkTheme),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Persistent theme preference",
                            color = contentColor(darkTheme).copy(alpha = 0.72f),
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = onThemeToggle,
                        colors = SwitchDefaults.colors()
                    )
                }
            }
        }

        item {
            GradientCard(darkTheme = darkTheme) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Data Management",
                        color = contentColor(darkTheme),
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onRestoreSampleData) {
                        Text("Restore Sample Data")
                    }
                    TextButton(onClick = onClearAllData) {
                        Text("Clear All Transactions")
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
    ) {
        AppTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun TransactionActionsDialog(
    transaction: Transaction,
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GradientCard(darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Transaction", color = contentColor(darkTheme), fontWeight = FontWeight.Bold)
                Text(text = transaction.merchant, color = contentColor(darkTheme))
                Text(text = "₹${money(transaction.amount)}", color = contentColor(darkTheme).copy(alpha = 0.80f))
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Edit") }
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Delete") }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun CategoryPickerDialog(
    darkTheme: Boolean,
    transaction: Transaction,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GradientCard(darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Select Category", color = contentColor(darkTheme), fontWeight = FontWeight.Bold)
                Text(text = transaction.merchant, color = contentColor(darkTheme).copy(alpha = 0.78f), fontSize = 13.sp)
                Text(
                    text = "₹${money(transaction.amount)}",
                    color = if (transaction.isCredit) NeonEmerald else VividCrimson,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    items(CATEGORY_LIST) { category ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = categoryColor(category).copy(alpha = if (darkTheme) 0.15f else 0.10f),
                            border = BorderStroke(1.dp, categoryColor(category).copy(alpha = if (darkTheme) 0.40f else 0.30f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(category) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(categoryColor(category))
                                )
                                Text(
                                    text = category.replaceFirstChar { it.uppercase() },
                                    color = contentColor(darkTheme),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit", fontSize = 12.sp) }
                    TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("Delete", fontSize = 12.sp) }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun EditTransactionDialog(
    darkTheme: Boolean,
    editState: EditState,
    onDismiss: () -> Unit,
    onSave: (Double, String, Long) -> Unit
) {
    var amountText by rememberSaveable(editState.transaction.id) { mutableStateOf(editState.amountText) }
    var merchantText by rememberSaveable(editState.transaction.id) { mutableStateOf(editState.merchantText) }
    var timestampText by rememberSaveable(editState.transaction.id) { mutableStateOf(editState.timestampText) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        GradientCard(darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Edit transaction", color = contentColor(darkTheme), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = merchantText,
                    onValueChange = { merchantText = it },
                    label = { Text("Merchant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = timestampText,
                    onValueChange = { timestampText = it },
                    label = { Text("Date time yyyy-MM-dd HH:mm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(text = errorText.orEmpty(), color = VividCrimson)
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val timestamp = try {
                        LocalDateTime.parse(timestampText, editInputFormatter)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    } catch (_: DateTimeParseException) {
                        null
                    }
                    if (amount == null || merchantText.isBlank() || timestamp == null) {
                        errorText = "Check amount, merchant, and date format."
                        return@TextButton
                    }
                    onSave(amount, merchantText.trim(), timestamp)
                }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun ManualDatePickerDialog(
    darkTheme: Boolean,
    initialDate: LocalDate,
    maxDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        yearRange = (initialDate.year - 5)..maxDate.year
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val selected = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    if (!selected.isAfter(maxDate)) {
                        onDateSelected(selected)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun ManualTransactionDialog(
    darkTheme: Boolean,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (Double, String, LocalDate, Boolean, String?) -> Unit
) {
    var amountText by rememberSaveable { mutableStateOf("") }
    var merchantText by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(defaultDate) }
    var isCredit by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        ManualDatePickerDialog(
            darkTheme = darkTheme,
            initialDate = selectedDate,
            maxDate = LocalDate.now(ZoneId.systemDefault()),
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        GradientCard(darkTheme = darkTheme, accent = WarmOrange) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Add transaction",
                    color = contentColor(darkTheme),
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = merchantText,
                    onValueChange = { merchantText = it },
                    label = { Text("Merchant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Date: ${selectedDate.format(manualDateFormatter)}",
                        color = contentColor(darkTheme),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!isCredit) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (selectedCategory.isBlank()) "Select category" else selectedCategory.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            CATEGORY_LIST.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Type",
                    color = contentColor(darkTheme).copy(alpha = 0.72f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (!isCredit) MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.22f else 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = if (darkTheme) 0.40f else 0.86f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCredit = false }
                    ) {
                        Text(
                            text = "Debit",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            color = contentColor(darkTheme),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCredit) MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.22f else 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = if (darkTheme) 0.40f else 0.86f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCredit = true }
                    ) {
                        Text(
                            text = "Credit",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            color = contentColor(darkTheme),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (errorText != null) {
                    Text(text = errorText.orEmpty(), color = VividCrimson)
                }

                TextButton(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()

                        if (amount == null || amount <= 0.0 || merchantText.isBlank()) {
                            errorText = "Check amount and merchant."
                            return@TextButton
                        }

                        if (selectedDate.isAfter(LocalDate.now(ZoneId.systemDefault()))) {
                            errorText = "Cannot add future transactions."
                            return@TextButton
                        }

                        onSave(amount, merchantText.trim(), selectedDate, isCredit, if (isCredit) null else selectedCategory.ifBlank { null })
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(
    darkTheme: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GradientCard(darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = title, color = contentColor(darkTheme), fontWeight = FontWeight.Bold)
                Text(text = message, color = contentColor(darkTheme).copy(alpha = 0.80f))
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text(confirmText) }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    darkTheme: Boolean,
    monthOptions: List<MonthOption>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    val initialIndex = selectedIndex.coerceIn(0, monthOptions.lastIndex.coerceAtLeast(0))
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(selectedIndex, monthOptions.size) {
        if (selectedIndex in monthOptions.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(monthOptions) { index, month ->
            val selected = index == selectedIndex
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.22f else 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = if (darkTheme) 0.42f else 0.88f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f))
            ) {
                Text(
                    text = month.label,
                    modifier = Modifier
                        .clickable { onSelected(index) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = contentColor(darkTheme),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DaySelector(
    darkTheme: Boolean,
    yearMonth: YearMonth,
    selectedDay: Int,
    onSelected: (Int) -> Unit,
    dayAvailability: Set<Int>
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items((1..yearMonth.lengthOfMonth()).toList(), key = { it }) { day ->
            val selected = day == selectedDay
            val hasTransactions = day in dayAvailability
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    selected -> MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.22f else 0.16f)
                    hasTransactions -> Color(0xFF1B5E20).copy(alpha = if (darkTheme) 0.32f else 0.16f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = if (darkTheme) 0.28f else 0.80f)
                },
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
            ) {
                Column(
                    modifier = Modifier
                        .clickable { onSelected(day) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = day.toString(), fontWeight = FontWeight.Bold, color = contentColor(darkTheme))
                    Text(
                        text = yearMonth.atDay(day).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        color = if (hasTransactions) Color(0xFF2E7D32) else contentColor(darkTheme).copy(alpha = 0.70f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthTile(
    darkTheme: Boolean,
    month: MonthOption,
    onClick: () -> Unit
) {
    GradientCard(darkTheme = darkTheme, modifier = Modifier.clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = month.label, color = contentColor(darkTheme), fontWeight = FontWeight.Bold)
                Text(text = "Spent ₹${money(month.totalDebits)}", color = VividCrimson, fontWeight = FontWeight.SemiBold)
                Text(text = "Credited ₹${money(month.totalCredits)}", color = NeonEmerald, fontWeight = FontWeight.SemiBold)
            }
            Text(text = "Open", color = contentColor(darkTheme).copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun TransactionTile(
    darkTheme: Boolean,
    transaction: Transaction,
    isCredit: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val accent = if (isCredit) NeonEmerald else VividCrimson
    val monogram = transaction.merchant
        .split(" ", "-", ".", ",", "/")
        .filter { it.isNotBlank() }
        .joinToString("")
        .take(2)
        .uppercase(Locale.getDefault())
        .ifBlank { "TX" }
    val categoryLabel = transaction.category?.takeIf { it.isNotBlank() }

    GradientCard(
        darkTheme = darkTheme,
        accent = accent,
        modifier = Modifier.combinedClickable(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accent.copy(alpha = if (darkTheme) 0.24f else 0.18f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = monogram, color = contentColor(darkTheme), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.merchant, color = contentColor(darkTheme), fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Instant.ofEpochMilli(transaction.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().format(timeFormatter),
                        color = contentColor(darkTheme).copy(alpha = 0.72f),
                        fontSize = 12.sp
                    )
                    if (categoryLabel != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = categoryColor(categoryLabel).copy(alpha = if (darkTheme) 0.22f else 0.14f),
                            border = BorderStroke(0.5.dp, categoryColor(categoryLabel).copy(alpha = 0.30f))
                        ) {
                            Text(
                                text = categoryLabel.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = categoryColor(categoryLabel),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Text(
                text = "₹${money(transaction.amount)}",
                color = if (darkTheme && !isCredit) Color(0xFFFFB74D) else accent,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun SummaryTile(
    darkTheme: Boolean,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GradientCard(darkTheme = darkTheme, accent = accent, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = label,
                color = contentColor(darkTheme).copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = if (darkTheme) Color.White else Color(0xFF10151D),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ClickableSummaryTile(
    darkTheme: Boolean,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GradientCard(darkTheme = darkTheme, accent = accent, modifier = modifier.clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = label,
                color = contentColor(darkTheme).copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = if (darkTheme) Color.White else Color(0xFF10151D),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategorySpendingTile(
    darkTheme: Boolean,
    category: String,
    amount: Double,
    percentage: Double
) {
    val categoryName = if (category == "Uncategorized") "Other" else category.replaceFirstChar { it.uppercase() }
    GradientCard(darkTheme = darkTheme, accent = categoryColor(category)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(categoryColor(category))
                    )
                    Text(
                        text = categoryName,
                        color = contentColor(darkTheme),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${String.format("%.1f", percentage)}%",
                    color = categoryColor(category),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(categoryColor(category).copy(alpha = if (darkTheme) 0.20f else 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (percentage / 100f).toFloat().coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(categoryColor(category))
                )
            }

            Text(
                text = "₹${money(amount)}",
                color = contentColor(darkTheme),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun GradientCard(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    accent: Color = WarmBlue,
    content: @Composable () -> Unit
) {
    val base = if (darkTheme) {
        listOf(
            Color(0xFF0D1B4C),
            Color(0xFF081235)
        )
    } else {
        listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF2F6FF)
        )
    }
    val overlay = if (darkTheme) {
        Brush.linearGradient(listOf(accent.copy(alpha = 0.24f), Color.Transparent))
    } else {
        Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), Color.Transparent))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (darkTheme) 0.28f else 0.20f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(base))
                .background(overlay)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(
    darkTheme: Boolean,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color(0xFFEFF3F9),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = contentColor(darkTheme),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CircularProgressRing(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress = androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "ring"
    ).value

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 12.dp.toPx()
            val sizePx = size.minDimension - strokeWidthPx
            val offset = strokeWidthPx / 2f
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun EmptyPulseState() {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.82f),
                            Color.White.copy(alpha = 0.54f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = R.drawable.pixel_cat,
                imageLoader = imageLoader,
                contentDescription = "Animated pixel cat",
                modifier = Modifier.size(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "No transactions yet",
            color = Color(0xFF10151D),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun buildMonthOptions(transactions: List<Transaction>, zoneId: ZoneId): List<MonthOption> {
    val today = LocalDate.now(zoneId)
    val oldest = transactions.minOfOrNull {
        Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate()
    }?.let { YearMonth.from(it) } ?: YearMonth.from(today)
    val newest = YearMonth.from(today)

    return generateSequence(newest) { month -> month.minusMonths(1) }
        .takeWhile { !it.isBefore(oldest) }
        .map { yearMonth ->
            val monthTransactions = transactions.filter {
                Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate().yearMonth() == yearMonth
            }
            MonthOption(
                yearMonth = yearMonth,
                label = yearMonth.formatMonthLabel(),
                totalDebits = monthTransactions.filterNot { it.isCredit }.sumOf { it.amount },
                totalCredits = monthTransactions.filter { it.isCredit }.sumOf { it.amount }
            )
        }
        .toList()
}

private fun buildFourWeekTotals(transactions: List<Transaction>): List<Double> {
    val grouped = transactions.filterNot { it.isCredit }.groupBy {
        val day = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
        when {
            day <= 7 -> 1
            day <= 14 -> 2
            day <= 21 -> 3
            else -> 4
        }
    }
    return (1..4).map { week ->
        grouped[week].orEmpty().sumOf { it.amount }
    }
}

private fun defaultDayForMonth(month: YearMonth, today: LocalDate): Int {
    return if (month == YearMonth.from(today)) today.dayOfMonth else 1
}

private fun YearMonth.formatMonthLabel(): String {
    return month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + year
}

private fun LocalDate.yearMonth(): YearMonth = YearMonth.from(this)

private fun screenBackgroundBrush(darkTheme: Boolean): Brush {
    return if (darkTheme) {
        Brush.verticalGradient(colors = listOf(Color(0xFF080A0F), Color(0xFF080A0F)))
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFFFDFEFF), Color(0xFFEAF0F7))
        )
    }
}

private fun money(value: Double): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value)
}

private fun contentColor(darkTheme: Boolean): Color {
    return if (darkTheme) Color(0xFFF5F7FA) else Color(0xFF10151D)
}

private val AppTab.icon: ImageVector
    get() = when (this) {
        AppTab.Dash -> Icons.AutoMirrored.Outlined.Send
        AppTab.Flow -> Icons.AutoMirrored.Outlined.TrendingDown
        AppTab.Inflow -> Icons.AutoMirrored.Outlined.TrendingUp
        AppTab.Trends -> Icons.Outlined.Timeline
        AppTab.Vault -> Icons.Outlined.Settings
    }
