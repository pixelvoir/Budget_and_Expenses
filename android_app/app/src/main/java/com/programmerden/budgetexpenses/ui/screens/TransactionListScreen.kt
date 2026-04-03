@file:OptIn(ExperimentalFoundationApi::class)

package com.programmerden.budgetexpenses.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.programmerden.budgetexpenses.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.animation.togetherWith

private val DeepCharcoal = Color(0xFF0C1017)
private val GlassTint = Color.White.copy(alpha = 0.08f)
private val GlassBorder = Color.White.copy(alpha = 0.20f)
private val Emerald = Color(0xFF00E676)
private val Slate = Color(0xFFE0E0E0)
private val Crimson = Color(0xFFFF1744)
private val RingGreen = Color(0xFF00E676)
private val RingYellow = Color(0xFFFFD54F)
private val RingRed = Color(0xFFFF5252)

private enum class BudgetTab(val label: String) {
    Pulse("PULSE"),
    Flow("FLOW"),
    Inflow("INFLOW"),
    Vault("VAULT")
}

@Composable
fun TransactionListScreen(
    transactions: List<Transaction>,
    onSampleSubmit: (String) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var selectedTab by rememberSaveable { mutableStateOf(BudgetTab.Pulse) }
    var monthlyBudgetInput by rememberSaveable { mutableStateOf("10000") }
    var monthlyBudgetLimit by rememberSaveable { mutableFloatStateOf(10000f) }

    val budgetValue = monthlyBudgetLimit.coerceAtLeast(0f)
    val debitTransactions = remember(transactions) { transactions.filterNot { it.isCredit } }
    val creditTransactions = remember(transactions) { transactions.filter { it.isCredit } }
    val totalDebits = remember(debitTransactions) { debitTransactions.sumOf { it.amount } }
    val totalCredits = remember(creditTransactions) { creditTransactions.sumOf { it.amount } }
    val remainingAmount = budgetValue - totalDebits.toFloat()
    val debitRatio = if (budgetValue <= 0f) 0f else (totalDebits.toFloat() / budgetValue).coerceAtLeast(0f)

    Surface(modifier = Modifier.fillMaxSize(), color = DeepCharcoal) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .backgroundOrbs()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderSummary(
                    totalDebits = totalDebits,
                    totalCredits = totalCredits,
                    monthlyBudgetLimit = budgetValue.toDouble(),
                    remainingAmount = remainingAmount.toDouble()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                        (slideInHorizontally(
                            animationSpec = tween(320),
                            initialOffsetX = { fullWidth -> fullWidth * direction }
                        ) + fadeIn(animationSpec = tween(220))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(280),
                                targetOffsetX = { fullWidth -> -fullWidth * direction }
                            ) + fadeOut(animationSpec = tween(180)))
                    },
                    label = "budget_tab_transition"
                ) { tab ->
                    when (tab) {
                        BudgetTab.Pulse -> PulseTab(
                            transactions = transactions,
                            totalDebits = totalDebits,
                            budgetLimit = budgetValue.toDouble(),
                            remainingAmount = remainingAmount.toDouble(),
                            progress = debitRatio,
                            onEmptyAction = onSampleSubmit
                        )
                        BudgetTab.Flow -> TransactionFeedTab(
                            title = "Debits",
                            transactions = debitTransactions,
                            highlightColor = Slate,
                            onTransactionClick = { transaction ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        BudgetTab.Inflow -> TransactionFeedTab(
                            title = "Credits",
                            transactions = creditTransactions,
                            highlightColor = Emerald,
                            glow = true,
                            onTransactionClick = { transaction ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                        BudgetTab.Vault -> VaultTab(
                            monthlyBudgetInput = monthlyBudgetInput,
                            monthlyBudgetLimit = monthlyBudgetLimit,
                            onBudgetInputChange = { value ->
                                monthlyBudgetInput = value
                                value.toFloatOrNull()?.takeIf { it > 0f }?.let {
                                    monthlyBudgetLimit = it
                                }
                            },
                            onSliderChange = { value ->
                                monthlyBudgetLimit = value
                                monthlyBudgetInput = value.roundToInt().toString()
                            },
                            remainingAmount = remainingAmount.toDouble(),
                            overBudget = remainingAmount < 0f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = tab
                    }
                )
            }
        }
    }
}

@Composable
private fun HeaderSummary(
    totalDebits: Double,
    totalCredits: Double,
    monthlyBudgetLimit: Double,
    remainingAmount: Double
) {
    Column {
        Text(
            text = "Budget Pulse",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Live glassmorphic tracking for your monthly spend flow.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(14.dp))
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricChip(label = "Debits", value = "${formatMoney(totalDebits)}", tint = Crimson)
                MetricChip(label = "Credits", value = "${formatMoney(totalCredits)}", tint = Emerald)
                MetricChip(label = "Budget", value = "${formatMoney(monthlyBudgetLimit)}", tint = Slate)
                MetricChip(label = "Left", value = "${formatMoney(remainingAmount)}", tint = if (remainingAmount < 0) Crimson else Emerald)
            }
        }
    }
}

@Composable
private fun PulseTab(
    transactions: List<Transaction>,
    totalDebits: Double,
    budgetLimit: Double,
    remainingAmount: Double,
    progress: Float,
    onEmptyAction: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GlassCard(
            modifier = Modifier.heightIn(min = 320.dp)
        ) {
            if (transactions.isEmpty()) {
                EmptyPulseState()
            } else {
                val ringColor = when {
                    progress < 0.60f -> RingGreen
                    progress < 0.90f -> RingYellow
                    else -> RingRed
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressRing(
                        progress = progress,
                        ringColor = ringColor,
                        modifier = Modifier.size(260.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Remaining: ${currencyLabel(remainingAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingAmount < 0) Crimson else Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Debits ${formatMoney(totalDebits)} of ${formatMoney(budgetLimit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPulseState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "sleeping_cat_illustration",
                color = Color.White.copy(alpha = 0.70f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No transactions yet. The pulse is asleep.",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Incoming SMS activity will wake the dashboard automatically.",
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TransactionFeedTab(
    title: String,
    transactions: List<Transaction>,
    highlightColor: Color,
    glow: Boolean = false,
    onTransactionClick: (Transaction) -> Unit
) {
    val listState = rememberLazyListState()
    val sections = remember(transactions) { groupTransactionsByDay(transactions) }

    Column(modifier = Modifier.fillMaxSize()) {
        GlassCard(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Sticky date sections with fluid glass tiles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
                Text(
                    text = "${transactions.size}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = highlightColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (sections.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "No $title yet.",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                sections.forEach { section ->
                    stickyHeader {
                        SectionHeader(label = section.label)
                    }
                    items(section.transactions, key = { it.id }) { transaction ->
                        TransactionTile(
                            transaction = transaction,
                            highlightColor = highlightColor,
                            glow = glow,
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultTab(
    monthlyBudgetInput: String,
    monthlyBudgetLimit: Float,
    onBudgetInputChange: (String) -> Unit,
    onSliderChange: (Float) -> Unit,
    remainingAmount: Double,
    overBudget: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GlassCard {
            Text(
                text = "Monthly Budget",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Tune the limit with the slider or type a precise value.",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedTextField(
                value = monthlyBudgetInput,
                onValueChange = onBudgetInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("monthlyBudgetLimit") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = monthlyBudgetLimit.coerceAtLeast(0f),
                onValueChange = onSliderChange,
                valueRange = 0f..500000f,
                steps = 19
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Current limit: ${currencyLabel(monthlyBudgetLimit.toDouble())}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Remaining: ${currencyLabel(remainingAmount)}",
                color = if (overBudget) Crimson else Emerald,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        GlassCard {
            Text(
                text = if (overBudget) "Alert: budget exceeded." else "On track: no over-budget alert.",
                color = if (overBudget) Crimson else Emerald,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "The crimson alert color is reserved for over-budget states.",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.06f),
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TransactionTile(
    transaction: Transaction,
    highlightColor: Color,
    glow: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val amountColor = if (transaction.isCredit) Emerald else Slate
    val initials = transaction.merchant
        .split(" ", ",", ".", "-")
        .filter { it.isNotBlank() }
        .joinToString("")
        .take(2)
        .uppercase(Locale.getDefault())
        .ifBlank { "TX" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassTint),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (glow) 0.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (glow) {
                        Modifier
                            .blur(12.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    if (transaction.isCredit) Emerald.copy(alpha = 0.36f) else Slate.copy(alpha = 0.24f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(0.5.dp, GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchant,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatter.format(Date(transaction.timestamp)),
                        color = Color.White.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Rs ${formatMoney(transaction.amount)}",
                    color = if (transaction.isCredit) Emerald else amountColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun GlassBottomBar(
    selectedTab: BudgetTab,
    onTabSelected: (BudgetTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Transparent
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BudgetTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    val contentColor = if (selected) Color.White else Color.White.copy(alpha = 0.62f)
                    val background = if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(background)
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = contentColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                color = contentColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

private val BudgetTab.icon: ImageVector
    get() = when (this) {
        BudgetTab.Pulse -> Icons.Outlined.Dashboard
        BudgetTab.Flow -> Icons.AutoMirrored.Outlined.TrendingDown
        BudgetTab.Inflow -> Icons.AutoMirrored.Outlined.TrendingUp
        BudgetTab.Vault -> Icons.Outlined.Settings
    }

@Composable
private fun MetricChip(label: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.72f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassTint),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

private data class DaySection(
    val label: String,
    val transactions: List<Transaction>
)

private fun groupTransactionsByDay(transactions: List<Transaction>): List<DaySection> {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)

    return transactions
        .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .map { (date, list) ->
            DaySection(
                label = relativeDayLabel(date, today),
                transactions = list.sortedByDescending { it.timestamp }
            )
        }
}

private fun relativeDayLabel(date: LocalDate, today: LocalDate): String {
    return when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", " +
            date.dayOfMonth.toString().padStart(2, '0') + " " +
            date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " +
            date.year
    }
}

private fun formatMoney(value: Double): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value)
}

private fun currencyLabel(value: Double): String {
    return "$${formatMoney(value)}"
}

@Composable
private fun CircularProgressRing(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "ring_progress"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 12.dp.toPx()
            val diameterAdjustment = strokeWidthPx / 2f
            val size = size.minDimension - strokeWidthPx
            val topLeft = Offset(diameterAdjustment, diameterAdjustment)
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(size, size),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(size, size),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
    }
}

private fun Modifier.backgroundOrbs(): Modifier {
    return this
        .background(
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF182235),
                    DeepCharcoal
                ),
                center = Offset.Zero,
                radius = 1800f
            )
        )
        .then(
            Modifier
                .fillMaxSize()
        )
}
